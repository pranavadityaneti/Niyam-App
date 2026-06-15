package com.myniyam.app.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.billing.AdBanner
import com.myniyam.app.billing.Entitlements
import com.myniyam.app.billing.PremiumState
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.onboarding.mantraGist
import com.myniyam.app.ui.theme.NiyamBackground
import com.myniyam.app.ui.theme.NiyamTheme

/** Favourites tab (SP-P1): the mantras the user hearted, in catalog order. */
@Composable
fun FavouritesScreen(onOpenDetail: (String) -> Unit) {
    val ctx = LocalContext.current
    MantraRepository.ensureLoaded(ctx)
    val snap = UserPrefs.snapshot()
    val state = Entitlements.state(snap.premiumActive, snap.trialStartEpochDay, java.time.LocalDate.now().toEpochDay())
    val favourites = MantraRepository.all().filter { it.id in snap.favouriteMantraIds }

    NiyamBackground {
        Scaffold(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 96.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(R.string.favourites_overline).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = NiyamTheme.colors.overlineWarm
                )
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.favourites_title), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(14.dp))

                if (favourites.isEmpty()) {
                    Text(
                        stringResource(R.string.favourites_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(favourites, key = { it.id }) { mantra ->
                            Card(
                                onClick = { onOpenDetail(mantra.id) },
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(24.dp),
                                        ambientColor = Color(0xFF7A3D12).copy(alpha = 0.10f),
                                        spotColor = Color(0xFF7A3D12).copy(alpha = 0.10f)
                                    )
                            ) {
                                Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            mantra.name.forScript(snap.displayLanguage.script),
                                            style = MaterialTheme.typography.titleLarge,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (!Entitlements.canUseMantra(state, mantra.id, snap.currentMantraId)) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(start = 8.dp).size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        mantraGist(mantra.meaning.forLang(snap.displayLanguage.meaningLang)),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "${stringResource(mantra.sourceCategory.labelRes())} · ~${mantra.estimatedReadSeconds}s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                if (state == PremiumState.FREE) {
                    AdBanner()
                }
            }
        }
    }
}
