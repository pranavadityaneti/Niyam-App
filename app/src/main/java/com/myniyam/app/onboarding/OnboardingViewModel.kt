package com.myniyam.app.onboarding

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myniyam.app.data.BlockList
import com.myniyam.app.data.DisplayLanguage
import com.myniyam.app.data.Intention
import com.myniyam.app.data.UserPrefs
import kotlinx.coroutines.launch

/** Holds onboarding selections across the 4 steps and persists each on confirm (spec §5/§7). */
class OnboardingViewModel : ViewModel() {

    var selectedIntention: Intention? by mutableStateOf(null)
        private set
    var selectedMantraId: String? by mutableStateOf(null)
        private set
    var selectedLanguage: DisplayLanguage by mutableStateOf(DisplayLanguage.ENGLISH)
        private set
    var selectedPackages: Set<String> by mutableStateOf(BlockList.DEFAULT_PACKAGES)
        private set

    // Step 5 (pause behaviour) — held here like every other step so back/forward
    // within onboarding doesn't lose an unsaved edit. Defaults mirror UserPrefs.
    var pauseLengthSeconds: Int by mutableStateOf(UserPrefs.snapshot().pauseLengthSeconds)
        private set
    var intervalEnabled: Boolean by mutableStateOf(UserPrefs.snapshot().intervalCheckInEnabled)
        private set
    var intervalMinutes: Int by mutableStateOf(UserPrefs.snapshot().intervalMinutes)
        private set

    fun selectIntention(intention: Intention) { selectedIntention = intention }
    fun selectMantra(id: String) { selectedMantraId = id }
    fun selectLanguage(language: DisplayLanguage) { selectedLanguage = language }
    fun togglePackage(pkg: String) {
        selectedPackages = if (pkg in selectedPackages) selectedPackages - pkg else selectedPackages + pkg
    }
    fun updatePauseLength(seconds: Int) { pauseLengthSeconds = seconds }
    fun updateIntervalEnabled(enabled: Boolean) { intervalEnabled = enabled }
    fun updateIntervalMinutes(minutes: Int) { intervalMinutes = minutes }

    fun persistPause(context: Context) {
        viewModelScope.launch {
            UserPrefs.setPauseBehaviour(context, intervalEnabled, intervalMinutes, pauseLengthSeconds)
        }
    }

    fun canContinueFromIntention() = selectedIntention != null
    fun canContinueFromMantra() = selectedMantraId != null
    fun canContinueFromApps() = selectedPackages.isNotEmpty()

    fun persistIntention(context: Context) {
        val intention = selectedIntention ?: return
        viewModelScope.launch { UserPrefs.setIntention(context, intention) }
    }

    fun persistMantra(context: Context) {
        val id = selectedMantraId ?: return
        viewModelScope.launch { UserPrefs.setCurrentMantra(context, id) }
    }

    fun persistLanguage(context: Context) {
        viewModelScope.launch { UserPrefs.setDisplayLanguage(context, selectedLanguage) }
    }

    fun persistApps(context: Context) {
        viewModelScope.launch { UserPrefs.setBlockedPackages(context, selectedPackages) }
    }

    fun persistOnboardingComplete(context: Context) {
        viewModelScope.launch {
            UserPrefs.setOnboardingComplete(context)
            UserPrefs.startTrial(context, java.time.LocalDate.now().toEpochDay())
        }
    }
}
