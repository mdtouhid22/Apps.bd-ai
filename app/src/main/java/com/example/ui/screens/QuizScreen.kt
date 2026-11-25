package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.preferences.AppLanguage
import com.example.data.preferences.UserPreferences
import com.example.domain.QuizDifficulty
import com.example.domain.QuizInputMode
import com.example.localization.AppStrings
import com.example.ui.QuizUiState
import com.example.ui.components.BaseBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalNumixColors
import com.example.ui.util.PlatformActions

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizScreen(
    quizState: QuizUiState,
    preferences: UserPreferences,
    onSelectDifficulty: (QuizDifficulty) -> Unit,
    onSelectMode: (QuizInputMode?) -> Unit,
    onSelectOption: (String) -> Unit,
    onTypedAnswerChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNextQuestion: () -> Unit,
    onResetStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = preferences.language
    val bn = lang == AppLanguage.BANGLA
    val ext = LocalNumixColors.current
    val q = quizState.currentQuestion

    val accuracyPct = if (preferences.quizTotalAnswered > 0) {
        (preferences.quizTotalCorrect * 100) / preferences.quizTotalAnswered
    } else 0

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Score & Streak Tracker
        item {
            GlassCard(accentColor = ext.hexColor) {
                SectionHeader(
                    title = if (bn) "অনুশীলন ও কুইজ মোড" else "Practice & Quiz Mode",
                    subtitle = if (bn) "অ্যালগরিদমিক প্রশ্ন ও তাৎক্ষণিক ব্যাখ্যা"
                    else "Algorithmically generated challenges with instant step explanations",
                    trailing = {
                        OutlinedButton(
                            onClick = onResetStats,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("quiz_reset_stats_btn"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (bn) "রিসেট" else "Reset")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuizStatPill(
                        label = if (bn) "স্কোর" else "Score",
                        value = "${preferences.quizTotalCorrect}/${preferences.quizTotalAnswered}",
                        sub = "$accuracyPct%",
                        icon = Icons.Rounded.EmojiEvents,
                        accent = ext.decimalColor,
                        modifier = Modifier.weight(1f)
                    )
                    QuizStatPill(
                        label = if (bn) "স্ট্রিক" else "Streak",
                        value = "${preferences.quizCurrentStreak}",
                        sub = if (bn) "সর্বোচ্চ ${preferences.quizBestStreak}" else "Best ${preferences.quizBestStreak}",
                        icon = Icons.Rounded.LocalFireDepartment,
                        accent = ext.warningColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Difficulty & Question Type Controls
        item {
            GlassCard {
                Text(
                    text = if (bn) "কঠিন্যের স্তর" else "Difficulty Level",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuizDifficulty.entries.forEach { diff ->
                        val label = when (diff) {
                            QuizDifficulty.BEGINNER -> if (bn) "প্রাথমিক" else "Beginner"
                            QuizDifficulty.INTERMEDIATE -> if (bn) "মধ্যম" else "Intermediate"
                            QuizDifficulty.ADVANCED -> if (bn) "উন্নত" else "Advanced"
                        }
                        FilterChip(
                            selected = quizState.difficulty == diff,
                            onClick = { onSelectDifficulty(diff) },
                            label = { Text(label) },
                            modifier = Modifier.testTag("quiz_diff_${diff.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (bn) "প্রশ্নের ধরন" else "Question Format",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = quizState.preferredMode == null,
                        onClick = { onSelectMode(null) },
                        label = { Text(if (bn) "মিশ্র" else "Mixed") },
                        modifier = Modifier.testTag("quiz_mode_mixed")
                    )
                    FilterChip(
                        selected = quizState.preferredMode == QuizInputMode.MULTIPLE_CHOICE,
                        onClick = { onSelectMode(QuizInputMode.MULTIPLE_CHOICE) },
                        label = { Text(if (bn) "বহুনির্বাচনি" else "Multiple Choice") },
                        modifier = Modifier.testTag("quiz_mode_mcq")
                    )
                    FilterChip(
                        selected = quizState.preferredMode == QuizInputMode.TYPE_ANSWER,
                        onClick = { onSelectMode(QuizInputMode.TYPE_ANSWER) },
                        label = { Text(if (bn) "উত্তর লিখুন" else "Enter Answer") },
                        modifier = Modifier.testTag("quiz_mode_type")
                    )
                }
            }
        }

        // 3. Active Question Card
        item {
            GlassCard(accentColor = ext.colorFor(q.targetSystem)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BaseBadge(system = q.sourceSystem)
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    BaseBadge(system = q.targetSystem)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (bn)
                        "নিচের ${AppStrings.systemName(q.sourceSystem, lang)} সংখ্যাটিকে ${AppStrings.systemName(q.targetSystem, lang)}-এ রূপান্তর করুন:"
                    else
                        "Convert this ${AppStrings.systemName(q.sourceSystem, lang)} integer to ${AppStrings.systemName(q.targetSystem, lang)}:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${q.promptNumber}${q.sourceSystem.subscript}",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = ext.colorFor(q.sourceSystem),
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (q.inputMode == QuizInputMode.MULTIPLE_CHOICE) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        q.options.forEachIndexed { idx, option ->
                            val isSelected = quizState.selectedOption == option
                            val isCorrectOpt = option == q.correctAnswer
                            val borderColor = when {
                                quizState.isSubmitted && isCorrectOpt -> ext.successColor
                                quizState.isSubmitted && isSelected && !isCorrectOpt -> MaterialTheme.colorScheme.error
                                isSelected -> MaterialTheme.colorScheme.primary
                                else -> ext.glassBorder
                            }
                            val bgColor = when {
                                quizState.isSubmitted && isCorrectOpt -> ext.successColor.copy(alpha = 0.16f)
                                quizState.isSubmitted && isSelected && !isCorrectOpt -> MaterialTheme.colorScheme.error.copy(alpha = 0.16f)
                                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            }
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(enabled = !quizState.isSubmitted) {
                                        PlatformActions.playFeedbackTone(preferences.soundEffects)
                                        onSelectOption(option)
                                    }
                                    .testTag("quiz_option_$idx"),
                                color = bgColor,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.5.dp, borderColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${option}${q.targetSystem.subscript}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    if (quizState.isSubmitted && isCorrectOpt) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = "Correct", tint = ext.successColor)
                                    } else if (quizState.isSubmitted && isSelected && !isCorrectOpt) {
                                        Icon(Icons.Rounded.Cancel, contentDescription = "Incorrect", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = quizState.typedAnswer,
                        onValueChange = onTypedAnswerChange,
                        enabled = !quizState.isSubmitted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_typed_input"),
                        label = {
                            Text(
                                if (bn) "${AppStrings.systemName(q.targetSystem, lang)} উত্তর লিখুন"
                                else "Enter ${AppStrings.systemName(q.targetSystem, lang)} answer"
                            )
                        },
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        singleLine = true,
                        supportingText = {
                            Text(
                                if (bn) "বৈধ অঙ্ক: ${q.targetSystem.validDigitsLabel}"
                                else "Valid digits: ${q.targetSystem.validDigitsLabel}"
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                val canSubmit = if (q.inputMode == QuizInputMode.MULTIPLE_CHOICE) {
                    quizState.selectedOption != null
                } else {
                    quizState.typedAnswer.isNotBlank()
                }
                if (!quizState.isSubmitted) {
                    Button(
                        onClick = {
                            PlatformActions.playFeedbackTone(preferences.soundEffects)
                            onSubmit()
                        },
                        enabled = canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("quiz_submit_btn")
                    ) {
                        Text(if (bn) "উত্তর যাচাই করুন" else "Check Answer")
                    }
                } else {
                    Button(
                        onClick = onNextQuestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("quiz_next_btn")
                    ) {
                        Text(if (bn) "পরবর্তী প্রশ্ন" else "Next Question")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // 4. Instant Feedback & Dynamic Step-by-Step Solution
        item {
            AnimatedVisibility(visible = quizState.isSubmitted) {
                val correct = quizState.isCorrect == true
                val feedbackColor = if (correct) ext.successColor else MaterialTheme.colorScheme.error
                GlassCard(accentColor = feedbackColor) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (correct) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                            contentDescription = null,
                            tint = feedbackColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = if (correct) {
                                    if (bn) "সঠিক উত্তর!" else "Correct! Well done!"
                                } else {
                                    if (bn) "সঠিক নয় — নিচে সমাধান দেখুন" else "Not quite — review the exact steps"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = feedbackColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${if (bn) "সঠিক উত্তর:" else "Correct Answer:"} ${q.correctAnswer}${q.targetSystem.subscript}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (bn) "কেন এটি সঠিক?" else "Why this answer is correct:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    q.explanation.steps.forEach { step ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "${step.stepNumber}. ${step.title.get(bn)}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = step.description.get(bn),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!step.formula.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = step.formula,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                                        color = ext.binaryColor
                                    )
                                }
                                step.rows.take(6).forEach { r ->
                                    Text(
                                        text = "${r.leftExpression}  →  ${r.rightResult}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizStatPill(
    label: String,
    value: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    value,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                    fontWeight = FontWeight.Bold
                )
                Text(sub, style = MaterialTheme.typography.labelSmall, color = accent)
            }
        }
    }
}
