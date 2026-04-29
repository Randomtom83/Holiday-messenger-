package com.holidaymessenger.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.holidaymessenger.data.db.entity.MessageLog
import com.holidaymessenger.data.db.entity.MessageStatus
import com.holidaymessenger.ui.home.UpcomingSend
import com.holidaymessenger.util.Prefs
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val holidayTheme = getHolidayTheme(uiState.nextHoliday?.name)
    val context = LocalContext.current

    // Confetti trigger on new SENT log
    var initialSeenLogId by rememberSaveable { mutableStateOf<Long?>(null) }
    var confettiParties by remember { mutableStateOf<List<Party>>(emptyList()) }

    LaunchedEffect(uiState.recentMessages) {
        val latestSent = uiState.recentMessages.firstOrNull { it.status == MessageStatus.SENT }
        if (latestSent != null) {
            if (initialSeenLogId == null) {
                initialSeenLogId = latestSent.id
                Prefs.setLastSeenLogId(context, latestSent.id)
            } else if (latestSent.id > (initialSeenLogId ?: -1L)) {
                initialSeenLogId = latestSent.id
                Prefs.setLastSeenLogId(context, latestSent.id)
                confettiParties = listOf(
                    Party(
                        speed = 0f,
                        maxSpeed = 30f,
                        damping = 0.9f,
                        angle = 270,
                        spread = 120,
                        colors = listOf(0xFFC62828.toInt(), 0xFF2E7D32.toInt(), 0xFFF9A825.toInt()),
                        position = Position.Relative(0.5, 0.0),
                        emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(40)
                    )
                )
                delay(2200)
                confettiParties = emptyList()
            }
        }
    }

    // Joy score delta toast
    var prevScore by rememberSaveable { mutableStateOf(-1) }
    var scoreDelta by remember { mutableStateOf(0) }
    var showDelta by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.joyScore) {
        if (prevScore < 0) {
            prevScore = uiState.joyScore
        } else if (uiState.joyScore > prevScore) {
            scoreDelta = uiState.joyScore - prevScore
            prevScore = uiState.joyScore
            showDelta = true
            delay(1500)
            showDelta = false
        } else {
            prevScore = uiState.joyScore
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Festive Joy ",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = holidayTheme.primaryColor
                                )
                            )
                            Text("✨", style = MaterialTheme.typography.titleLarge)
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToHistory) {
                            Icon(Icons.Filled.History, contentDescription = "History", tint = holidayTheme.primaryColor)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = holidayTheme.primaryColor.copy(alpha = 0.05f)
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    HolidayHeroCard(
                        holidayName = uiState.nextHoliday?.name ?: "No Holidays",
                        daysLeft = uiState.daysToNextHoliday,
                        theme = holidayTheme
                    )
                }

                item {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                JoyMeter(
                                    progress = uiState.joyMeterProgress,
                                    themeColor = holidayTheme.primaryColor,
                                    isBoosted = uiState.isBoosted,
                                    breakdown = uiState.joyBreakdown,
                                    streak = uiState.streak
                                )
                            }
                        }
                        AnimatedVisibility(
                            visible = showDelta,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Text(
                                "+$scoreDelta 🌟",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = holidayTheme.primaryColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                item {
                    UpcomingSendsCard(
                        upcoming = uiState.upcomingSends,
                        themeColor = holidayTheme.primaryColor
                    )
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Recent Magic",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            modifier = Modifier.weight(1f)
                        )
                        if (uiState.recentMessages.isNotEmpty()) {
                            TextButton(onClick = onNavigateToHistory) {
                                Text("See All", color = holidayTheme.primaryColor)
                            }
                        }
                    }
                }

                if (uiState.recentMessages.isEmpty()) {
                    item {
                        EmptyMessagesCard(holidayTheme.primaryColor)
                    }
                } else {
                    items(uiState.recentMessages) { log ->
                        ModernMessageLogCard(log, holidayTheme.primaryColor)
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }

        if (confettiParties.isNotEmpty()) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = confettiParties
            )
        }
    }
}

@Composable
fun UpcomingSendsCard(upcoming: List<UpcomingSend>, themeColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Coming Up ✨",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
            )
            Spacer(Modifier.height(12.dp))
            if (upcoming.isEmpty()) {
                Text(
                    "No upcoming magic — assign people to your enabled holidays!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                upcoming.forEach { u ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = themeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${u.label} → ${u.contactName}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                formatRelativeTime(u.scheduledTime),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatRelativeTime(epochMs: Long): String {
    val now = System.currentTimeMillis()
    val diffMs = epochMs - now
    if (diffMs <= 0) return "Soon"
    val days = diffMs / (24 * 60 * 60 * 1000)
    val instant = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault())
    val timeStr = instant.format(DateTimeFormatter.ofPattern("h:mm a"))
    return when {
        days <= 0L -> "Today, $timeStr"
        days == 1L -> "Tomorrow, $timeStr"
        else -> "in $days days, $timeStr"
    }
}

