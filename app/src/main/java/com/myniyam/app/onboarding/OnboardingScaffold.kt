package com.myniyam.app.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.ui.theme.NiyamBackground
import com.myniyam.app.ui.theme.NiyamTheme

/**
 * Shared frame for every onboarding step (spec §5 + SP-8 §2): step overline (warm
 * brown small caps), sans 700 title, a progress-dash row, a scrollable content
 * region on the sunrise gradient, and a pill Continue button pinned to the bottom.
 * Colours come only from MaterialTheme.colorScheme (Button fills with primary/orange).
 */
@Composable
fun OnboardingScaffold(
    step: Int,
    title: String,
    ctaEnabled: Boolean,
    onContinue: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    NiyamBackground {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.onb_step_fmt, step).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = NiyamTheme.colors.overlineWarm
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(14.dp))
                ProgressDashes(stepIndex = step - 1, stepCount = 4)
                Spacer(Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    content = content
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onContinue,
                    enabled = ctaEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = stringResource(R.string.onb_continue),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

/**
 * Progress-dash row (SP-8 spec §2). Dashes filled [PumpkinOrange] up to and
 * including [stepIndex] (0-based), the rest in the theme hairline.
 */
@Composable
fun ProgressDashes(stepIndex: Int, stepCount: Int) {
    Row {
        repeat(stepCount) { i ->
            Box(
                modifier = Modifier
                    .padding(end = 6.dp)
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

/**
 * A tappable selection card used across onboarding steps. SP-8 §2: floating
 * warm-white card, 24dp radius, soft warm ambient shadow; selected state shows a
 * 2dp orange border + warm orange fill (OrangeTint) + trailing check, with a
 * spring-scale on selection. An optional [leading] slot renders before the text
 * (e.g. an app icon); when null every existing call site is unchanged.
 * The Material3 onClick overload supplies the pressed ripple for free.
 */
@Composable
fun SelectableCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    supportingText: String? = null,
    trailingChip: String? = null,
    leading: (@Composable () -> Unit)? = null
) {
    val scale by animateFloatAsState(if (selected) 1f else 0.985f, label = "cardScale")
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) NiyamTheme.colors.orangeTint else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0xFF7A3D12).copy(alpha = 0.10f),
                spotColor = Color(0xFF7A3D12).copy(alpha = 0.10f)
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leading != null) {
                Box(modifier = Modifier.padding(end = 14.dp)) {
                    leading()
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (supportingText != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (trailingChip != null) {
                Text(
                    text = trailingChip,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(999.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }
}
