package com.myniyam.app.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.permissions.OemAutostartHelper

@Composable
fun OemAutostartScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    val flow = OemAutostartHelper.flowFor(Build.MANUFACTURER)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.perm_oem_title),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.padding(top = 16.dp))
            Text(
                text = "Your phone is ${Build.MANUFACTURER} (${flow.name}). " +
                    stringResource(R.string.perm_oem_body),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.padding(top = 48.dp))
            Button(
                onClick = { OemAutostartHelper.openAutostartSettings(ctx) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.grant))
            }
            Spacer(modifier = Modifier.padding(top = 8.dp))
            OutlinedButton(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.done))
            }
        }
    }
}
