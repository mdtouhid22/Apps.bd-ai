package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.preferences.ThemeMode
import com.example.domain.NumberSystem

data class NumixExtendedColors(
    val glassSurface: Color,
    val glassBorder: Color,
    val subtleGlowBlue: Color,
    val subtleGlowViolet: Color,
    val subtleGlowCyan: Color,
    val binaryColor: Color,
    val octalColor: Color,
    val decimalColor: Color,
    val hexColor: Color,
    val successColor: Color,
    val warningColor: Color,
    val isDark: Boolean
) {
    fun colorFor(system: NumberSystem): Color = when (system) {
        NumberSystem.BINARY -> binaryColor
        NumberSystem.OCTAL -> octalColor
        NumberSystem.DECIMAL -> decimalColor
        NumberSystem.HEXADECIMAL -> hexColor
    }
}

val LocalNumixColors = staticCompositionLocalOf {
    NumixExtendedColors(
        glassSurface = NavySurfaceElevated.copy(alpha = 0.78f),
        glassBorder = NavyBorder,
        subtleGlowBlue = ElectricBlue.copy(alpha = 0.16f),
        subtleGlowViolet = ElectricViolet.copy(alpha = 0.16f),
        subtleGlowCyan = ElectricCyan.copy(alpha = 0.16f),
        binaryColor = BinaryAccent,
        octalColor = OctalAccent,
        decimalColor = DecimalAccent,
        hexColor = HexAccent,
        successColor = EmeraldAccent,
        warningColor = AmberAccent,
        isDark = true
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlueLight,
    onPrimary = Color(0xFF05132B),
    primaryContainer = Color(0xFF1A325C),
    onPrimaryContainer = Color(0xFFD6E6FF),
    secondary = ElectricVioletLight,
    onSecondary = Color(0xFF1F0C47),
    secondaryContainer = Color(0xFF2F1E5C),
    onSecondaryContainer = Color(0xFFECE2FF),
    tertiary = ElectricCyanLight,
    onTertiary = Color(0xFF032028),
    tertiaryContainer = Color(0xFF0D3844),
    onTertiaryContainer = Color(0xFFCFF8FF),
    background = NavyDeep,
    onBackground = Color(0xFFF1F5F9),
    surface = NavySurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = NavySurfaceElevated,
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainerLowest = NavyDeep,
    surfaceContainerLow = NavySurface,
    surfaceContainer = NavySurfaceElevated,
    surfaceContainerHigh = NavySurfaceHigh,
    outline = NavyBorder,
    outlineVariant = Color(0xFF1E293B),
    error = RoseError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = ElectricViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF4C1D95),
    tertiary = ElectricCyan,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFFAFE),
    onTertiaryContainer = Color(0xFF164E63),
    background = SlateLightBg,
    onBackground = SlateTextPrimary,
    surface = SlateLightSurface,
    onSurface = SlateTextPrimary,
    surfaceVariant = SlateLightElevated,
    onSurfaceVariant = SlateTextSecondary,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = SlateLightBg,
    surfaceContainer = SlateLightSurface,
    surfaceContainerHigh = SlateLightElevated,
    outline = SlateLightBorder,
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFDC2626),
    onError = Color.White
)

private val NumixShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun NumixTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val extendedColors = if (isDark) {
        NumixExtendedColors(
            glassSurface = NavySurfaceElevated.copy(alpha = 0.85f),
            glassBorder = NavyBorder.copy(alpha = 0.85f),
            subtleGlowBlue = ElectricBlue.copy(alpha = 0.18f),
            subtleGlowViolet = ElectricViolet.copy(alpha = 0.18f),
            subtleGlowCyan = ElectricCyan.copy(alpha = 0.16f),
            binaryColor = ElectricCyanLight,
            octalColor = ElectricVioletLight,
            decimalColor = ElectricBlueLight,
            hexColor = Color(0xFF34D399),
            successColor = Color(0xFF34D399),
            warningColor = Color(0xFFFBBF24),
            isDark = true
        )
    } else {
        NumixExtendedColors(
            glassSurface = SlateLightSurface.copy(alpha = 0.94f),
            glassBorder = SlateLightBorder,
            subtleGlowBlue = ElectricBlue.copy(alpha = 0.10f),
            subtleGlowViolet = ElectricViolet.copy(alpha = 0.10f),
            subtleGlowCyan = ElectricCyan.copy(alpha = 0.10f),
            binaryColor = Color(0xFF0891B2),
            octalColor = Color(0xFF7C3AED),
            decimalColor = Color(0xFF2563EB),
            hexColor = Color(0xFF059669),
            successColor = Color(0xFF059669),
            warningColor = Color(0xFFD97706),
            isDark = false
        )
    }

    CompositionLocalProvider(LocalNumixColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NumixTypography,
            shapes = NumixShapes,
            content = content
        )
    }
}
