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
import androidx.compose.runtime.mutableIntStateOf
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
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.onboarding.PauseControls
import com.myniyam.app.ui.theme.NiyamBackground
import kotlinx.coroutines.launch

/** Settings editor for pause behaviour (SP-P-PAUSE) — mirrors the onboarding step. */
@Composable
fun PauseSettingScreen(onSaved: () -> Unit, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snap = remember { UserPrefs.snapshot() }

    var pauseSeconds by remember { mutableIntStateOf(snap.pauseLengthSeconds) }
    var intervalEnabled by remember { mutableStateOf(snap.intervalCheckInEnabled) }
    var intervalMinutes by remember { mutableIntStateOf(snap.intervalMinutes) }

    NiyamBackground {
        Scaffold(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                SettingTopBar(stringResource(R.string.settings_pause_title), onBack)
                Spacer(Modifier.height(20.dp))
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    PauseControls(
                        pauseSeconds = pauseSeconds,
                        onPauseSeconds = { pauseSeconds = it },
                        intervalEnabled = intervalEnabled,
                        onIntervalEnabled = { intervalEnabled = it },
                        intervalMinutes = intervalMinutes,
                        onIntervalMinutes = { intervalMinutes = it }
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            UserPrefs.setPauseBehaviour(ctx, intervalEnabled, intervalMinutes, pauseSeconds)
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
