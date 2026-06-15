package com.myniyam.app.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.myniyam.app.data.Deity
import com.myniyam.app.data.Intention
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.SourceCategory
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.onboarding.mantraGist
import com.myniyam.app.ui.theme.NiyamBackground
import com.myniyam.app.ui.theme.NiyamTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(onOpenDetail: (String) -> Unit) {
    val ctx = LocalContext.current
    MantraRepository.ensureLoaded(ctx)
    val all = MantraRepository.all()
    val snap = UserPrefs.snapshot()
    val state = Entitlements.state(snap.premiumActive, snap.trialStartEpochDay, java.time.LocalDate.now().toEpochDay())

    var selection by remember { mutableStateOf(LibraryFilters.Selection()) }
    val results = remember(selection, all) { LibraryFilters.apply(all, selection) }
    val activeCount = remember(selection) {
        listOf(selection.category, selection.length, selection.intention, selection.deity).count { it != null }
    }
    var showFilters by remember { mutableStateOf(false) }

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
                    stringResource(R.string.library_overline).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = NiyamTheme.colors.overlineWarm
                )
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.library_title), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(14.dp))

                // Single, tidy filter trigger (SP-P1): the four scrolling rows now
                // live behind a bottom sheet. Active dimensions shown as a count badge.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterTriggerPill(activeCount = activeCount, onClick = { showFilters = true })
                    Spacer(Modifier.weight(1f))
                    if (activeCount > 0) {
                        TextButton(onClick = { selection = LibraryFilters.Selection() }) {
                            Text(stringResource(R.string.filter_clear_all))
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                if (results.isEmpty()) {
                    Text(
                        stringResource(R.string.library_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f)) {
                        items(results, key = { it.id }) { mantra ->
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
                                        when {
                                            mantra.id == snap.currentMantraId -> MarkerChip(
                                                stringResource(R.string.library_current_marker),
                                                MaterialTheme.colorScheme.primary
                                            )
                                            mantra.id in snap.completedMantraIds -> MarkerChip(
                                                stringResource(R.string.library_completed_marker),
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                        if (!Entitlements.canUseMantra(state, mantra.id, snap.currentMantraId)) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier
                                                    .padding(start = 8.dp)
                                                    .size(16.dp)
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

    if (showFilters) {
        FilterSheet(
            selection = selection,
            resultCount = results.size,
            onChange = { selection = it },
            onClearAll = { selection = LibraryFilters.Selection() },
            onDismiss = { showFilters = false }
        )
    }
}

/** Pill that opens the filter sheet, with a count badge for active dimensions (SP-P1). */
@Composable
private fun FilterTriggerPill(activeCount: Int, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(999.dp),
                ambientColor = Color(0xFF7A3D12).copy(alpha = 0.10f),
                spotColor = Color(0xFF7A3D12).copy(alpha = 0.10f)
            )
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(999.dp))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(stringResource(R.string.filter_heading), style = MaterialTheme.typography.labelLarge)
        if (activeCount > 0) {
            Spacer(Modifier.size(8.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    activeCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    selection: LibraryFilters.Selection,
    resultCount: Int,
    onChange: (LibraryFilters.Selection) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.filter_heading),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                val anyActive = listOf(selection.category, selection.length, selection.intention, selection.deity)
                    .any { it != null }
                if (anyActive) {
                    TextButton(onClick = onClearAll) { Text(stringResource(R.string.filter_clear_all)) }
                }
            }
            Spacer(Modifier.height(8.dp))

            FilterSection(
                title = stringResource(R.string.filter_section_source),
                options = SourceCategory.entries.map { it to it.labelRes() },
                selected = selection.category,
                onSelect = { onChange(selection.copy(category = it)) }
            )
            FilterSection(
                title = stringResource(R.string.filter_section_length),
                options = LengthBucket.entries.map { it to it.labelRes() },
                selected = selection.length,
                onSelect = { onChange(selection.copy(length = it)) }
            )
            FilterSection(
                title = stringResource(R.string.filter_section_intention),
                options = Intention.entries.map { it to it.labelRes() },
                selected = selection.intention,
                onSelect = { onChange(selection.copy(intention = it)) }
            )
            FilterSection(
                title = stringResource(R.string.filter_section_deity),
                options = Deity.entries.map { it to it.labelRes() },
                selected = selection.deity,
                onSelect = { onChange(selection.copy(deity = it)) }
            )

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    stringResource(R.string.filter_show_count, resultCount),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun <T> FilterSection(
    title: String,
    options: List<Pair<T, Int>>,
    selected: T?,
    onSelect: (T?) -> Unit
) {
    Spacer(Modifier.height(12.dp))
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = NiyamTheme.colors.overlineWarm
    )
    Spacer(Modifier.height(6.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text(stringResource(R.string.library_filter_all)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NiyamTheme.colors.orangeTint,
                selectedLabelColor = NiyamTheme.colors.onTint
            )
        )
        options.forEach { (value, labelRes) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(if (selected == value) null else value) },
                label = { Text(stringResource(labelRes)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NiyamTheme.colors.orangeTint,
                    selectedLabelColor = NiyamTheme.colors.onTint
                )
            )
        }
    }
}

@Composable
internal fun MarkerChip(label: String, color: Color) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

internal fun SourceCategory.labelRes(): Int = when (this) {
    SourceCategory.VEDIC -> R.string.cat_vedic
    SourceCategory.UPANISHAD -> R.string.cat_upanishad
    SourceCategory.GITA -> R.string.cat_gita
    SourceCategory.STOTRA -> R.string.cat_stotra
}

internal fun LengthBucket.labelRes(): Int = when (this) {
    LengthBucket.UNDER_30S -> R.string.len_under30
    LengthBucket.S30_TO_60 -> R.string.len_30to60
    LengthBucket.OVER_60S -> R.string.len_over60
}

internal fun Intention.labelRes(): Int = when (this) {
    Intention.FOCUS -> R.string.intent_focus
    Intention.CALM -> R.string.intent_calm
    Intention.SADHANA -> R.string.intent_sadhana
    Intention.DHARMA -> R.string.intent_dharma
    Intention.DEVOTION -> R.string.intent_devotion
}

internal fun Deity.labelRes(): Int = when (this) {
    Deity.SHIVA -> R.string.deity_shiva
    Deity.VISHNU -> R.string.deity_vishnu
    Deity.DEVI -> R.string.deity_devi
    Deity.GANESHA -> R.string.deity_ganesha
    Deity.HANUMAN -> R.string.deity_hanuman
    Deity.KRISHNA -> R.string.deity_krishna
    Deity.RAMA -> R.string.deity_rama
    Deity.SARASWATI -> R.string.deity_saraswati
    Deity.LAKSHMI -> R.string.deity_lakshmi
    Deity.UNIVERSAL -> R.string.deity_universal
}
