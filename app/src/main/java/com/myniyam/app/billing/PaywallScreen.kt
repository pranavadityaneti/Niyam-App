package com.myniyam.app.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.onboarding.SelectableCard
import com.myniyam.app.ui.theme.NiyamBackground
import com.myniyam.app.ui.theme.NiyamTheme
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Premium paywall (spec §3.4) in the locked SP-8 "Sunrise Sans" system: warm overline,
 * 700 hero, floating benefits card, three plan cards (yearly pre-selected), filled-orange
 * pill CTA driving a sandbox purchase. Static — no animated elements.
 */
@Composable
fun PaywallScreen(onUnlocked: () -> Unit, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val snap = UserPrefs.snapshot()
    val today = LocalDate.now().toEpochDay()
    val state = Entitlements.state(snap.premiumActive, snap.trialStartEpochDay, today)
    val daysLeft = Entitlements.trialDaysLeft(snap.trialStartEpochDay, today)

    var selectedPlan by remember { mutableStateOf(Plan.YEARLY) }

    NiyamBackground {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(24.dp))

                // 1. Overline + close
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.paywall_overline).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = NiyamTheme.colors.overlineWarm,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.paywall_close_desc),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 2. Hero
                Text(
                    text = stringResource(R.string.paywall_hero),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 3. Trial caption (only while in trial)
                if (state == PremiumState.TRIAL) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = pluralStringResource(R.plurals.paywall_trial_left, daysLeft, daysLeft),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Middle section scrolls; CTA stays pinned below (protected bottom zone).
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 4. Benefits card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = Color(0xFF7A3D12).copy(alpha = 0.10f),
                                spotColor = Color(0xFF7A3D12).copy(alpha = 0.10f)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(20.dp)
                    ) {
                        BenefitRow(stringResource(R.string.paywall_benefit_mantras))
                        Spacer(Modifier.height(14.dp))
                        BenefitRow(stringResource(R.string.paywall_benefit_languages))
                        Spacer(Modifier.height(14.dp))
                        BenefitRow(stringResource(R.string.paywall_benefit_switching))
                        Spacer(Modifier.height(14.dp))
                        BenefitRow(stringResource(R.string.paywall_benefit_no_ads))
                    }

                    Spacer(Modifier.height(20.dp))

                    // 5. Plan picker
                    Column {
                        Plan.entries.forEach { plan ->
                            val period = stringResource(
                                when (plan) {
                                    Plan.WEEKLY -> R.string.paywall_period_week
                                    Plan.MONTHLY -> R.string.paywall_period_month
                                    Plan.YEARLY -> R.string.paywall_period_year
                                }
                            )
                            SelectableCard(
                                text = "₹${plan.priceInr} / $period",
                                supportingText = if (plan == Plan.YEARLY) {
                                    stringResource(R.string.paywall_best_value)
                                } else {
                                    null
                                },
                                selected = selectedPlan == plan,
                                onClick = { selectedPlan = plan }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 6. CTA
                Button(
                    onClick = {
                        scope.launch {
                            if (SandboxBillingGateway.purchase(ctx, selectedPlan)) onUnlocked()
                        }
                    },
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = stringResource(R.string.paywall_cta),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(Modifier.height(12.dp))

                // 7. Footnote
                Text(
                    text = stringResource(R.string.paywall_footnote),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun BenefitRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
