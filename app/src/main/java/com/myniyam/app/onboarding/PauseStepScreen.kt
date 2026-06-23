package com.myniyam.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.myniyam.app.R

/**
 * Onboarding step 5 (SP-P-PAUSE): "How Niyam pauses you" — pick the pause length
 * and (optionally) turn on the interval check-in. State lives in the shared
 * [OnboardingViewModel] (like every other step), so back-then-forward keeps the
 * edit; persisted to UserPrefs on Continue, then advance to the permission flow.
 */
@Composable
fun PauseStepScreen(vm: OnboardingViewModel, onContinue: () -> Unit, onBack: (() -> Unit)? = null) {
    val ctx = LocalContext.current

    OnboardingScaffold(
        step = 5,
        title = stringResource(R.string.onb_pause_title),
        ctaEnabled = true,
        onBack = onBack,
        onContinue = {
            vm.persistPause(ctx)
            onContinue()
        }
    ) {
        PauseControls(
            pauseSeconds = vm.pauseLengthSeconds,
            onPauseSeconds = vm::updatePauseLength,
            intervalEnabled = vm.intervalEnabled,
            onIntervalEnabled = vm::updateIntervalEnabled,
            intervalMinutes = vm.intervalMinutes,
            onIntervalMinutes = vm::updateIntervalMinutes
        )
    }
}
