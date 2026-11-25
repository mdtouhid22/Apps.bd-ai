package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.preferences.AppLanguage
import com.example.domain.NumberSystem
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalNumixColors
import com.example.ui.util.PlatformActions

private enum class TableFilter {
    ALL, EVEN, ODD, POWERS_OF_2, PRINTABLE_ASCII
}

private data class ReferenceRow(
    val decValue: Int,
    val dec: String,
    val bin: String,
    val oct: String,
    val hex: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TableScreen(
    language: AppLanguage,
    onOpenInConverter: (String, NumberSystem, NumberSystem) -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bn = language == AppLanguage.BANGLA
    val ext = LocalNumixColors.current

    val fullTable = remember {
        (0..255).map { n ->
            ReferenceRow(
                decValue = n,
                dec = n.toString(10),
                bin = n.toString(2).padStart(8, '0'),
                oct = n.toString(8).padStart(3, '0'),
                hex = n.toString(16).uppercase().padStart(2, '0')
            )
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(TableFilter.ALL) }
    var sortAscending by remember { mutableStateOf(true) }
    var showDec by remember { mutableStateOf(true) }
    var showBin by remember { mutableStateOf(true) }
    var showOct by remember { mutableStateOf(true) }
    var showHex by remember { mutableStateOf(true) }
    var pageSize by remember { mutableIntStateOf(16) }
    var currentPage by remember { mutableIntStateOf(0) }

    val filteredRows = remember(searchQuery, activeFilter, sortAscending) {
        val q = searchQuery.trim().lowercase()
        val base = fullTable.filter { row ->
            val matchesFilter = when (activeFilter) {
                TableFilter.ALL -> true
                TableFilter.EVEN -> row.decValue % 2 == 0
                TableFilter.ODD -> row.decValue % 2 != 0
                TableFilter.POWERS_OF_2 -> row.decValue > 0 && (row.decValue and (row.decValue - 1)) == 0
                TableFilter.PRINTABLE_ASCII -> row.decValue in 32..126
            }
            val matchesSearch = q.isEmpty() ||
                row.dec.contains(q) ||
                row.bin.contains(q) ||
                row.decValue.toString(2).contains(q) ||
                row.oct.contains(q) ||
                row.hex.lowercase().contains(q)
            matchesFilter && matchesSearch
        }
        if (sortAscending) base else base.reversed()
    }

    val totalPages = ((filteredRows.size + pageSize - 1) / pageSize).coerceAtLeast(1)
    val safePage = currentPage.coerceIn(0, totalPages - 1)
    val pagedRows = filteredRows.drop(safePage * pageSize).take(pageSize)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassCard(accentColor = ext.octalColor) {
                SectionHeader(
                    title = if (bn) "সংখ্যা পদ্ধতি সারণি (০–২৫৫)" else "Number System Table (0–255)",
                    subtitle = if (bn) "যেকোনো মান ট্যাপ করে কপি করুন বা কনভার্টারে খুলুন"
                    else "Tap any cell to copy • Tap DEC to open in converter",
                    trailing = {
                        OutlinedButton(
                            onClick = { sortAscending = !sortAscending },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("table_sort_btn"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Rounded.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (sortAscending) "0→255" else "255→0")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        currentPage = 0
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("table_search_input"),
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                currentPage = 0
                            }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    label = {
                        Text(if (bn) "ডেসিম্যাল, বাইনারি, অক্টাল বা হেক্স খুঁজুন..." else "Search 0–255 in any base (e.g. 45, 2D, 101101)...")
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (bn) "ফিল্টার" else "Filter",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TableFilter.entries.forEach { f ->
                        val label = when (f) {
                            TableFilter.ALL -> if (bn) "সব (২৫৬)" else "All (256)"
                            TableFilter.EVEN -> if (bn) "জোড়" else "Even"
                            TableFilter.ODD -> if (bn) "বিজোড়" else "Odd"
                            TableFilter.POWERS_OF_2 -> if (bn) "২-এর ঘাত" else "Powers of 2"
                            TableFilter.PRINTABLE_ASCII -> "ASCII (32–126)"
                        }
                        FilterChip(
                            selected = activeFilter == f,
                            onClick = {
                                activeFilter = f
                                currentPage = 0
                            },
                            label = { Text(label) },
                            modifier = Modifier.testTag("table_filter_${f.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (bn) "কলাম প্রদর্শন ও প্রতি পৃষ্ঠায় সারি" else "Columns & Page Size",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = showDec,
                        onClick = { if (showBin || showOct || showHex) showDec = !showDec },
                        label = { Text("DEC") },
                        modifier = Modifier.testTag("col_toggle_dec")
                    )
                    FilterChip(
                        selected = showBin,
                        onClick = { if (showDec || showOct || showHex) showBin = !showBin },
                        label = { Text("BIN") },
                        modifier = Modifier.testTag("col_toggle_bin")
                    )
                    FilterChip(
                        selected = showOct,
                        onClick = { if (showDec || showBin || showHex) showOct = !showOct },
                        label = { Text("OCT") },
                        modifier = Modifier.testTag("col_toggle_oct")
                    )
                    FilterChip(
                        selected = showHex,
                        onClick = { if (showDec || showBin || showOct) showHex = !showHex },
                        label = { Text("HEX") },
                        modifier = Modifier.testTag("col_toggle_hex")
                    )
                    listOf(16, 32, 64).forEach { size ->
                        FilterChip(
                            selected = pageSize == size,
                            onClick = {
                                pageSize = size
                                currentPage = 0
                            },
                            label = { Text("$size/pg") },
                            modifier = Modifier.testTag("page_size_$size")
                        )
                    }
                }
            }
        }

        // Table Header Row
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showDec) HeaderCell("DEC (10)", ext.decimalColor, Modifier.weight(0.9f))
                    if (showBin) HeaderCell("BIN (2)", ext.binaryColor, Modifier.weight(1.7f))
                    if (showOct) HeaderCell("OCT (8)", ext.octalColor, Modifier.weight(0.9f))
                    if (showHex) HeaderCell("HEX (16)", ext.hexColor, Modifier.weight(0.9f))
                }
            }
        }

        if (pagedRows.isEmpty()) {
            item {
                GlassCard {
                    Text(
                        text = if (bn) "খোঁজার সাথে কোনো সংখ্যা মেলেনি।" else "No matching values found in 0–255.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(pagedRows, key = { it.decValue }) { row ->
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ext.glassBorder.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showDec) {
                            TableValueCell(
                                text = row.dec,
                                color = ext.decimalColor,
                                onClick = {
                                    onOpenInConverter(row.dec, NumberSystem.DECIMAL, NumberSystem.BINARY)
                                },
                                modifier = Modifier
                                    .weight(0.9f)
                                    .testTag("table_dec_${row.dec}")
                            )
                        }
                        if (showBin) {
                            TableValueCell(
                                text = row.bin,
                                color = ext.binaryColor,
                                onClick = {
                                    if (PlatformActions.copyToClipboard(context, "Binary", row.bin)) {
                                        onShowToast("${if (bn) "কপি হয়েছে" else "Copied"}: ${row.bin}₂")
                                    }
                                },
                                modifier = Modifier.weight(1.7f)
                            )
                        }
                        if (showOct) {
                            TableValueCell(
                                text = row.oct,
                                color = ext.octalColor,
                                onClick = {
                                    if (PlatformActions.copyToClipboard(context, "Octal", row.oct)) {
                                        onShowToast("${if (bn) "কপি হয়েছে" else "Copied"}: ${row.oct}₈")
                                    }
                                },
                                modifier = Modifier.weight(0.9f)
                            )
                        }
                        if (showHex) {
                            TableValueCell(
                                text = row.hex,
                                color = ext.hexColor,
                                onClick = {
                                    if (PlatformActions.copyToClipboard(context, "Hex", row.hex)) {
                                        onShowToast("${if (bn) "কপি হয়েছে" else "Copied"}: ${row.hex}₁₆")
                                    }
                                },
                                modifier = Modifier.weight(0.9f)
                            )
                        }
                    }
                }
            }
        }

        // Pagination Bar
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (safePage > 0) currentPage = safePage - 1 },
                        enabled = safePage > 0,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("table_prev_page")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Previous page", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (bn) "পূর্ব" else "Prev")
                    }
                    Text(
                        text = if (bn) "পৃষ্ঠা ${safePage + 1} / $totalPages (${filteredRows.size})"
                        else "Page ${safePage + 1} of $totalPages (${filteredRows.size} rows)",
                        style = MaterialTheme.typography.labelMedium
                    )
                    OutlinedButton(
                        onClick = { if (safePage < totalPages - 1) currentPage = safePage + 1 },
                        enabled = safePage < totalPages - 1,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("table_next_page")
                    ) {
                        Text(if (bn) "পরবর্তী" else "Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Next page", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
private fun TableValueCell(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.SemiBold
        ),
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 2.dp)
    )
}
