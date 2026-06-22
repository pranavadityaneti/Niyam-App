package com.myniyam.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.myniyam.app.R
import com.myniyam.app.data.UserPrefs
import kotlinx.coroutines.launch

/**
 * Onboarding step 5 (SP-P-PAUSE): "How Niyam pauses you" — pick the pause length
 * and (optionally) turn on the interval check-in. Persists directly to UserPrefs
 * on Continue, then advances to the permission flow.
 */
@Composable
fun PauseStepScreen(onContinue: () -> Unit, onBack: (() -> Unit)? = null) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snap = remember { UserPrefs.snapshot() }

    var pauseSeconds by remember { mutableIntStateOf(snap.pauseLengthSeconds) }
    var intervalEnabled by remember { mutableStateOf(snap.intervalCheckInEnabled) }
    var intervalMinutes by remember { mutableIntStateOf(snap.intervalMinutes) }

    OnboardingScaffold(
        step = 5,
        title = stringResource(R.string.onb_pause_title),
        ctaEnabled = true,
        onBack = onBack,
        onContinue = {
            scope.launch {
                UserPrefs.setPauseBehaviour(ctx, intervalEnabled, intervalMinutes, pauseSeconds)
            }
            onContinue()
        }
    ) {
        PauseControls(
            pauseSeconds = pauseSeconds,
            onPauseSeconds = { pauseSeconds = it },
            intervalEnabled = intervalEnabled,
            onIntervalEnabled = { intervalEnabled = it },
            intervalMinutes = intervalMinutes,
            onIntervalMinutes = { intervalMinutes = it }
        )
    }
}
