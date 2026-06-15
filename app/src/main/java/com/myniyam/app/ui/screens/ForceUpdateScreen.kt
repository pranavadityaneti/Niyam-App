package com.myniyam.app.ui.screens

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myniyam.app.ui.theme.NiyamBackground

/**
 * Blocking "please update" screen (OTA force-update kill-switch). Shown when the
 * server's min_supported_version_code is above this build's version code. There
 * is no dismiss — the only action is to open the Play listing.
 */
@Composable
fun ForceUpdateScreen(message: String) {
    val ctx = LocalContext.current
    NiyamBackground {
        Scaffold(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground) { pad ->
            Column(
                Modifier.fillMaxSize().padding(pad).padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Time to update",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = {
                        val pkg = ctx.packageName
                        val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$pkg".toUri())
                        val fallback = Intent(
                            Intent.ACTION_VIEW,
                            "https://play.google.com/store/apps/details?id=$pkg".toUri()
                        )
                        try { ctx.startActivity(intent) } catch (e: Exception) { ctx.startActivity(fallback) }
                    },
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("Update now", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
