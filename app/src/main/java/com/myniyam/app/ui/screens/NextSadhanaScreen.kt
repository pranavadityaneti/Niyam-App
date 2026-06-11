package com.myniyam.app.ui.screens

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
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.onboarding.SelectableCard
import com.myniyam.app.onboarding.mantraGist
import com.myniyam.app.progress.NextSadhana
import com.myniyam.app.ui.theme.NiyamBackground
import kotlinx.coroutines.launch

/**
 * Post-celebration picker for the user's next sadhana (spec §4). Offers up to three
 * recommendations from NextSadhana.candidates; selecting one re-stamps the current
 * mantra (clearing the celebration flag) and returns Home.
 */
@Composable
fun NextSadhanaScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    MantraRepository.ensureLoaded(ctx)

    val snap = UserPrefs.snapshot()
    val options = remember {
        NextSadhana.candidates(snap.selectedIntention, snap.completedMantraIds, snap.currentMantraId)
    }
    var selectedId by remember { mutableStateOf<String?>(null) }

    NiyamBackground {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
            ) {
            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.next_title),
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                options.forEach { mantra ->
                    SelectableCard(
                        text = mantra.canonicalName,
                        supportingText = mantraGist(mantra.meaning.en),
                        trailingChip = stringResource(R.string.onb_read_time_fmt, mantra.estimatedReadSeconds),
                        selected = selectedId == mantra.id,
                        onClick = { selectedId = mantra.id }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val id = selectedId ?: return@Button
                    scope.launch {
                        UserPrefs.setCurrentMantra(ctx, id)
                        onDone()
                    }
                },
                enabled = selectedId != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = stringResource(R.string.onb_continue),
                    style = MaterialTheme.typography.labelLarge
                )
            }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
