package com.myniyam.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.myniyam.app.R
import com.myniyam.app.data.Intention
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.StarterMantras

@Composable
fun MantraPickerScreen(vm: OnboardingViewModel, onContinue: () -> Unit) {
    val ctx = LocalContext.current
    MantraRepository.ensureLoaded(ctx)
    val intention = vm.selectedIntention ?: Intention.SADHANA
    val options = StarterMantras.forIntention(intention)

    OnboardingScaffold(
        step = 2,
        title = stringResource(R.string.onb_mantra_title),
        ctaEnabled = vm.canContinueFromMantra(),
        onContinue = {
            vm.persistMantra(ctx)
            onContinue()
        }
    ) {
        options.forEach { mantra ->
            SelectableCard(
                text = mantra.canonicalName,
                supportingText = mantra.meaning.en.substringBefore(". ") + ".",
                trailingChip = stringResource(R.string.onb_read_time_fmt, mantra.estimatedReadSeconds),
                selected = vm.selectedMantraId == mantra.id,
                onClick = { vm.selectMantra(mantra.id) }
            )
        }
    }
}
