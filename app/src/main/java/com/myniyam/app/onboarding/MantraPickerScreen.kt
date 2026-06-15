package com.myniyam.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.myniyam.app.R
import com.myniyam.app.data.Intention
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.StarterMantras

private const val GIST_MAX_CHARS = 90

/**
 * One-line gist of a meaning: the first sentence, or a word-boundary
 * truncation for single-sentence meanings with no ". " delimiter
 * (the two Sahasranama entries).
 */
internal fun mantraGist(meaningEn: String): String {
    val first = meaningEn.substringBefore(". ", meaningEn).removeSuffix(".")
    if (first.length <= GIST_MAX_CHARS) return "$first."
    val atWord = first.take(GIST_MAX_CHARS).substringBeforeLast(' ').trimEnd(',', ';', ':', '—', '-')
    return "$atWord…"
}

@Composable
fun MantraPickerScreen(vm: OnboardingViewModel, onContinue: () -> Unit, onBack: (() -> Unit)? = null) {
    val ctx = LocalContext.current
    MantraRepository.ensureLoaded(ctx)
    val intention = vm.selectedIntention ?: Intention.SADHANA
    val options = StarterMantras.forIntention(intention)

    OnboardingScaffold(
        step = 3,
        title = stringResource(R.string.onb_mantra_title),
        ctaEnabled = vm.canContinueFromMantra(),
        onContinue = {
            vm.persistMantra(ctx)
            onContinue()
        },
        onBack = onBack
    ) {
        options.forEach { mantra ->
            SelectableCard(
                text = mantra.canonicalName,
                supportingText = mantraGist(mantra.meaning.en),
                trailingChip = stringResource(R.string.onb_read_time_fmt, mantra.estimatedReadSeconds),
                selected = vm.selectedMantraId == mantra.id,
                onClick = { vm.selectMantra(mantra.id) }
            )
        }
    }
}
