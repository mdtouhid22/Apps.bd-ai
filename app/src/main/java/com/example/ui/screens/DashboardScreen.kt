package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ConversionHistoryEntity
import com.example.data.preferences.AppLanguage
import com.example.data.preferences.UserPreferences
import com.example.domain.NumberSystem
import com.example.domain.ValidationResult
import com.example.localization.AppStrings
import com.example.localization.NavDestination
import com.example.ui.ConverterUiState
import com.example.ui.components.BaseBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.SystemSelectorRow
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalNumixColors

private data class QuickPreset(
    val labelEn: String,
    val labelBn: String,
    val input: String,
    val source: NumberSystem,
    val target: NumberSystem
)

private val dashboardPresets = listOf(
    QuickPreset("45₁₀ → Binary", "45₁₀ → বাইনারি", "45", NumberSystem.DECIMAL, NumberSystem.BINARY),
    QuickPreset("101101₂ → Decimal", "101101₂ → ডেসিম্যাল", "101101", NumberSystem.BINARY, NumberSystem.DECIMAL),
    QuickPreset("2D₁₆ → Decimal", "2D₁₆ → ডেসিম্যাল", "2D", NumberSystem.HEXADECIMAL, NumberSystem.DECIMAL),
    QuickPreset("55₈ → Hex", "55₈ → হেক্স", "55", NumberSystem.OCTAL, NumberSystem.HEXADECIMAL),
    QuickPreset("255₁₀ → All Bases", "255₁₀ → সব পদ্ধতি", "255", NumberSystem.DECIMAL, NumberSystem.HEXADECIMAL)
)

