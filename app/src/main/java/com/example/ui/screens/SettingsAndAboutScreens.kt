package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.preferences.AppLanguage
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferences
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.LocalNumixColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onThemeChange: (ThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onRealTimeChange: (Boolean) -> Unit,
    onGroupDigitsChange: (Boolean) -> Unit,
    onSoundEffectsChange: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onResetPreferences: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bn = preferences.language == AppLanguage.BANGLA
    var confirmClear by remember { mutableStateOf(false) }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(if (bn) "সব ইতিহাস মুছবেন?" else "Clear Conversion History?") },
            text = {
                Text(
                    if (bn) "সব সংরক্ষিত ইতিহাস স্থায়ীভাবে মুছে যাবে।"
                    else "All saved conversion history entries will be removed."
                )
            },
            confirmButton = {
                Button(onClick = {
                    confirmClear = false
                    onClearHistory()
                }) {
                    Text(if (bn) "মুছে ফেলুন" else "Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text(if (bn) "বাতিল" else "Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Appearance
        item {
            GlassCard(accentColor = MaterialTheme.colorScheme.primary) {
                SectionHeader(
                    title = if (bn) "থিম ও ভাষা" else "Appearance & Language",
                    subtitle = if (bn) "ডিভাইসে স্থায়ীভাবে সংরক্ষিত হয়" else "Saved locally via DataStore Preferences"
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (bn) "থিম মোড" else "Theme Mode",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = preferences.themeMode == ThemeMode.DARK,
                        onClick = { onThemeChange(ThemeMode.DARK) },
                        leadingIcon = { Icon(Icons.Rounded.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text(if (bn) "ডার্ক" else "Dark") },
                        modifier = Modifier.testTag("settings_theme_dark")
                    )
                    FilterChip(
                        selected = preferences.themeMode == ThemeMode.LIGHT,
                        onClick = { onThemeChange(ThemeMode.LIGHT) },
                        leadingIcon = { Icon(Icons.Rounded.LightMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text(if (bn) "লাইট" else "Light") },
                        modifier = Modifier.testTag("settings_theme_light")
                    )
                    FilterChip(
                        selected = preferences.themeMode == ThemeMode.SYSTEM,
                        onClick = { onThemeChange(ThemeMode.SYSTEM) },
                        leadingIcon = { Icon(Icons.Rounded.SettingsBrightness, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text(if (bn) "সিস্টেম" else "System") },
                        modifier = Modifier.testTag("settings_theme_system")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (bn) "ভাষা (Language)" else "Language",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = preferences.language == lang,
                            onClick = { onLanguageChange(lang) },
                            leadingIcon = { Icon(Icons.Rounded.Language, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text(lang.displayLabel) },
                            modifier = Modifier.testTag("settings_lang_${lang.code}")
                        )
                    }
                }
            }
        }

        // Formatting & Behavior
        item {
            GlassCard {
                SectionHeader(
                    title = if (bn) "কনভার্টার ও ফরম্যাটিং" else "Converter & Formatting"
                )
                Spacer(modifier = Modifier.height(10.dp))
                SettingSwitchRow(
                    title = if (bn) "রিয়েল-টাইম রূপান্তর" else "Real-time Conversion",
                    subtitle = if (bn) "টাইপ করার সাথে সাথে ফলাফল ও ব্যাখ্যা আপডেট হবে" else "Update results and steps instantly while typing",
                    checked = preferences.realTimeConversion,
                    onCheckedChange = onRealTimeChange,
                    testTag = "settings_realtime_switch"
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingSwitchRow(
                    title = if (bn) "ডিজিট গ্রুপিং ফরম্যাটিং" else "Digit Grouping Formatting",
                    subtitle = if (bn) "বাইনারি ৪-বিট, হেক্স ৪-অঙ্ক এবং ডেসিম্যালে কমা দেখাবে" else "Group 4-bit binary nibbles and comma-separate decimals",
                    checked = preferences.groupDigits,
                    onCheckedChange = onGroupDigitsChange,
                    testTag = "settings_group_digits_switch"
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingSwitchRow(
                    title = if (bn) "সাউন্ড এফেক্ট" else "Sound Effects",
                    subtitle = if (bn) "কীপ্যাড ও কুইজ ট্যাপে হালকা সাউন্ড ফিডব্যাক" else "Subtle audio feedback on keypad & quiz actions",
                    checked = preferences.soundEffects,
                    onCheckedChange = onSoundEffectsChange,
                    leadingIcon = Icons.Rounded.VolumeUp,
                    testTag = "settings_sound_switch"
                )
            }
        }

        // Data & Reset
        item {
            GlassCard {
                SectionHeader(
                    title = if (bn) "ডাটা ব্যবস্থাপনা" else "Data & Reset"
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { confirmClear = true },
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("settings_clear_history_btn")
                    ) {
                        Icon(Icons.Rounded.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (bn) "ইতিহাস মুছুন" else "Clear History")
                    }
                    OutlinedButton(
                        onClick = onResetPreferences,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("settings_reset_prefs_btn")
                    ) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (bn) "রিসেট সেটিংস" else "Reset Prefs")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
fun AboutScreen(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val bn = language == AppLanguage.BANGLA
    val ext = LocalNumixColors.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassCard(accentColor = ext.binaryColor) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_numix_logo),
                        contentDescription = "NumiX logo",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Column {
                        Text(
                            text = "NumiX",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (bn) "সংখ্যা পদ্ধতি কনভার্টার ও গণিত শিক্ষা প্ল্যাটফর্ম"
                            else "Number System Converter & Mathematics Learning Platform",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "v1.0 • Offline BigInteger Engine",
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.binaryColor
                        )
                    }
                }
            }
        }

        item {
            GlassCard {
                SectionHeader(
                    title = if (bn) "মূল বৈশিষ্ট্যসমূহ" else "Core Capabilities"
                )
                Spacer(modifier = Modifier.height(10.dp))
                val features = if (bn) listOf(
                    "নির্ধারিত সীমাহীন পূর্ণসংখ্যার (BigInteger) নিখুঁত রূপান্তর",
                    "১২টি রূপান্তর দিকের জন্য ইংরেজি ও বাংলায় ডাইনামিক ধাপে ধাপে ব্যাখ্যা",
                    "০–২৫৫ সার্চযোগ্য ও ফিল্টারযোগ্য সংখ্যা পদ্ধতি সারণি",
                    "৩টি স্তরের অ্যালগরিদমিক কুইজ ও তাৎক্ষণিক ব্যাখ্যা",
                    "অফলাইন রুম ডাটাবেস, প্রিয় তালিকা ও CSV এক্সপোর্ট"
                ) else listOf(
                    "Arbitrary-precision BigInteger conversion with zero overflow or precision loss",
                    "Dynamic step-by-step mathematical explanations in English & Bangla across all 12 pairs",
                    "Interactive 0–255 Reference Table with search, filters, column toggles, and pagination",
                    "Algorithmic Practice & Quiz Mode across Beginner, Intermediate, and Advanced tiers",
                    "100% local Room history, favorites, .txt explanation export, print, and CSV export"
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    features.forEach { feat ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircleOutline,
                                contentDescription = null,
                                tint = ext.successColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(feat, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
