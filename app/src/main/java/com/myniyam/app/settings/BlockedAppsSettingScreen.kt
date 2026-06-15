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
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.onboarding.AppIcon
import com.myniyam.app.onboarding.SelectableCard
import com.myniyam.app.ui.theme.NiyamBackground
import kotlinx.coroutines.launch

private val APP_CATALOG: List<Pair<String, String>> = listOf(
    "Instagram" to "com.instagram.android",
    "YouTube" to "com.google.android.youtube",
    "Facebook" to "com.facebook.katana",
    "X" to "com.twitter.android",
    "Reddit" to "com.reddit.frontpage",
    "Snapchat" to "com.snapchat.android",
    "TikTok" to "com.zhiliaoapp.musically"
)

@Composable
fun BlockedAppsSettingScreen(onSaved: () -> Unit, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf(UserPrefs.snapshot().blockedPackages) }

    NiyamBackground {
        Scaffold(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                SettingTopBar(stringResource(R.string.settings_apps_title), onBack)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.settings_apps_empty_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    APP_CATALOG.forEach { (name, pkg) ->
                        SelectableCard(
                            text = name,
                            selected = pkg in selected,
                            onClick = {
                                selected = if (pkg in selected) selected - pkg else selected + pkg
                            },
                            leading = { AppIcon(pkg = pkg, name = name) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            UserPrefs.setBlockedPackages(ctx, selected)
                            onSaved()
                        }
                    },
                    enabled = selected.isNotEmpty(),
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
