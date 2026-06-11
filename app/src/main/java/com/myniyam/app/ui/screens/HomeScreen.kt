package com.myniyam.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.myniyam.app.data.CurrentSadhana
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.permissions.PermissionChecker
import com.myniyam.app.progress.ProgressRepository
import com.myniyam.app.ui.theme.NiyamBackground
import com.myniyam.app.ui.theme.NiyamTheme
import com.myniyam.app.R

@Composable
fun HomeScreen(onFixProtection: () -> Unit, onBrowseLibrary: () -> Unit, onOpenSettings: () -> Unit) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var refreshKey by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val stats by produceState(
        initialValue = ProgressRepository.HomeStats(0, 14, 0, 0),
        key1 = refreshKey
    ) {
        MantraRepository.ensureLoaded(ctx)
        value = ProgressRepository.homeStats(ctx)
    }

    val protectionOk = remember(refreshKey) { PermissionChecker.allPermissionsGranted(ctx) }
    val mantra = MantraRepository.displayMantra(CurrentSadhana.MANTRA_ID)
    val firstLine = mantra.text.forScript(CurrentSadhana.LANGUAGE.script).lineSequence().first()

    // Progress fill animates 0 → dayN/dayM exactly once, when stats first resolve.
    val progressTarget = if (stats.dayM == 0) 0f else stats.dayN.toFloat() / stats.dayM
    val animatedFraction = remember { Animatable(0f) }
    LaunchedEffect(progressTarget) {
        animatedFraction.animateTo(
            targetValue = progressTarget,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }

    NiyamBackground {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.home_overline).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = NiyamTheme.colors.overlineWarm,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_content_desc),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hero day numeral
                Text(
                    text = stringResource(R.string.home_day_fmt, stats.dayN, stats.dayM),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Mantra card — floating shadowed card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color(0xFF7A3D12).copy(alpha = 0.10f),
                            spotColor = Color(0xFF7A3D12).copy(alpha = 0.10f)
                        )
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(20.dp)
                ) {
                    Text(
                        text = mantra.canonicalName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = firstLine,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { animatedFraction.value },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatChip(
                        text = stringResource(R.string.home_streak_fmt, stats.streak),
                        highlight = stats.streak >= 2
                    )
                    StatChip(
                        text = stringResource(R.string.home_today_fmt, stats.todayReads),
                        highlight = false
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onBrowseLibrary,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_browse_library),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Protection row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !protectionOk) { onFixProtection() }
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (protectionOk) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = if (protectionOk) {
                            stringResource(R.string.home_protection_ok)
                        } else {
                            stringResource(R.string.home_protection_fix)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StatChip(text: String, highlight: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (highlight) {
            MaterialTheme.colorScheme.onSecondary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier
            .background(
                color = if (highlight) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}
