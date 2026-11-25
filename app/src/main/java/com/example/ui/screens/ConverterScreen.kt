package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.preferences.AppLanguage
import com.example.data.preferences.UserPreferences
import com.example.domain.ConversionExplanation
import com.example.domain.ExplanationStep
import com.example.domain.NumberSystem
import com.example.domain.ValidationResult
import com.example.localization.AppStrings
import com.example.ui.ConverterUiState
import com.example.ui.components.GlassCard
import com.example.ui.components.ResultRepresentationCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.SystemSelectorRow
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalNumixColors
import com.example.ui.util.PlatformActions

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConverterScreen(
    state: ConverterUiState,
    preferences: UserPreferences,
    onInputChange: (String) -> Unit,
    onAppendChar: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSelectSource: (NumberSystem) -> Unit,
    onSelectTarget: (NumberSystem) -> Unit,
    onSwap: () -> Unit,
    onConvertAndSave: () -> Unit,
    onSaveFavorite: () -> Unit,
    onToggleRealTime: (Boolean) -> Unit,
    onStepChange: (Int) -> Unit,
    onToggleAllSteps: () -> Unit,
    onToggleWhyItWorks: () -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang = preferences.language
    val bn = lang == AppLanguage.BANGLA
    val ext = LocalNumixColors.current

    val downloadExplanationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        val exp = state.explanation
        if (uri != null && exp != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(exp.toPlainTextReport(bn).toByteArray(Charsets.UTF_8))
                }
                onShowToast(if (bn) "ব্যাখ্যা ফাইল সংরক্ষিত হয়েছে" else "Explanation saved as .txt")
            } catch (_: Exception) {
                onShowToast(if (bn) "ফাইল সংরক্ষণ করা যায়নি" else "Failed to save explanation file")
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Advanced Input Workspace
        item {
            GlassCard(accentColor = MaterialTheme.colorScheme.primary) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (bn) "উন্নত সংখ্যা কনভার্টার" else "Advanced Number Converter",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (bn) "BigInteger নির্ভুলতা • ১২টি রূপান্তর দিক"
                            else "Arbitrary-precision BigInteger • All 12 directions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (bn) "লাইভ" else "Live",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = preferences.realTimeConversion,
                            onCheckedChange = onToggleRealTime,
                            modifier = Modifier.testTag("realtime_conversion_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (bn) "উৎস সংখ্যা পদ্ধতি (Source)" else "Source Number System",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                SystemSelectorRow(
                    selected = state.sourceSystem,
                    onSelect = onSelectSource,
                    language = lang,
                    tagPrefix = "conv_src"
                )

                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = state.inputText,
                    onValueChange = {
                        PlatformActions.playFeedbackTone(preferences.soundEffects)
                        onInputChange(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("converter_number_input"),
                    label = {
                        Text(
                            if (bn) "${AppStrings.systemName(state.sourceSystem, lang)} মান লিখুন"
                            else "Enter ${AppStrings.systemName(state.sourceSystem, lang)} integer"
                        )
                    },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    trailingIcon = {
                        Row {
                            if (state.inputText.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        if (PlatformActions.copyToClipboard(context, "Input", state.inputText)) {
                                            onShowToast(if (bn) "ইনপুট কপি হয়েছে" else "Input copied")
                                        }
                                    },
                                    modifier = Modifier.testTag("copy_input_btn")
                                ) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy input")
                                }
                                IconButton(
                                    onClick = onClear,
                                    modifier = Modifier.testTag("clear_input_btn")
                                ) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "Clear input")
                                }
                            }
                        }
                    },
                    isError = state.validation !is ValidationResult.Valid &&
                        state.validation !is ValidationResult.Empty,
                    supportingText = {
                        val msg = AppStrings.validationMessage(state.validation, lang)
                        if (msg.isNotEmpty() && state.validation !is ValidationResult.Valid) {
                            Text(msg, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(
                                if (bn) "সমর্থিত অঙ্ক: ${state.sourceSystem.validDigitsLabel} (ঋণাত্মক '-' সহ)"
                                else "Allowed: ${state.sourceSystem.validDigitsLabel} (supports leading '-' and arbitrary length)"
                            )
                        }
                    }
                )

                // Adaptive Valid Digit Quick-Pad
                Spacer(modifier = Modifier.height(10.dp))
                val quickKeys = when (state.sourceSystem) {
                    NumberSystem.BINARY -> listOf("0", "1", "-")
                    NumberSystem.OCTAL -> listOf("0", "1", "2", "3", "4", "5", "6", "7", "-")
                    NumberSystem.DECIMAL -> listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "-")
                    NumberSystem.HEXADECIMAL -> listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "-")
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickKeys.forEach { key ->
                        Surface(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    PlatformActions.playFeedbackTone(preferences.soundEffects)
                                    if (key == "-") {
                                        val toggled = if (state.inputText.startsWith("-")) {
                                            state.inputText.drop(1)
                                        } else {
                                            "-" + state.inputText
                                        }
                                        onInputChange(toggled)
                                    } else {
                                        onAppendChar(key)
                                    }
                                }
                                .testTag("quick_key_$key"),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, ext.glassBorder)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = JetBrainsMonoFontFamily
                                    )
                                )
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                PlatformActions.playFeedbackTone(preferences.soundEffects)
                                onBackspace()
                            }
                            .testTag("quick_key_backspace"),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                contentDescription = "Backspace",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (bn) "লক্ষ্য সংখ্যা পদ্ধতি (Target)" else "Target Number System",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FilledTonalButton(
                        onClick = {
                            PlatformActions.playFeedbackTone(preferences.soundEffects)
                            onSwap()
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("swap_systems_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (bn) "অদলবদল" else "Swap")
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                SystemSelectorRow(
                    selected = state.targetSystem,
                    onSelect = onSelectTarget,
                    language = lang,
                    tagPrefix = "conv_tgt"
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            PlatformActions.playFeedbackTone(preferences.soundEffects)
                            onConvertAndSave()
                            onShowToast(if (bn) "রূপান্তর সম্পন্ন ও ইতিহাসে সংরক্ষিত" else "Converted & saved to history")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("convert_save_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (bn) "রূপান্তর ও সংরক্ষণ" else "Convert & Save")
                    }
                    OutlinedButton(
                        onClick = onClear,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("clear_all_btn")
                    ) {
                        Text(if (bn) "মুছুন" else "Clear")
                    }
                }
            }
        }

        // 2. Simultaneous Four-Base Result Cards
        val result = state.result
        if (result != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(
                        title = if (bn) "সব সংখ্যা পদ্ধতিতে ফলাফল" else "All Four Representations",
                        subtitle = if (bn) "এক ক্লিকে কপি করুন বা ইনপুট হিসেবে ব্যবহার করুন" else "Tap Copy or tap a card to convert from that base",
                        trailing = {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = onSaveFavorite,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("favorite_conversion_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.BookmarkAdd,
                                        contentDescription = "Save to favorites"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val shareBody = buildString {
                                            appendLine("NumiX Conversion: ${result.normalizedSource}${result.sourceSystem.subscript}")
                                            appendLine("Binary (2): ${result.binary}")
                                            appendLine("Octal (8): ${result.octal}")
                                            appendLine("Decimal (10): ${result.decimal}")
                                            appendLine("Hexadecimal (16): ${result.hexadecimal}")
                                        }
                                        PlatformActions.shareText(context, "NumiX Conversion", shareBody)
                                    },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("share_conversion_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = "Share conversion results"
                                    )
                                }
                            }
                        }
                    )

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val twoCol = maxWidth >= 560.dp
                        val systems = NumberSystem.entries
                        if (twoCol) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                for (r in 0 until 2) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        for (c in 0 until 2) {
                                            val sys = systems[r * 2 + c]
                                            val rawVal = result.valueFor(sys)
                                            val displayVal = result.formattedValueFor(sys, preferences.groupDigits)
                                            ResultRepresentationCard(
                                                system = sys,
                                                displayValue = displayVal,
                                                isTarget = sys == state.targetSystem,
                                                language = lang,
                                                onCopy = {
                                                    if (PlatformActions.copyToClipboard(context, sys.shortCode, rawVal)) {
                                                        onShowToast("${AppStrings.systemName(sys, lang)} ${if (bn) "কপি হয়েছে" else "copied"}: $rawVal")
                                                    }
                                                },
                                                onUseAsSource = {
                                                    onSelectSource(sys)
                                                    onInputChange(rawVal)
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                systems.forEach { sys ->
                                    val rawVal = result.valueFor(sys)
                                    val displayVal = result.formattedValueFor(sys, preferences.groupDigits)
                                    ResultRepresentationCard(
                                        system = sys,
                                        displayValue = displayVal,
                                        isTarget = sys == state.targetSystem,
                                        language = lang,
                                        onCopy = {
                                            if (PlatformActions.copyToClipboard(context, sys.shortCode, rawVal)) {
                                                onShowToast("${AppStrings.systemName(sys, lang)} ${if (bn) "কপি হয়েছে" else "copied"}: $rawVal")
                                            }
                                        },
                                        onUseAsSource = {
                                            onSelectSource(sys)
                                            onInputChange(rawVal)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val allText = buildString {
                                appendLine("Binary (Base 2): ${result.binary}")
                                appendLine("Octal (Base 8): ${result.octal}")
                                appendLine("Decimal (Base 10): ${result.decimal}")
                                append("Hexadecimal (Base 16): ${result.hexadecimal}")
                            }
                            if (PlatformActions.copyToClipboard(context, "All Results", allText)) {
                                onShowToast(if (bn) "সব ৪টি ফলাফল কপি হয়েছে" else "All 4 results copied")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("copy_all_results_btn")
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (bn) "সব ফলাফল কপি করুন" else "Copy All Results")
                    }
                }
            }
        }

        // 3. Step-by-Step Mathematical Explanation Engine
        val explanation = state.explanation
        if (explanation != null) {
            item {
                ExplanationEngineSection(
                    explanation = explanation,
                    activeStepIndex = state.activeStepIndex,
                    showAllSteps = state.showAllStepsExpanded,
                    showWhyItWorks = state.showWhyItWorks,
                    isBangla = bn,
                    onStepChange = onStepChange,
                    onToggleAllSteps = onToggleAllSteps,
                    onToggleWhyItWorks = onToggleWhyItWorks,
                    onCopyExplanation = {
                        if (PlatformActions.copyToClipboard(context, "NumiX Explanation", explanation.toPlainTextReport(bn))) {
                            onShowToast(if (bn) "সম্পূর্ণ ব্যাখ্যা কপি হয়েছে" else "Explanation copied to clipboard")
                        }
                    },
                    onDownloadTxt = {
                        downloadExplanationLauncher.launch("NumiX_Explanation.txt")
                    },
                    onPrint = {
                        PlatformActions.printExplanation(
                            context,
                            "NumiX: ${explanation.finalAnswerExpression}",
                            explanation.toPlainTextReport(bn)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ExplanationEngineSection(
    explanation: ConversionExplanation,
    activeStepIndex: Int,
    showAllSteps: Boolean,
    showWhyItWorks: Boolean,
    isBangla: Boolean,
    onStepChange: (Int) -> Unit,
    onToggleAllSteps: () -> Unit,
    onToggleWhyItWorks: () -> Unit,
    onCopyExplanation: () -> Unit,
    onDownloadTxt: () -> Unit,
    onPrint: () -> Unit
) {
    val ext = LocalNumixColors.current
    val totalSteps = explanation.steps.size.coerceAtLeast(1)
    val safeIndex = activeStepIndex.coerceIn(0, totalSteps - 1)
    val currentStep = explanation.steps[safeIndex]

    GlassCard(accentColor = ext.octalColor) {
        SectionHeader(
            title = if (isBangla) "ধাপে ধাপে গাণিতিক ব্যাখ্যা" else "Step-by-Step Explanation",
            subtitle = "${explanation.sourceDisplay}  →  ${explanation.targetDisplay}"
        )

        Spacer(modifier = Modifier.height(12.dp))
        // Progress Indicator + Previous / Next Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isBangla) "ধাপ ${safeIndex + 1} / $totalSteps" else "Step ${safeIndex + 1} of $totalSteps",
                style = MaterialTheme.typography.labelLarge,
                color = ext.octalColor
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { onStepChange(safeIndex - 1) },
                    enabled = safeIndex > 0,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("explanation_prev_step_btn"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Previous step", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBangla) "পূর্ববর্তী" else "Prev")
                }
                FilledTonalButton(
                    onClick = { onStepChange(safeIndex + 1) },
                    enabled = safeIndex < totalSteps - 1,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("explanation_next_step_btn"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(if (isBangla) "পরবর্তী" else "Next")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Next step", modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (safeIndex + 1).toFloat() / totalSteps.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = ext.octalColor
        )

        Spacer(modifier = Modifier.height(14.dp))
        // Focused Step Card
        StepDetailCard(step = currentStep, isBangla = isBangla, isHighlighted = true)

        Spacer(modifier = Modifier.height(12.dp))
        // Toggle All Steps View
        TextButton(
            onClick = onToggleAllSteps,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("toggle_all_steps_btn")
        ) {
            Icon(
                imageVector = if (showAllSteps) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                if (showAllSteps) {
                    if (isBangla) "সব ধাপ সংকুচিত করুন" else "Collapse Full Step List"
                } else {
                    if (isBangla) "একসাথে সব ধাপ দেখুন ($totalSteps)" else "Expand All Steps ($totalSteps)"
                }
            )
        }

        AnimatedVisibility(visible = showAllSteps) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                explanation.steps.forEachIndexed { idx, step ->
                    if (idx != safeIndex) {
                        StepDetailCard(
                            step = step,
                            isBangla = isBangla,
                            isHighlighted = false,
                            onClick = { onStepChange(idx) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        // Final Answer Card
        Surface(
            color = ext.successColor.copy(alpha = 0.14f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ext.successColor.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("explanation_final_answer_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isBangla) "চূড়ান্ত উত্তর" else "Final Answer",
                    style = MaterialTheme.typography.labelMedium,
                    color = ext.successColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = explanation.finalAnswerExpression,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        // "Why does this work?" conceptual section
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onToggleWhyItWorks)
                .testTag("why_it_works_toggle"),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = null,
                            tint = ext.warningColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isBangla) "এটি কীভাবে কাজ করে?" else "Why does this work?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Icon(
                        imageVector = if (showWhyItWorks) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null
                    )
                }
                AnimatedVisibility(visible = showWhyItWorks) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = explanation.whyItWorks.get(isBangla),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = ext.glassBorder)
        Spacer(modifier = Modifier.height(12.dp))

        // Action bar: Copy Explanation, Download TXT, Print
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCopyExplanation,
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("copy_explanation_btn"),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(if (isBangla) "কপি" else "Copy", maxLines = 1)
            }
            OutlinedButton(
                onClick = onDownloadTxt,
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("download_explanation_btn"),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(if (isBangla) "সেভ .TXT" else "Save .TXT", maxLines = 1)
            }
            OutlinedButton(
                onClick = onPrint,
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("print_explanation_btn"),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(if (isBangla) "প্রিন্ট" else "Print", maxLines = 1)
            }
        }
    }
}

@Composable
private fun StepDetailCard(
    step: ExplanationStep,
    isBangla: Boolean,
    isHighlighted: Boolean,
    onClick: (() -> Unit)? = null
) {
    val ext = LocalNumixColors.current
    val bg = if (isHighlighted) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
    }
    val border = if (isHighlighted) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    } else {
        BorderStroke(1.dp, ext.glassBorder.copy(alpha = 0.7f))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        color = bg,
        shape = RoundedCornerShape(16.dp),
        border = border
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isBangla) "ধাপ ${step.stepNumber}" else "Step ${step.stepNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = step.title.get(isBangla),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = step.description.get(isBangla),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!step.formula.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = step.formula,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ext.binaryColor
                    )
                }
            }

            if (step.rows.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    step.rows.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (row.highlight) MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)
                                )
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = row.leftExpression,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = row.rightResult,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (row.highlight) ext.binaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            step.note?.let { note ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.get(isBangla),
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.warningColor
                )
            }
        }
    }
}