@Composable
fun DashboardScreen(
    converterState: ConverterUiState,
    preferences: UserPreferences,
    recentHistory: List<ConversionHistoryEntity>,
    onInputChange: (String) -> Unit,
    onSelectSource: (NumberSystem) -> Unit,
    onSelectTarget: (NumberSystem) -> Unit,
    onSwap: () -> Unit,
    onConvertAndOpen: () -> Unit,
    onApplyPreset: (String, NumberSystem, NumberSystem) -> Unit,
    onOpenLesson: (NumberSystem) -> Unit,
    onReuseHistory: (ConversionHistoryEntity) -> Unit,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = preferences.language
    val bn = lang == AppLanguage.BANGLA
    val ext = LocalNumixColors.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_numix),
                    contentDescription = "NumiX mathematical matrix banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(24.dp))
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF070B14).copy(alpha = 0.92f),
                                    Color(0xFF0B1325).copy(alpha = 0.78f),
                                    Color(0xFF0B1325).copy(alpha = 0.55f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Surface(
                        color = ext.binaryColor.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = ext.binaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (bn) "নির্ভুল BigInteger ইঞ্জিন • অফলাইন রেডি"
                                else "Exact BigInteger Engine • Step-by-Step Math",
                                style = MaterialTheme.typography.labelMedium,
                                color = ext.binaryColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (bn) "NumiX-এ স্বাগতম" else "Welcome to NumiX",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (bn) "সংখ্যা পদ্ধতি রূপান্তর করুন, ধাপে ধাপে বুঝুন এবং দক্ষ হোন।"
                        else "Convert, Understand & Master Number Systems.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onNavigate(NavDestination.CONVERTER) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("hero_open_converter"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Calculate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (bn) "পূর্ণ কনভার্টার" else "Open Converter")
                        }
                        OutlinedButton(
                            onClick = { onNavigate(NavDestination.QUIZ) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("hero_open_quiz"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Quiz,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (bn) "কুইজ খেলুন" else "Practice Quiz")
                        }
                    }
                }
            }
        }

        // 2. Main Quick Converter Card
        item {
            GlassCard(accentColor = MaterialTheme.colorScheme.primary) {
                SectionHeader(
                    title = if (bn) "দ্রুত সংখ্যা রূপান্তর" else "Quick Converter",
                    subtitle = if (bn) "তাৎক্ষণিক BigInteger গণনা" else "Instant arbitrary-precision conversion"
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (bn) "উৎস পদ্ধতি (From)" else "Source System",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                SystemSelectorRow(
                    selected = converterState.sourceSystem,
                    onSelect = onSelectSource,
                    language = lang,
                    tagPrefix = "dash_src"
                )

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = converterState.inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_converter_input"),
                    label = {
                        Text(
                            if (bn) "${AppStrings.systemName(converterState.sourceSystem, lang)} সংখ্যা লিখুন"
                            else "Enter ${AppStrings.systemName(converterState.sourceSystem, lang)} integer"
                        )
                    },
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true,
                    isError = converterState.validation !is ValidationResult.Valid &&
                        converterState.validation !is ValidationResult.Empty,
                    supportingText = {
                        val msg = AppStrings.validationMessage(converterState.validation, lang)
                        if (msg.isNotEmpty() && converterState.validation !is ValidationResult.Valid) {
                            Text(msg)
                        } else {
                            Text(
                                if (bn) "বৈধ অঙ্ক: ${converterState.sourceSystem.validDigitsLabel}"
                                else "Allowed digits: ${converterState.sourceSystem.validDigitsLabel}"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (bn) "লক্ষ্য পদ্ধতি (To)" else "Target System",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = onSwap,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("dashboard_swap_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = "Swap source and target systems"
                        )
                    }
                }
                SystemSelectorRow(
                    selected = converterState.targetSystem,
                    onSelect = onSelectTarget,
                    language = lang,
                    tagPrefix = "dash_tgt"
                )

                val result = converterState.result
                if (result != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = ext.colorFor(converterState.targetSystem).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${result.normalizedSource}${result.sourceSystem.subscript}  =",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                BaseBadge(system = converterState.targetSystem)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = result.formattedValueFor(
                                    converterState.targetSystem,
                                    preferences.groupDigits
                                ),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ext.colorFor(converterState.targetSystem)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onConvertAndOpen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("dashboard_full_breakdown_btn")
                ) {
                    Text(if (bn) "সব ফলাফল ও ধাপে ধাপে ব্যাখ্যা দেখুন" else "Convert & View Step-by-Step Explanation")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 3. Quick Conversion Presets
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = if (bn) "দ্রুত উদাহরণ" else "Quick Conversions",
                    subtitle = if (bn) "এক ক্লিকে পরীক্ষা করুন" else "One-tap verified examples"
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(dashboardPresets) { preset ->
                        FilledTonalButton(
                            onClick = { onApplyPreset(preset.input, preset.source, preset.target) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("preset_${preset.input}_${preset.source.shortCode.lowercase()}")
                        ) {
                            Text(if (bn) preset.labelBn else preset.labelEn)
                        }
                    }
                }
            }
        }

        // 4. Four Number System Info Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(
                    title = if (bn) "সংখ্যা পদ্ধতিসমূহ" else "Number Systems",
                    subtitle = if (bn) "প্রতিটি ভিত্তির মূল ধারণা জানুন" else "Explore bases, digits, and applications"
                )
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isTwoCol = maxWidth >= 560.dp
                    val systems = NumberSystem.entries
                    if (isTwoCol) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (rowIndex in 0 until 2) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    for (colIndex in 0 until 2) {
                                        val sys = systems[rowIndex * 2 + colIndex]
                                        SystemInfoCard(
                                            system = sys,
                                            language = lang,
                                            onLearnMore = { onOpenLesson(sys) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            systems.forEach { sys ->
                                SystemInfoCard(
                                    system = sys,
                                    language = lang,
                                    onLearnMore = { onOpenLesson(sys) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Learning & Tool Shortcuts
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(
                    title = if (bn) "শেখার শর্টকাট" else "Learning Shortcuts",
                    subtitle = if (bn) "অনুশীলন, সারণি ও পাঠ" else "Interactive tools to build mastery"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ShortcutTile(
                        title = if (bn) "পাঠশালা" else "Lessons",
                        icon = Icons.Rounded.MenuBook,
                        accent = ext.binaryColor,
                        onClick = { onNavigate(NavDestination.LEARN) },
                        testTag = "shortcut_learn",
                        modifier = Modifier.weight(1f)
                    )
                    ShortcutTile(
                        title = if (bn) "০–২৫৫ সারণি" else "0–255 Table",
                        icon = Icons.Rounded.TableChart,
                        accent = ext.octalColor,
                        onClick = { onNavigate(NavDestination.TABLE) },
                        testTag = "shortcut_table",
                        modifier = Modifier.weight(1f)
                    )
                    ShortcutTile(
                        title = if (bn) "অনুশীলন কুইজ" else "Quiz Mode",
                        icon = Icons.Rounded.Quiz,
                        accent = ext.hexColor,
                        onClick = { onNavigate(NavDestination.QUIZ) },
                        testTag = "shortcut_quiz",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 6. Recent Conversion History
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = if (bn) "সাম্প্রতিক রূপান্তর" else "Recent History",
                    trailing = {
                        TextButton(
                            onClick = { onNavigate(NavDestination.HISTORY) },
                            modifier = Modifier.testTag("dashboard_view_all_history")
                        ) {
                            Text(if (bn) "সব দেখুন" else "View All")
                        }
                    }
                )
                if (recentHistory.isEmpty()) {
                    GlassCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (bn) "এখনো কোনো রূপান্তর সংরক্ষিত হয়নি। কনভার্টার থেকে Convert চাপুন!"
                                else "No saved conversions yet. Tap Convert & View Explanation to save one.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    recentHistory.take(4).forEach { entry ->
                        val srcSys = NumberSystem.fromBase(entry.sourceBase)
                        val tgtSys = NumberSystem.fromBase(entry.targetBase)
                        GlassCard(
                            onClick = { onReuseHistory(entry) },
                            modifier = Modifier.testTag("recent_history_item_${entry.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${entry.originalNumber}${srcSys.subscript}  →  ${entry.convertedNumber}${tgtSys.subscript}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${AppStrings.systemName(srcSys, lang)} → ${AppStrings.systemName(tgtSys, lang)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = if (bn) "পুনঃব্যবহার" else "Reuse",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (bn) "NumiX • নির্ভুল সংখ্যা পদ্ধতি রূপান্তর ও গণিত শিক্ষা প্ল্যাটফর্ম"
                    else "NumiX • Precision Number System Converter & Learning Platform",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SystemInfoCard(
    system: NumberSystem,
    language: AppLanguage,
    onLearnMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bn = language == AppLanguage.BANGLA
    val ext = LocalNumixColors.current
    val accent = ext.colorFor(system)

    GlassCard(
        modifier = modifier.testTag("system_info_card_${system.shortCode.lowercase()}"),
        accentColor = accent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppStrings.systemName(system, language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            BaseBadge(system = system)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (bn) "বৈধ অঙ্ক: ${system.validDigitsLabel}" else "Valid digits: ${system.validDigitsLabel}",
            style = MaterialTheme.typography.labelSmall,
            color = accent
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = AppStrings.systemShortDesc(system, language),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(
            onClick = onLearnMore,
            contentPadding = PaddingValues(horizontal = 0.dp),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("learn_more_${system.shortCode.lowercase()}")
        ) {
            Text(if (bn) "আরও জানুন" else "Learn More", color = accent)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ShortcutTile(
    title: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag),
        accentColor = accent,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
