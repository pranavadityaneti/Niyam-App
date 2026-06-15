package com.myniyam.app.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.myniyam.app.R
import com.myniyam.app.data.DisplayLanguage

private val LANGUAGE_LABELS: List<Triple<DisplayLanguage, String, String>> = listOf(
    Triple(DisplayLanguage.ENGLISH, "English", "Roman script"),
    Triple(DisplayLanguage.HINDI, "हिन्दी", "Hindi"),
    Triple(DisplayLanguage.DEVANAGARI_SANSKRIT, "संस्कृतम् — देवनागरी", "Sanskrit (Devanagari)"),
    Triple(DisplayLanguage.MARATHI, "मराठी", "Marathi"),
    Triple(DisplayLanguage.TELUGU, "తెలుగు", "Telugu"),
    Triple(DisplayLanguage.TAMIL, "தமிழ்", "Tamil"),
    Triple(DisplayLanguage.KANNADA, "ಕನ್ನಡ", "Kannada"),
    Triple(DisplayLanguage.BENGALI, "বাংলা", "Bengali"),
    Triple(DisplayLanguage.GUJARATI, "ગુજરાતી", "Gujarati")
)

@Composable
fun LanguageScreen(vm: OnboardingViewModel, onContinue: () -> Unit) {
    val ctx = LocalContext.current
    OnboardingScaffold(
        step = 1,
        title = stringResource(R.string.onb_language_title),
        ctaEnabled = true,
        onContinue = {
            vm.persistLanguage(ctx)
            onContinue()
        }
    ) {
        LANGUAGE_LABELS.forEach { (lang, native, caption) ->
            SelectableCard(
                text = native,
                supportingText = caption,
                selected = vm.selectedLanguage == lang,
                onClick = { vm.selectLanguage(lang) }
            )
        }
    }
}
