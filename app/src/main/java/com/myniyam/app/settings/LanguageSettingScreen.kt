package com.myniyam.app.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.billing.Entitlements
import com.myniyam.app.data.DisplayLanguage
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.onboarding.SelectableCard
import com.myniyam.app.ui.theme.NiyamBackground
import kotlinx.coroutines.launch

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
fun LanguageSettingScreen(onSaved: () -> Unit, onPaywall: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snap = UserPrefs.snapshot()
    val state = Entitlements.state(snap.premiumActive, snap.trialStartEpochDay, java.time.LocalDate.now().toEpochDay())
    var selected by remember { mutableStateOf(snap.displayLanguage) }

    NiyamBackground {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.settings_language_title), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    LANGUAGE_LABELS.forEach { (lang, native, caption) ->
                        val usable = Entitlements.canUseLanguage(state, lang, snap.displayLanguage)
                        SelectableCard(
                            text = native,
                            supportingText = if (usable) {
                                caption
                            } else {
                                caption + stringResource(R.string.settings_language_premium_suffix)
                            },
                            selected = selected == lang,
                            onClick = { if (usable) selected = lang else onPaywall() }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            UserPrefs.setDisplayLanguage(ctx, selected)
                            onSaved()
                        }
                    },
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(stringResource(R.string.settings_save), style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
