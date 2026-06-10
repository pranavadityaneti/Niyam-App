package com.myniyam.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.data.CurrentSadhana
import com.myniyam.app.data.MantraRepository

/**
 * Shown when a sadhana reaches its completion threshold (spec §4). Celebrates the
 * just-completed mantra and offers two paths: choose a new sadhana, or keep going
 * with the current mantra. Colours come only from MaterialTheme.colorScheme.
 */
@Composable
fun CelebrationScreen(
    onChooseNext: () -> Unit,
    onKeepCurrent: () -> Unit
) {
    val mantra = MantraRepository.displayMantra(CurrentSadhana.MANTRA_ID)

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(56.dp)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.celebration_overline).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(
                    R.string.celebration_title_fmt,
                    mantra.completionThresholdDays,
                    mantra.canonicalName
                ),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.celebration_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = onChooseNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = stringResource(R.string.celebration_next),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onKeepCurrent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = stringResource(R.string.celebration_keep),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
