package com.myniyam.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.myniyam.app.R
import com.myniyam.app.permissions.PermissionChecker
import com.myniyam.app.service.AppLockForegroundService

@Composable
fun HomeScreen() {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var usageOk by remember { mutableStateOf(PermissionChecker.hasUsageStatsAccess(ctx)) }
    var overlayOk by remember { mutableStateOf(PermissionChecker.hasOverlayPermission(ctx)) }
    var accessibilityOk by remember { mutableStateOf(PermissionChecker.isAccessibilityServiceEnabled(ctx)) }
    var batteryOk by remember { mutableStateOf(PermissionChecker.isIgnoringBatteryOptimizations(ctx)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageOk = PermissionChecker.hasUsageStatsAccess(ctx)
                overlayOk = PermissionChecker.hasOverlayPermission(ctx)
                accessibilityOk = PermissionChecker.isAccessibilityServiceEnabled(ctx)
                batteryOk = PermissionChecker.isIgnoringBatteryOptimizations(ctx)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val allOk = usageOk && overlayOk && accessibilityOk && batteryOk

    LaunchedEffect(allOk) {
        if (allOk) {
            val intent = Intent(ctx, AppLockForegroundService::class.java).apply {
                action = AppLockForegroundService.ACTION_START
            }
            ctx.startForegroundService(intent)
        }
    }

    val bannerLabel = if (allOk)
        stringResource(R.string.home_protection_active)
    else
        stringResource(R.string.home_protection_at_risk)
    val bannerColor = if (allOk) Color(0xFF2E7D32) else Color(0xFFC62828)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                text = bannerLabel,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bannerColor)
                    .padding(16.dp)
            )

            Spacer(modifier = Modifier.padding(top = 24.dp))

            PermissionRow("Usage access", usageOk)
            PermissionRow("Display over other apps", overlayOk)
            PermissionRow("Accessibility service", accessibilityOk)
            PermissionRow("Ignore battery optimization", batteryOk)
        }
    }
}

@Composable
private fun PermissionRow(label: String, isOk: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = if (isOk) "✓" else "✗",
            color = if (isOk) Color(0xFF2E7D32) else Color(0xFFC62828),
            style = MaterialTheme.typography.titleLarge
        )
    }
}
