package com.myniyam.app.onboarding

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.myniyam.app.ui.theme.NiyamTheme

/** Loads the installed app's launcher icon as an ImageBitmap, or null if not installed. */
private fun loadIconBitmap(context: Context, pkg: String): ImageBitmap? = try {
    val drawable = context.packageManager.getApplicationIcon(pkg)
    drawable.toBitmap(width = 96, height = 96).asImageBitmap()
} catch (e: PackageManager.NameNotFoundException) {
    null
} catch (e: Exception) {
    null
}

/**
 * Leading app glyph for blocked-app rows (SP-8 spec §2). Shows the real launcher
 * icon when the app is installed; otherwise an initial-letter circle in ChipFill.
 * No bundled brand assets — looks native, avoids trademark logos.
 */
@Composable
fun AppIcon(pkg: String, name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember(pkg) { loadIconBitmap(context, pkg) }
    Box(
        modifier = modifier
            .size(40.dp)
            .background(NiyamTheme.colors.chipFill, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = name,
                modifier = Modifier.size(40.dp)
            )
        } else {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
