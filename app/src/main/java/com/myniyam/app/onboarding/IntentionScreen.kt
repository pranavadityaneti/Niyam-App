package com.myniyam.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.myniyam.app.R
import com.myniyam.app.data.Intention

@Composable
fun IntentionScreen(vm: OnboardingViewModel, onContinue: () -> Unit) {
    val options = listOf(
        Intention.FOCUS to stringResource(R.string.onb_intention_focus),
        Intention.CALM to stringResource(R.string.onb_intention_calm),
        Intention.SADHANA to stringResource(R.string.onb_intention_sadhana),
        Intention.DHARMA to stringResource(R.string.onb_intention_dharma),
        Intention.DEVOTION to stringResource(R.string.onb_intention_devotion)
    )
    OnboardingScaffold(
        step = 1,
        title = stringResource(R.string.onb_intention_title),
        ctaEnabled = vm.canContinueFromIntention(),
        onContinue = onContinue
    ) {
        options.forEach { (intention, label) ->
            SelectableCard(
                text = label,
                selected = vm.selectedIntention == intention,
                onClick = { vm.selectIntention(intention) }
            )
        }
    }
}
