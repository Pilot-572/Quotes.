package xyz.crt572.quotes.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.crt572.quotes.data.FontFamilyChoice
import xyz.crt572.quotes.data.Playlist
import xyz.crt572.quotes.data.Quote
import xyz.crt572.quotes.data.QuoteSize
import xyz.crt572.quotes.data.TextAlignment

// Add + Edit share this sheet: initial == null means adding.
// v2 trap: no nested scrollables in here — this Column owns the only scroll.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuoteSheet(
    initial: Quote?,
    initialPlaylistIds: Set<Int>,
    playlists: List<Playlist>,
    onSave: (Quote, Set<Int>) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initial?.text ?: "") }
    var author by remember { mutableStateOf(initial?.author ?: "") }
    var source by remember { mutableStateOf(initial?.source ?: "") }
    var size by remember { mutableStateOf(initial?.sizeOverride) }
    var alignment by remember { mutableStateOf(initial?.alignmentOverride) }
    var bold by remember { mutableStateOf(initial?.boldOverride == true) }
    var italic by remember { mutableStateOf(initial?.italicOverride == true) }
    var font by remember { mutableStateOf(initial?.fontFamilyOverride) }
    var selectedPlaylists by remember { mutableStateOf(initialPlaylistIds) }
    var styleExpanded by remember { mutableStateOf(false) }

    val hasOverrides = size != null || alignment != null || bold || italic || font != null

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Quote") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Author (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = source,
                onValueChange = { source = it },
                label = { Text("Source (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { styleExpanded = !styleExpanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (styleExpanded) Icons.Filled.KeyboardArrowDown
                    else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                )
                Text(
                    text = "Style (${if (hasOverrides) "custom" else "default"})",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                if (styleExpanded && hasOverrides) {
                    TextButton(onClick = {
                        size = null; alignment = null; bold = false; italic = false; font = null
                    }) { Text("Reset") }
                }
            }

            if (styleExpanded) {
                ChipRowLabel("Size")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // "Auto" not "Default": null size also enables widget auto-fit
                    FilterChip(selected = size == null, onClick = { size = null }, label = { Text("Auto") })
                    QuoteSize.entries.forEach { s ->
                        FilterChip(selected = size == s, onClick = { size = s }, label = { Text(s.name) })
                    }
                }

                ChipRowLabel("Alignment")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = alignment == null, onClick = { alignment = null }, label = { Text("Default") })
                    FilterChip(selected = alignment == TextAlignment.Start, onClick = { alignment = TextAlignment.Start }, label = { Text("Left") })
                    FilterChip(selected = alignment == TextAlignment.Center, onClick = { alignment = TextAlignment.Center }, label = { Text("Center") })
                    FilterChip(selected = alignment == TextAlignment.End, onClick = { alignment = TextAlignment.End }, label = { Text("Right") })
                }

                ChipRowLabel("Style")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // two-state by design: selected = override true, unselected = inherit
                    FilterChip(selected = bold, onClick = { bold = !bold }, label = { Text("Bold") })
                    FilterChip(selected = italic, onClick = { italic = !italic }, label = { Text("Italic") })
                }

                ChipRowLabel("Font")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = font == null, onClick = { font = null }, label = { Text("Default") })
                    FilterChip(selected = font == FontFamilyChoice.Serif, onClick = { font = FontFamilyChoice.Serif }, label = { Text("Serif") })
                    FilterChip(selected = font == FontFamilyChoice.SansSerif, onClick = { font = FontFamilyChoice.SansSerif }, label = { Text("Sans") })
                    FilterChip(selected = font == FontFamilyChoice.Monospace, onClick = { font = FontFamilyChoice.Monospace }, label = { Text("Mono") })
                }
            }

            if (playlists.isNotEmpty()) {
                ChipRowLabel("Playlists")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    playlists.forEach { playlist ->
                        val selected = playlist.id in selectedPlaylists
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedPlaylists =
                                    if (selected) selectedPlaylists - playlist.id
                                    else selectedPlaylists + playlist.id
                            },
                            label = { Text(playlist.name) },
                        )
                    }
                }
            }

            Button(
                onClick = {
                    onSave(
                        (initial ?: Quote(text = "")).copy(
                            text = text.trim(),
                            author = author.trim().ifBlank { null },
                            source = source.trim().ifBlank { null },
                            sizeOverride = size,
                            alignmentOverride = alignment,
                            boldOverride = if (bold) true else null,
                            italicOverride = if (italic) true else null,
                            fontFamilyOverride = font,
                        ),
                        selectedPlaylists,
                    )
                },
                enabled = text.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save") }
        }
    }
}

@Composable
private fun ChipRowLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
