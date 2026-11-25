package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ConversionHistoryEntity
import com.example.data.preferences.AppLanguage
import com.example.domain.NumberSystem
import com.example.localization.AppStrings
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalNumixColors
import com.example.ui.util.PlatformActions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    historyItems: List<ConversionHistoryEntity>,
    language: AppLanguage,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onReuse: (ConversionHistoryEntity) -> Unit,
    onToggleFavorite: (ConversionHistoryEntity) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onClearAll: () -> Unit,
    buildCsv: () -> String,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bn = language == AppLanguage.BANGLA
    val ext = LocalNumixColors.current
    var favoritesOnly by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(buildCsv().toByteArray(Charsets.UTF_8))
                }
                onShowToast(if (bn) "CSV ফাইল সংরক্ষিত হয়েছে" else "History exported as CSV")
            } catch (_: Exception) {
                onShowToast(if (bn) "CSV সংরক্ষণ ব্যর্থ হয়েছে" else "Failed to export CSV")
            }
        }
    }

    val displayed = remember(historyItems, favoritesOnly) {
        if (favoritesOnly) historyItems.filter { it.isFavorite } else historyItems
    }
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault()) }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(if (bn) "সব ইতিহাস মুছবেন?" else "Clear All History?") },
            text = {
                Text(
                    if (bn) "সংরক্ষিত সব রূপান্তর এবং প্রিয় তালিকা স্থায়ীভাবে মুছে যাবে।"
                    else "This will permanently delete all saved conversions and favorites."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        onClearAll()
                    },
                    modifier = Modifier.testTag("confirm_clear_history_btn")
                ) {
                    Text(if (bn) "মুছে ফেলুন" else "Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(if (bn) "বাতিল" else "Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassCard(accentColor = MaterialTheme.colorScheme.primary) {
                SectionHeader(
                    title = if (bn) "রূপান্তর ইতিহাস" else "Conversion History",
                    subtitle = if (bn) "সংরক্ষিত মানসমূহ, প্রিয় তালিকা এবং CSV এক্সপোর্ট"
                    else "Reuse, favorite, search, or export your saved conversions"
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input"),
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    label = { Text(if (bn) "ইতিহাসে খুঁজুন..." else "Search saved numbers...") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !favoritesOnly,
                        onClick = { favoritesOnly = false },
                        label = { Text(if (bn) "সব (${historyItems.size})" else "All (${historyItems.size})") },
                        modifier = Modifier.testTag("history_filter_all")
                    )
                    FilterChip(
                        selected = favoritesOnly,
                        onClick = { favoritesOnly = true },
                        label = { Text(if (bn) "প্রিয়" else "Favorites") },
                        modifier = Modifier.testTag("history_filter_favorites")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { csvLauncher.launch("NumiX_History.csv") },
                        enabled = historyItems.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("export_csv_btn")
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (bn) "CSV ডাউনলোড" else "Export CSV")
                    }
                    OutlinedButton(
                        onClick = {
                            PlatformActions.shareText(context, "NumiX History CSV", buildCsv())
                        },
                        enabled = historyItems.isNotEmpty(),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("share_csv_btn")
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share CSV", modifier = Modifier.size(16.dp))
                    }
                    OutlinedButton(
                        onClick = { showClearConfirm = true },
                        enabled = historyItems.isNotEmpty(),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("clear_history_btn")
                    ) {
                        Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear all history", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        if (displayed.isEmpty()) {
            item {
                GlassCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = if (favoritesOnly) {
                                if (bn) "এখনো কোনো প্রিয় রূপান্তর নেই।" else "No favorite conversions yet."
                            } else {
                                if (bn) "ইতিহাস খালি। কনভার্টারে গিয়ে রূপান্তর সংরক্ষণ করুন।" else "History is empty. Save a conversion in Converter to see it here."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(displayed, key = { it.id }) { item ->
                val srcSys = NumberSystem.fromBase(item.sourceBase)
                val tgtSys = NumberSystem.fromBase(item.targetBase)
                GlassCard(
                    modifier = Modifier.testTag("history_card_${item.id}"),
                    accentColor = if (item.isFavorite) ext.warningColor else null
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${item.originalNumber}${srcSys.subscript}  →  ${item.convertedNumber}${tgtSys.subscript}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${AppStrings.systemName(srcSys, language)} → ${AppStrings.systemName(tgtSys, language)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = dateFormatter.format(Date(item.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row {
                            IconButton(
                                onClick = { onToggleFavorite(item) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("fav_toggle_${item.id}")
                            ) {
                                Icon(
                                    imageVector = if (item.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                    contentDescription = "Toggle favorite",
                                    tint = if (item.isFavorite) ext.warningColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { onReuse(item) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("reuse_history_${item.id}")
                            ) {
                                Icon(Icons.Rounded.Replay, contentDescription = "Reuse in converter")
                            }
                            IconButton(
                                onClick = { onDeleteItem(item.id) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("delete_history_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Delete history entry",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
