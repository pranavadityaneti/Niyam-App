package com.myniyam.app.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.ui.theme.OrangeTint

/**
 * Shared frame for every onboarding step (spec §5): step overline, serif title,
 * scrollable content region, and a pill Continue button pinned to the bottom.
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
    Scaffold { innerPadding ->
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
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

/**
 * A tappable selection card used across onboarding steps. Selected state is shown
 * with an orange border, a warm orange fill (OrangeTint), and a trailing check.
 * The Material3 onClick overload supplies the pressed ripple for free.
 */
@Composable
fun SelectableCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    supportingText: String? = null,
    trailingChip: String? = null
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
        },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) OrangeTint else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
