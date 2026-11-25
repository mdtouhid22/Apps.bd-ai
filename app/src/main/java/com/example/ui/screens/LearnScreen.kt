package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.preferences.AppLanguage
import com.example.domain.NumberSystem
import com.example.localization.AppStrings
import com.example.ui.components.BaseBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.SystemSelectorRow
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalNumixColors

@Composable
fun LearnScreen(
    selectedSystem: NumberSystem,
    language: AppLanguage,
    onSelectSystem: (NumberSystem) -> Unit,
    onTryInConverter: (String, NumberSystem, NumberSystem) -> Unit,
    onStartQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bn = language == AppLanguage.BANGLA
    val ext = LocalNumixColors.current
    val lessons = AppStrings.lessons(language)
    val lesson = lessons.first { it.system == selectedSystem }
    val accent = ext.colorFor(selectedSystem)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Illustrated Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .clip(RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_learn_systems),
                    contentDescription = "Number systems crystalline illustration",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF070B14).copy(alpha = 0.28f),
                                    Color(0xFF070B14).copy(alpha = 0.90f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp)
                ) {
                    Text(
                        text = if (bn) "সংখ্যা পদ্ধতি শিখুন" else "Learn Number Systems",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (bn) "বাইনারি, ডেসিম্যাল, অক্টাল ও হেক্সাডেসিম্যালের বিস্তারিত পাঠ"
                        else "Interactive lessons with place values, formulas & real-world uses",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // System Picker Tabs
        item {
            SystemSelectorRow(
                selected = selectedSystem,
                onSelect = onSelectSystem,
                language = language,
                tagPrefix = "learn_tab"
            )
        }

        // Lesson Card
        item {
            GlassCard(accentColor = accent) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BaseBadge(system = lesson.system)
                    Text(
                        text = if (bn) "অঙ্ক: ${lesson.system.validDigitsLabel}" else "Digits: ${lesson.system.validDigitsLabel}",
                        style = MaterialTheme.typography.labelMedium,
                        color = accent
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lesson.subtitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = accent
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = lesson.definition,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = if (bn) "স্থানীয় মান (Place Values)" else "Place Values"
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = lesson.placeValuesExplanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = if (bn) "উদাহরণ বিশ্লেষণ" else "Worked Example"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = accent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = lesson.exampleFormula,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = accent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        lesson.exampleBreakdown.forEach { row ->
                            Text(
                                text = row,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = if (bn) "বাস্তব জীবনে ব্যবহার" else "Real-World Applications"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    lesson.realWorldUses.forEach { useCase ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircleOutline,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = useCase,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val target = if (lesson.system == NumberSystem.DECIMAL) NumberSystem.BINARY else NumberSystem.DECIMAL
                    Button(
                        onClick = { onTryInConverter(lesson.trySampleInput, lesson.system, target) },
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("learn_try_converter_btn")
                    ) {
                        Icon(Icons.Rounded.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (bn) "কনভার্টারে দেখুন" else "Try in Converter")
                    }
                    OutlinedButton(
                        onClick = onStartQuiz,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("learn_start_quiz_btn")
                    ) {
                        Icon(Icons.Rounded.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (bn) "কুইজ" else "Quiz")
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
