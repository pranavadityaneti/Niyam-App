package com.myniyam.app.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myniyam.app.R
import com.myniyam.app.data.CurrentSadhana
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.Script
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.progress.ProgressRepository
import kotlinx.coroutines.launch

@Composable
fun MantraDetailScreen(mantraId: String, onSwitched: () -> Unit, onMissing: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    MantraRepository.ensureLoaded(ctx)
    val mantra = MantraRepository.byId(mantraId)
    if (mantra == null) {
        LaunchedEffect(Unit) { onMissing() }
        return
    }

    val snap = UserPrefs.snapshot()
    val isCurrent = mantra.id == snap.currentMantraId
    val isCompleted = mantra.id in snap.completedMantraIds
    val lang = CurrentSadhana.LANGUAGE

    var showDialog by remember { mutableStateOf(false) }
    var currentDayN by remember { mutableStateOf<Int?>(null) }

    fun performSwitch() {
        scope.launch {
            UserPrefs.setCurrentMantra(ctx, mantra.id)
            onSwitched()
        }
    }

    Scaffold { padding ->
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Text(mantra.canonicalName, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    mantra.text.forScript(lang.script),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Serif,
                        lineHeight = 30.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    mantra.text.forScript(Script.ROMAN),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
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
            Button(
                onClick = {
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

    if (showDialog) {
        val currentName = MantraRepository.displayMantra(snap.currentMantraId).canonicalName
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.detail_switch_title)) },
            text = {
                Text(
                    currentDayN?.takeIf { it > 0 }?.let {
                        stringResource(R.string.detail_switch_body_fmt, it, currentName)
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
