package com.holidaymessenger.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Substitutes {name} and {firstName} tokens in a template.
 */
fun substituteTemplate(template: String, name: String): String {
    val firstName = name.trim().split(" ").firstOrNull() ?: name
    return template
        .replace("{name}", name)
        .replace("{firstName}", firstName)
}

@Composable
fun MessagePreviewDialog(
    template: String,
    sampleNames: List<String>,
    onDismiss: () -> Unit
) {
    val samples = if (sampleNames.isEmpty()) listOf("Alex", "Jordan", "Sam") else sampleNames.take(3)
    FestiveDialog(
        title = "Preview",
        onDismiss = onDismiss,
        confirmLabel = "Close",
        onConfirm = onDismiss,
        dismissLabel = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "See how your message looks when personalized:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            samples.forEach { name ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "To: $name",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            substituteTemplate(template, name),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
