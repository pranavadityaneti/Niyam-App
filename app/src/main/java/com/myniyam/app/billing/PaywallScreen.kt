package com.myniyam.app.billing

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myniyam.app.BuildConfig
import com.myniyam.app.R
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.ui.theme.NiyamBackground
import com.myniyam.app.ui.theme.NiyamTheme
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Premium paywall v2 (SP-12 spec §1, founder-approved mockup): trial timeline as the
 * hero content while in TRIAL, expanded three-plan view behind "More plans" (and as
 * the default in FREE state, where the timeline's "trial is on" line would be false).
 * Sunrise Sans system throughout; static — no animated elements.
 */
@Composable
fun PaywallScreen(onUnlocked: () -> Unit, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val snap = UserPrefs.snapshot()
    val today = LocalDate.now().toEpochDay()
    val state = Entitlements.state(snap.premiumActive, snap.trialStartEpochDay, today)

    var expanded by remember { mutableStateOf(state != PremiumState.TRIAL) }
    var selectedPlan by remember { mutableStateOf(Plan.YEARLY) }

    NiyamBackground {
        Scaffold(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(24.dp))

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

                // Middle scrolls; trust pill + CTA stay pinned (protected bottom zone).
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (!expanded) {
                        TimelineRow(
                            icon = Icons.Default.Done,
                            title = stringResource(R.string.paywall_timeline_today_title),
                            body = stringResource(R.string.paywall_timeline_today_body),
                            connector = true
                        )
                        TimelineRow(
                            icon = Icons.Default.Notifications,
                            title = stringResource(R.string.paywall_timeline_day6_title),
                            body = stringResource(R.string.paywall_timeline_day6_body),
                            connector = true
                        )
                        TimelineRow(
                            icon = Icons.Default.CheckCircle,
                            title = stringResource(R.string.paywall_timeline_day7_title),
                            body = stringResource(R.string.paywall_timeline_day7_body),
                            connector = false
                        )

                        Spacer(Modifier.height(16.dp))

                        PlanCard(
                            title = stringResource(R.string.paywall_plan_annual_fmt, Plan.YEARLY.priceInr),
                            subline = stringResource(
                                R.string.paywall_plan_annual_subline_fmt, Plan.YEARLY.priceInr / 12
                            ),
                            badge = stringResource(R.string.paywall_best_value),
                            selected = true,
                            onClick = { selectedPlan = Plan.YEARLY }
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.paywall_hero),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(16.dp))
                        PlanCard(
                            title = stringResource(R.string.paywall_plan_annual_fmt, Plan.YEARLY.priceInr),
                            subline = stringResource(
                                R.string.paywall_plan_annual_subline_fmt, Plan.YEARLY.priceInr / 12
                            ),
                            badge = stringResource(R.string.paywall_best_value),
                            selected = selectedPlan == Plan.YEARLY,
                            onClick = { selectedPlan = Plan.YEARLY }
                        )
                        Spacer(Modifier.height(10.dp))
                        PlanCard(
                            title = stringResource(R.string.paywall_plan_monthly_fmt, Plan.MONTHLY.priceInr),
                            subline = stringResource(R.string.paywall_plan_monthly_subline),
                            badge = null,
                            selected = selectedPlan == Plan.MONTHLY,
                            onClick = { selectedPlan = Plan.MONTHLY }
                        )
                        Spacer(Modifier.height(10.dp))
                        PlanCard(
                            title = stringResource(R.string.paywall_plan_weekly_fmt, Plan.WEEKLY.priceInr),
                            subline = stringResource(R.string.paywall_plan_weekly_subline),
                            badge = null,
                            selected = selectedPlan == Plan.WEEKLY,
                            onClick = { selectedPlan = Plan.WEEKLY }
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.paywall_restore),
                            style = MaterialTheme.typography.labelMedium,
                            color = NiyamTheme.colors.overlineWarm,
                            modifier = Modifier
                                .clickable {
                                    scope.launch {
                                        if (Billing.gateway.restorePurchases(ctx)) {
                                            onUnlocked()
                                        } else {
                                            Toast.makeText(
                                                ctx,
                                                ctx.getString(R.string.paywall_restore_none),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                                .padding(vertical = 8.dp)
                        )
                        Spacer(Modifier.weight(1f))
                        // FREE state has no collapsed timeline view to return to.
                        if (state == PremiumState.TRIAL) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { expanded = !expanded; selectedPlan = Plan.YEARLY }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        if (expanded) R.string.paywall_fewer_plans
                                        else R.string.paywall_more_plans
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NiyamTheme.colors.overlineWarm
                                )
                                Icon(
                                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                                        else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = NiyamTheme.colors.overlineWarm,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = com.myniyam.app.backend.RemoteConfig.paywallTrust()
                            ?: stringResource(
                                if (BuildConfig.DEBUG) R.string.paywall_trust_sandbox else R.string.paywall_trust
                            ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        scope.launch {
                            if (Billing.gateway.purchase(ctx, selectedPlan)) onUnlocked()
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

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun TimelineRow(icon: ImageVector, title: String, body: String, connector: Boolean) {
    Row {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        ambientColor = Color(0xFF7A3D12).copy(alpha = 0.10f),
                        spotColor = Color(0xFF7A3D12).copy(alpha = 0.10f)
                    )
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            if (connector) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(26.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp, bottom = if (connector) 10.dp else 0.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    subline: String,
    badge: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFF7A3D12).copy(alpha = 0.10f),
                spotColor = Color(0xFF7A3D12).copy(alpha = 0.10f)
            )
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .then(
                if (selected) Modifier.border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(20.dp)
                ) else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (badge != null) {
                Text(
                    text = badge.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = NiyamTheme.colors.overlineWarm,
                    modifier = Modifier
                        .background(NiyamTheme.colors.orangeTint, RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                Spacer(Modifier.height(6.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