@Composable
fun HolidayHeroCard(
    holidayName: String,
    daysLeft: Long,
    theme: HolidayTheme
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(theme.primaryColor, theme.secondaryColor)
                    )
                )
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text(
                    holidayName,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )
                )
                Text(
                    if (daysLeft == 0L) "IT'S TIME! 🎉" else "$daysLeft DAYS UNTIL PARTY!",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Icon(
                imageVector = theme.icon,
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 30.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale),
                tint = Color.White.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
fun JoyMeter(
    progress: Float,
    themeColor: Color,
    isBoosted: Boolean = false,
    breakdown: com.holidaymessenger.util.JoyMeterRules.Breakdown,
    streak: Int
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessLow),
        label = "Joy Progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isBoosted) 24.dp else 12.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Joy Meter",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black)
                        )
                        if (streak > 0) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFFF9A825).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "🔥 $streak streak",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100)
                                    )
                                )
                            }
                        }
                    }
                    Text(
                        "Your festive energy level",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = themeColor.copy(alpha = 0.1f),
                    shape = CircleShape
                ) {
                    Text(
                        "${(animatedProgress * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color = themeColor,
                trackColor = themeColor.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "${breakdown.squadCount} squad · ${breakdown.enabledHolidayCount} holidays · ${breakdown.sentCount} sends",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ModernMessageLogCard(log: MessageLog, themeColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(themeColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    log.contactName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = themeColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    log.contactName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    log.message,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = if (log.status == MessageStatus.SENT) Icons.Default.AutoAwesome else Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (log.status == MessageStatus.SENT) themeColor else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun EmptyMessagesCard(themeColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = themeColor.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "The magic hasn't started yet!",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Pick your squad and start the countdown!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

data class HolidayTheme(
    val primaryColor: Color,
    val secondaryColor: Color,
    val icon: ImageVector
)

fun getHolidayTheme(holidayName: String?): HolidayTheme {
    return when (holidayName) {
        "Eid al-Fitr" -> HolidayTheme(Color(0xFF2E7D32), Color(0xFFFDD835), Icons.Default.Nightlight)
        "Eid al-Adha" -> HolidayTheme(Color(0xFF2E7D32), Color(0xFFFDD835), Icons.Default.Nightlight)
        "Diwali" -> HolidayTheme(Color(0xFFFF9800), Color(0xFFD32F2F), Icons.Default.Lightbulb)
        "Lunar New Year" -> HolidayTheme(Color(0xFFD32F2F), Color(0xFFFDD835), Icons.Default.Celebration)
        "Hanukkah" -> HolidayTheme(Color(0xFF1976D2), Color(0xFFBBDEFB), Icons.Default.Light)
        "Easter" -> HolidayTheme(Color(0xFF9C27B0), Color(0xFFE1BEE7), Icons.Default.Egg)
        "Election Day" -> HolidayTheme(Color(0xFFB71C1C), Color(0xFF0D47A1), Icons.Default.HowToVote)
        "Mother's Day" -> HolidayTheme(Color(0xFFE91E63), Color(0xFFFCE4EC), Icons.Default.LocalFlorist)
        "Father's Day" -> HolidayTheme(Color(0xFF1976D2), Color(0xFFE3F2FD), Icons.Default.Work)
        "Christmas" -> HolidayTheme(Color(0xFFD32F2F), Color(0xFF388E3C), Icons.Default.Celebration)
        "New Year's Day" -> HolidayTheme(Color(0xFF1A237E), Color(0xFFFDD835), Icons.Default.AutoAwesome)
        "Halloween" -> HolidayTheme(Color(0xFFFF9800), Color(0xFF212121), Icons.Default.Nightlight)
        "Valentine's Day" -> HolidayTheme(Color(0xFFE91E63), Color(0xFFF48FB1), Icons.Default.Favorite)
        "Thanksgiving" -> HolidayTheme(Color(0xFF795548), Color(0xFFFFA000), Icons.Default.Restaurant)
        "Independence Day" -> HolidayTheme(Color(0xFFB71C1C), Color(0xFF0D47A1), Icons.Default.Flag)
        "Juneteenth" -> HolidayTheme(Color(0xFFB71C1C), Color(0xFF1B5E20), Icons.Default.Star)
        else -> HolidayTheme(Color(0xFF6200EE), Color(0xFF03DAC5), Icons.Default.Event)
    }
}
