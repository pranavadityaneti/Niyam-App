package com.myniyam.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.data.CurrentSadhana
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.ui.theme.NiyamBackground
import com.myniyam.app.ui.theme.NiyamTheme

/**
 * Shown when a sadhana reaches its completion threshold (spec §4). Celebrates the
 * just-completed mantra and offers two paths: choose a new sadhana, or keep going
 * with the current mantra. SP-8 §2: big 700 numeral headline + a one-shot petal
 * scatter (gentle fall/fade in on entry, then still). NO Om mark.
 */
@Composable
fun CelebrationScreen(
    onChooseNext: () -> Unit,
    onKeepCurrent: () -> Unit
) {
    val mantra = MantraRepository.displayMantra(CurrentSadhana.MANTRA_ID)

    NiyamBackground {
        Scaffold(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                // One-shot petal scatter, drawn behind the content.
                PetalScatter(modifier = Modifier.fillMaxSize())

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.celebration_overline).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = NiyamTheme.colors.overlineWarm
                    )

                    Spacer(Modifier.height(12.dp))

                    // Big 700 numeral hero (the completed day count).
                    Text(
                        text = mantra.completionThresholdDays.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = stringResource(
                            R.string.celebration_title_fmt,
                            mantra.completionThresholdDays,
                            mantra.canonicalName
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.celebration_body),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(40.dp))

                    Button(
                        onClick = onChooseNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.celebration_next),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onKeepCurrent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.celebration_keep),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

/** A single petal's deterministic layout: horizontal position, fall distance, radius, color. */
private data class Petal(
    val xFraction: Float,
    val startYFraction: Float,
    val fallFraction: Float,
    val radiusDp: Float,
    val color: Color
)

// Fixed, deterministic petal set (10 dots) — stable across recompositions, no per-frame Random.
private val Amber = Color(0xFFFFB066)
private val Orange = Color(0xFFFF6400)
private val PETALS = listOf(
    Petal(0.10f, 0.06f, 0.14f, 6f, Orange),
    Petal(0.22f, 0.02f, 0.10f, 5f, Amber),
    Petal(0.34f, 0.10f, 0.16f, 7f, Amber),
    Petal(0.46f, 0.03f, 0.12f, 5f, Orange),
    Petal(0.58f, 0.08f, 0.18f, 6f, Amber),
    Petal(0.68f, 0.01f, 0.11f, 4f, Orange),
    Petal(0.78f, 0.07f, 0.15f, 7f, Amber),
    Petal(0.88f, 0.04f, 0.13f, 5f, Orange),
    Petal(0.16f, 0.12f, 0.09f, 4f, Amber),
    Petal(0.92f, 0.10f, 0.17f, 6f, Orange)
)

/**
 * One-shot petal scatter (SP-8 spec §2). A single [Animatable] 0→1 launched once
 * drives every petal's fall + fade-in; when it completes the petals hold still at
 * their settled positions — nothing loops.
 */
@Composable
private fun PetalScatter(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
    }
    Canvas(modifier = modifier) {
        val t = progress.value
        PETALS.forEach { petal ->
            val cx = petal.xFraction * size.width
            val cy = (petal.startYFraction + petal.fallFraction * t) * size.height
            drawCircle(
                color = petal.color.copy(alpha = t),
                radius = petal.radiusDp.dp.toPx(),
                center = Offset(cx, cy)
            )
        }
    }
}
