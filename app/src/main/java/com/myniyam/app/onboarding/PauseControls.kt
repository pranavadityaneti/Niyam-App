package com.myniyam.app.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.service.PauseConfig
import com.myniyam.app.ui.theme.NiyamTheme
import com.myniyam.app.ui.theme.PumpkinOrange

/**
 * Shared pause-behaviour controls (SP-P-PAUSE) used by the onboarding step and the
 * Settings editor. Stateless — the parent hoists the three values and the setters.
 * Pause length 15..60s; interval check-in is opt-in with 30/60/120-minute frequency.
 */
@Composable
fun ColumnScope.PauseControls(
    pauseSeconds: Int,
    onPauseSeconds: (Int) -> Unit,
    intervalEnabled: Boolean,
    onIntervalEnabled: (Boolean) -> Unit,
    intervalMinutes: Int,
    onIntervalMinutes: (Int) -> Unit
) {
    SectionLabel(stringResource(R.string.pause_length_label))
    Spacer(Modifier.height(10.dp))
    listOf(15, 20, 30, 45, 60).forEach { secs ->
        SelectableCard(
            text = stringResource(R.string.pause_secs_fmt, secs),
            selected = pauseSeconds == secs,
            onClick = { onPauseSeconds(secs) }
        )
    }

    Spacer(Modifier.height(20.dp))

    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.pause_interval_toggle), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(2.dp))
            Text(
                stringResource(R.string.pause_interval_caption),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = intervalEnabled,
            onCheckedChange = onIntervalEnabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = PumpkinOrange
            )
        )
    }

    if (intervalEnabled) {
        Spacer(Modifier.height(14.dp))
        SectionLabel(stringResource(R.string.pause_freq_label))
        Spacer(Modifier.height(10.dp))
        PauseConfig.ALLOWED_MINUTES.forEach { mins ->
            SelectableCard(
                text = stringResource(R.string.pause_mins_fmt, mins),
                selected = intervalMinutes == mins,
                onClick = { onIntervalMinutes(mins) }
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = NiyamTheme.colors.overlineWarm
    )
}
