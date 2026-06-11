package com.myniyam.app.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myniyam.app.BuildConfig
import com.myniyam.app.R
import com.myniyam.app.data.ThemePref
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.notifications.CompletionNotifier
import com.myniyam.app.ui.theme.NiyamBackground
import com.myniyam.app.ui.theme.NiyamTheme
import com.myniyam.app.ui.theme.PumpkinOrange
import com.myniyam.app.ui.theme.ThemeState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun SettingsScreen(
    onOpenCurrentSadhana: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenIntention: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snap = UserPrefs.snapshot()

    var notifyOn by remember { mutableStateOf(snap.notifyOnCompletion) }
    var permissionGranted by remember { mutableStateOf(CompletionNotifier.hasPostPermission(ctx)) }
    val currentTheme = ThemeState.pref

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
    }

    NiyamBackground {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(20.dp))

                SectionLabel(stringResource(R.string.settings_section_sadhana))
                SectionCard {
                    NavRow(
                        stringResource(R.string.settings_row_current_sadhana),
                        onOpenCurrentSadhana,
                        leading = Icons.Default.Star
                    )
                    NavRow(
                        stringResource(R.string.settings_row_language),
                        onOpenLanguage,
                        leading = Icons.AutoMirrored.Filled.List
                    )
                    NavRow(
                        stringResource(R.string.settings_row_apps),
                        onOpenApps,
                        trailing = stringResource(R.string.settings_apps_count_fmt, snap.blockedPackages.size),
                        leading = Icons.Default.Lock
                    )
                    NavRow(
                        stringResource(R.string.settings_row_intention),
                        onOpenIntention,
                        leading = Icons.Default.Edit
                    )
                }

                Spacer(Modifier.height(20.dp))
                SectionLabel(stringResource(R.string.settings_section_app))

                SectionCard {
                    Text(
                        stringResource(R.string.settings_row_appearance),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppearanceSegment(
                            label = stringResource(R.string.settings_appearance_light),
                            selected = currentTheme == ThemePref.LIGHT,
                            onClick = { scope.launch { UserPrefs.setThemePref(ctx, ThemePref.LIGHT) } },
                            modifier = Modifier.weight(1f)
                        )
                        AppearanceSegment(
                            label = stringResource(R.string.settings_appearance_dark),
                            selected = currentTheme == ThemePref.DARK,
                            onClick = { scope.launch { UserPrefs.setThemePref(ctx, ThemePref.DARK) } },
                            modifier = Modifier.weight(1f)
                        )
                        AppearanceSegment(
                            label = stringResource(R.string.settings_appearance_system),
                            selected = currentTheme == ThemePref.SYSTEM,
                            onClick = { scope.launch { UserPrefs.setThemePref(ctx, ThemePref.SYSTEM) } },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = NiyamTheme.colors.overlineWarm,
                            modifier = Modifier.padding(end = 14.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.settings_row_notify), style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (!permissionGranted && notifyOn) {
                                    stringResource(R.string.settings_notify_blocked)
                                } else {
                                    stringResource(R.string.settings_notify_caption)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = notifyOn,
                            onCheckedChange = { wantOn ->
                                notifyOn = wantOn
                                scope.launch { UserPrefs.setNotifyOnCompletion(ctx, wantOn) }
                                if (wantOn &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !CompletionNotifier.hasPostPermission(ctx)
                                ) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = PumpkinOrange
                            )
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))
                Text(
                    stringResource(R.string.settings_version_fmt, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Floating section card (SP-8 spec §2): warm-white surface, 24dp radius, soft
 * warm ambient shadow. Groups a section's rows on the sunrise canvas.
 */
@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0xFF7A3D12).copy(alpha = 0.10f),
                spotColor = Color(0xFF7A3D12).copy(alpha = 0.10f)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
        content = content
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = NiyamTheme.colors.overlineWarm,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun NavRow(
    label: String,
    onClick: () -> Unit,
    trailing: String? = null,
    leading: ImageVector? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            Icon(
                imageVector = leading,
                contentDescription = null,
                tint = NiyamTheme.colors.overlineWarm,
                modifier = Modifier.padding(end = 14.dp)
            )
        }
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (trailing != null) {
            Text(
                trailing,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text("  ›", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AppearanceSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        textAlign = TextAlign.Center,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .height(44.dp)
            .background(
                color = if (selected) {
                    NiyamTheme.colors.orangeTint
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
    )
}
