package com.myniyam.app.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.permissions.OemAutostartHelper
import com.myniyam.app.ui.theme.NiyamBackground

@Composable
fun OemAutostartScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    val flow = OemAutostartHelper.flowFor(Build.MANUFACTURER)

    NiyamBackground {
        Scaffold(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(56.dp))
                PermissionIconCircle()
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.perm_oem_title),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(
                        when (flow) {
                            OemAutostartHelper.OemFlow.MIUI -> R.string.perm_oem_body_miui
                            OemAutostartHelper.OemFlow.COLOR_OS -> R.string.perm_oem_body_color_os
                            OemAutostartHelper.OemFlow.FUNTOUCH_OS -> R.string.perm_oem_body_funtouch_os
                            OemAutostartHelper.OemFlow.OXYGEN_OS -> R.string.perm_oem_body_oxygen_os
                            OemAutostartHelper.OemFlow.ONE_UI -> R.string.perm_oem_body_one_ui
                            OemAutostartHelper.OemFlow.GENERIC -> R.string.perm_oem_body_generic
                        }
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.weight(1f))
                PermissionDashes(stepIndex = 4, stepCount = 5)
                Spacer(Modifier.height(20.dp))
                if (flow == OemAutostartHelper.OemFlow.GENERIC) {
                    // Stock Android has no OEM autostart setting to open — a Grant
                    // button would silently no-op, so Done is the single primary CTA.
                    Button(
                        onClick = onDone,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(stringResource(R.string.done), style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    // Known OEM: Grant opens the real autostart setting (primary);
                    // Done lets the user proceed once they've handled it (secondary).
                    Button(
                        onClick = { OemAutostartHelper.openAutostartSettings(ctx) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(stringResource(R.string.grant), style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onDone,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(stringResource(R.string.done), style = MaterialTheme.typography.labelLarge)
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}
