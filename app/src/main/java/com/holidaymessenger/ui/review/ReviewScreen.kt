package com.holidaymessenger.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.holidaymessenger.data.db.entity.Channel
import com.holidaymessenger.data.review.ReviewItem
import com.holidaymessenger.util.HolidayPlanner.ReviewState
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    onBack: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    val groups = uiState.items.groupBy { it.holidayId }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Review messages",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (uiState.isLoading) "Loading..." else "Nothing to review right now. When a holiday is a day away, its messages show up here. Nothing sends until you approve it.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                groups.forEach { (holidayId, holidayItems) ->
                    item(key = "header_$holidayId") {
                        HolidayHeader(
                            items = holidayItems,
                            canAct = holidayItems.any { it.state == ReviewState.PENDING },
                            onApproveAll = { viewModel.approveAll(holidayId) },
                            onSkipAll = { viewModel.skipAll(holidayId) }
                        )
                    }
                    holidayItems.forEach { reviewItem ->
                        item(key = reviewItem.key) {
                            ReviewCard(
                                item = reviewItem,
                                onApprove = { viewModel.approve(reviewItem) },
                                onSkip = { viewModel.skip(reviewItem) }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun HolidayHeader(
    items: List<ReviewItem>,
    canAct: Boolean,
    onApproveAll: () -> Unit,
    onSkipAll: () -> Unit
) {
    val first = items.first()
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            first.holidayName,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
        )
        Text(
            first.date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (canAct) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onApproveAll) { Text("Approve all") }
                TextButton(onClick = onSkipAll) { Text("Skip all") }
            }
        }
    }
}

@Composable
private fun ReviewCard(
    item: ReviewItem,
    onApprove: () -> Unit,
    onSkip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.contactName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    stateLabel(item.state),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                howItSends(item),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.text, style = MaterialTheme.typography.bodyMedium)

            if (item.state == ReviewState.PENDING || item.state == ReviewState.SKIPPED) {
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onApprove) { Text("Approve") }
                    if (item.state == ReviewState.PENDING) {
                        OutlinedButton(onClick = onSkip) { Text("Skip") }
                    }
                }
            }
        }
    }
}

private fun stateLabel(state: ReviewState): String = when (state) {
    ReviewState.PENDING -> "Needs review"
    ReviewState.APPROVED -> "Approved"
    ReviewState.SENT -> "Sent"
    ReviewState.SKIPPED -> "Skipped"
}

private fun howItSends(item: ReviewItem): String = when {
    item.isGroup -> "Group chat: you get a notification to tap and send"
    item.channel == Channel.WHATSAPP -> "WhatsApp: you get a notification to tap and send"
    else -> "Text message: sends on its own between 9 and 11 AM, or right away if that has passed"
}
