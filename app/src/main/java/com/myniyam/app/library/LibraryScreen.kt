package com.myniyam.app.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.myniyam.app.R
import com.myniyam.app.data.Deity
import com.myniyam.app.data.Intention
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.SourceCategory
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.onboarding.mantraGist
import com.myniyam.app.ui.theme.BottleGreen
import com.myniyam.app.ui.theme.OrangeTint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(onOpenDetail: (String) -> Unit) {
    val ctx = LocalContext.current
    MantraRepository.ensureLoaded(ctx)
    val all = MantraRepository.all()
    val snap = UserPrefs.snapshot()

    var selection by remember { mutableStateOf(LibraryFilters.Selection()) }
    val results = remember(selection, all) { LibraryFilters.apply(all, selection) }

    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.library_overline).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.library_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(14.dp))

            FilterRow(
                options = SourceCategory.entries.map { it to it.label() },
                selected = selection.category,
                onSelect = { selection = selection.copy(category = it) }
            )
            FilterRow(
                options = LengthBucket.entries.map { it to it.label() },
                selected = selection.length,
                onSelect = { selection = selection.copy(length = it) }
            )
            FilterRow(
                options = Intention.entries.map { it to it.label() },
                selected = selection.intention,
                onSelect = { selection = selection.copy(intention = it) }
            )
            FilterRow(
                options = Deity.entries.map { it to it.label() },
                selected = selection.deity,
                onSelect = { selection = selection.copy(deity = it) }
            )

            Spacer(Modifier.height(8.dp))

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
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        mantra.canonicalName,
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
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    mantraGist(mantra.meaning.en),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "${mantra.sourceCategory.label()} · ~${mantra.estimatedReadSeconds}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FilterRow(options: List<Pair<T, String>>, selected: T?, onSelect: (T?) -> Unit) {
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text(stringResource(R.string.library_filter_all)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = OrangeTint,
                selectedLabelColor = BottleGreen
            )
        )
        options.forEach { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(if (selected == value) null else value) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OrangeTint,
                    selectedLabelColor = BottleGreen
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

internal fun SourceCategory.label(): String = when (this) {
    SourceCategory.VEDIC -> "Vedic"
    SourceCategory.UPANISHAD -> "Upanishads"
    SourceCategory.GITA -> "Bhagavad Gita"
    SourceCategory.STOTRA -> "Stotras"
}

internal fun LengthBucket.label(): String = when (this) {
    LengthBucket.UNDER_30S -> "Under 30s"
    LengthBucket.S30_TO_60 -> "30–60s"
    LengthBucket.OVER_60S -> "Over 1 min"
}

internal fun Intention.label(): String = when (this) {
    Intention.FOCUS -> "Focus"
    Intention.CALM -> "Calm"
    Intention.SADHANA -> "Sadhana"
    Intention.DHARMA -> "Dharma"
    Intention.DEVOTION -> "Devotion"
}

internal fun Deity.label(): String = when (this) {
    Deity.SHIVA -> "Shiva"
    Deity.VISHNU -> "Vishnu"
    Deity.DEVI -> "Devi"
    Deity.GANESHA -> "Ganesha"
    Deity.HANUMAN -> "Hanuman"
    Deity.KRISHNA -> "Krishna"
    Deity.RAMA -> "Rama"
    Deity.SARASWATI -> "Saraswati"
    Deity.LAKSHMI -> "Lakshmi"
    Deity.UNIVERSAL -> "Universal"
}
