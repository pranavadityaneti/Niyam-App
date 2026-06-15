package com.myniyam.app.library

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myniyam.app.R
import com.myniyam.app.billing.Entitlements
import com.myniyam.app.data.CurrentSadhana
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.Script
import com.myniyam.app.data.Deity
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.progress.ProgressRepository
import com.myniyam.app.ui.theme.NiyamBackground
import com.myniyam.app.ui.theme.NiyamTheme
import com.myniyam.app.ui.theme.SaladGreen
import kotlinx.coroutines.launch

@Composable
fun MantraDetailScreen(mantraId: String, onSwitched: () -> Unit, onMissing: () -> Unit, onPaywall: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    MantraRepository.ensureLoaded(ctx)
    val mantra = MantraRepository.byId(mantraId)
    if (mantra == null) {
        LaunchedEffect(Unit) { onMissing() }
        return
    }

    val snap = UserPrefs.snapshot()
    val state = Entitlements.state(snap.premiumActive, snap.trialStartEpochDay, java.time.LocalDate.now().toEpochDay())
    val isCurrent = mantra.id == snap.currentMantraId
    val isCompleted = mantra.id in snap.completedMantraIds
    val locked = !Entitlements.canUseMantra(state, mantra.id, snap.currentMantraId)
    val lang = CurrentSadhana.LANGUAGE

    var showDialog by remember { mutableStateOf(false) }
    var currentDayN by remember { mutableStateOf<Int?>(null) }

    fun performSwitch() {
        scope.launch {
            UserPrefs.setCurrentMantra(ctx, mantra.id)
            onSwitched()
        }
    }

    NiyamBackground {
        Scaffold(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "${mantra.sourceCategory.label().uppercase()} · ${mantra.source.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NiyamTheme.colors.overlineWarm
                )
                Spacer(Modifier.height(10.dp))
                Text(mantra.canonicalName, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoChip(mantra.sourceCategory.label())
                    InfoChip("~${mantra.estimatedReadSeconds}s")
                    if (mantra.deity != Deity.UNIVERSAL) {
                        InfoChip(mantra.deity.label())
                    }
                    if (isCompleted) {
                        MarkerChip(stringResource(R.string.library_completed_marker), SaladGreen)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        Modifier
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
                            mantra.text.forScript(lang.script),
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 30.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (lang.script != Script.ROMAN) {
                            Spacer(Modifier.height(14.dp))
                            Text(
                                mantra.text.forScript(Script.ROMAN),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            mantra.meaning.forLang(lang.meaningLang),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "~${mantra.estimatedReadSeconds}s · ${mantra.completionThresholdDays}-day journey",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (locked) {
                            onPaywall()
                            return@Button
                        }
                        if (isCurrent) return@Button
                        val currentHasProgress = !snap.pendingCelebration &&
                            snap.currentMantraId !in snap.completedMantraIds
                        if (currentHasProgress) {
                            scope.launch {
                                val dayN = runCatching { ProgressRepository.homeStats(ctx).dayN }.getOrNull()
                                currentDayN = dayN
                                if (dayN != null && dayN >= 1) {
                                    showDialog = true
                                } else {
                                    performSwitch()
                                }
                            }
                        } else {
                            performSwitch()
                        }
                    },
                    enabled = !isCurrent,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        stringResource(
                            when {
                                locked -> R.string.detail_unlock_premium
                                isCurrent -> R.string.detail_is_current
                                isCompleted -> R.string.detail_practice_again
                                else -> R.string.detail_make_current
                            }
                        ),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    if (showDialog) {
        val currentName = MantraRepository.displayMantra(snap.currentMantraId).canonicalName
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.detail_switch_title)) },
            text = {
                Text(
                    currentDayN?.takeIf { it > 0 }?.let {
                        pluralStringResource(R.plurals.detail_switch_body_plural, it, it, currentName)
                    } ?: stringResource(R.string.detail_switch_body_nocount_fmt, currentName)
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false; performSwitch() }) {
                    Text(stringResource(R.string.detail_switch_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.detail_switch_cancel))
                }
            }
        )
    }
}

@Composable
private fun InfoChip(label: String) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = NiyamTheme.colors.inkMuted,
        modifier = Modifier
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(999.dp),
                ambientColor = Color(0xFF7A3D12).copy(alpha = 0.10f),
                spotColor = Color(0xFF7A3D12).copy(alpha = 0.10f)
            )
            .background(NiyamTheme.colors.chipFill, RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}
