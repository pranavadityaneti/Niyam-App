package com.myniyam.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.myniyam.app.R

/**
 * Reusable permission-grant screen.
 */
@Composable
fun PermissionScreen(
    titleResId: Int,
    bodyResId: Int,
    isGranted: () -> Boolean,
    launchSettings: () -> Unit,
    onGranted: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var grantedState by remember { mutableStateOf(isGranted()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                grantedState = isGranted()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(grantedState) {
        if (grantedState) onGranted()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(titleResId),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.padding(top = 16.dp))
            Text(
                text = stringResource(bodyResId),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.padding(top = 48.dp))
            Button(
                onClick = { launchSettings() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.grant))
            }
        }
    }
}
