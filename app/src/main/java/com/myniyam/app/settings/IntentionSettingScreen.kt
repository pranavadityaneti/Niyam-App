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
import com.myniyam.app.data.Intention
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.onboarding.SelectableCard
import com.myniyam.app.ui.theme.NiyamBackground
import kotlinx.coroutines.launch

@Composable
fun IntentionSettingScreen(onSaved: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf(UserPrefs.snapshot().selectedIntention) }

    val options = listOf(
        Intention.FOCUS to stringResource(R.string.onb_intention_focus),
        Intention.CALM to stringResource(R.string.onb_intention_calm),
        Intention.SADHANA to stringResource(R.string.onb_intention_sadhana),
        Intention.DHARMA to stringResource(R.string.onb_intention_dharma),
        Intention.DEVOTION to stringResource(R.string.onb_intention_devotion)
    )

    NiyamBackground {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.settings_intention_title), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    options.forEach { (intention, label) ->
                        SelectableCard(
                            text = label,
                            selected = selected == intention,
                            onClick = { selected = intention }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            UserPrefs.setIntention(ctx, selected)
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
