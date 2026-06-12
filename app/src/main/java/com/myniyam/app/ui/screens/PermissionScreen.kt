package com.myniyam.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.myniyam.app.R
import com.myniyam.app.ui.theme.NiyamBackground

/**
 * Reusable permission-grant screen (SP-8 §2): sunrise canvas, a floating
 * icon circle, sans 700 title, body, a five-dash progress row, and a bottom-
 * anchored filled CTA pill. The grant-detection logic is unchanged.
 */
@Composable
fun PermissionScreen(
    titleResId: Int,
    bodyResId: Int,
    isGranted: () -> Boolean,
    launchSettings: () -> Unit,
    onGranted: () -> Unit,
    stepIndex: Int = 0,
    stepCount: Int = 5,
    // Play prominent-disclosure variant (SP-16 §A): when set, a highlighted
    // disclosure card renders and the CTA becomes the affirmative consent.
    disclosureResId: Int? = null,
    ctaResId: Int = R.string.grant,
    onConsent: (() -> Unit)? = null
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

    NiyamBackground {
        Scaffold(containerColor = Color.Transparent) { padding ->
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
                    text = stringResource(titleResId),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(bodyResId),
                    style = MaterialTheme.typography.bodyLarge
                )
                if (disclosureResId != null) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(disclosureResId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = Color(0xFF7A3D12).copy(alpha = 0.10f),
                                spotColor = Color(0xFF7A3D12).copy(alpha = 0.10f)
                            )
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                PermissionDashes(stepIndex = stepIndex, stepCount = stepCount)
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { onConsent?.invoke(); launchSettings() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(stringResource(R.string.grant), style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

/** Floating warm-white circle holding the orange permission glyph (SP-8 §2). */
@Composable
internal fun PermissionIconCircle() {
    Box(
        modifier = Modifier
            .size(72.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = Color(0xFF7A3D12).copy(alpha = 0.10f),
                spotColor = Color(0xFF7A3D12).copy(alpha = 0.10f)
            )
            .background(MaterialTheme.colorScheme.surface, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(34.dp)
        )
    }
}

/** Five-dash permission progress row (SP-8 §2). */
@Composable
internal fun PermissionDashes(stepIndex: Int, stepCount: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(stepCount) { i ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(4.dp)
                    .width(if (i == stepIndex) 22.dp else 14.dp)
                    .background(
                        color = if (i <= stepIndex) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = RoundedCornerShape(999.dp)
                    )
            )
        }
    }
}
