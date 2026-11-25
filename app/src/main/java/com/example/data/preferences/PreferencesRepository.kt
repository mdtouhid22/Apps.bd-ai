package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "numix_prefs")

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

enum class AppLanguage(val code: String, val displayLabel: String) {
    ENGLISH("en", "English"),
    BANGLA("bn", "বাংলা")
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val realTimeConversion: Boolean = true,
    val groupDigits: Boolean = true,
    val soundEffects: Boolean = false,
    val quizTotalAnswered: Int = 0,
    val quizTotalCorrect: Int = 0,
    val quizCurrentStreak: Int = 0,
    val quizBestStreak: Int = 0
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val REAL_TIME = booleanPreferencesKey("real_time_conversion")
        val GROUP_DIGITS = booleanPreferencesKey("group_digits")
        val SOUND_EFFECTS = booleanPreferencesKey("sound_effects")
        val QUIZ_ANSWERED = intPreferencesKey("quiz_answered")
        val QUIZ_CORRECT = intPreferencesKey("quiz_correct")
        val QUIZ_STREAK = intPreferencesKey("quiz_streak")
        val QUIZ_BEST_STREAK = intPreferencesKey("quiz_best_streak")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.DARK.name
        val langStr = prefs[Keys.LANGUAGE] ?: AppLanguage.ENGLISH.name
        UserPreferences(
            themeMode = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.DARK),
            language = runCatching { AppLanguage.valueOf(langStr) }.getOrDefault(AppLanguage.ENGLISH),
            realTimeConversion = prefs[Keys.REAL_TIME] ?: true,
            groupDigits = prefs[Keys.GROUP_DIGITS] ?: true,
            soundEffects = prefs[Keys.SOUND_EFFECTS] ?: false,
            quizTotalAnswered = prefs[Keys.QUIZ_ANSWERED] ?: 0,
            quizTotalCorrect = prefs[Keys.QUIZ_CORRECT] ?: 0,
            quizCurrentStreak = prefs[Keys.QUIZ_STREAK] ?: 0,
            quizBestStreak = prefs[Keys.QUIZ_BEST_STREAK] ?: 0
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setRealTimeConversion(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REAL_TIME] = enabled }
    }

    suspend fun setGroupDigits(enabled: Boolean) {
        context.dataStore.edit { it[Keys.GROUP_DIGITS] = enabled }
    }

    suspend fun setSoundEffects(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_EFFECTS] = enabled }
    }

    suspend fun recordQuizResult(isCorrect: Boolean) {
        context.dataStore.edit { prefs ->
            val answered = (prefs[Keys.QUIZ_ANSWERED] ?: 0) + 1
            val correct = (prefs[Keys.QUIZ_CORRECT] ?: 0) + if (isCorrect) 1 else 0
            val newStreak = if (isCorrect) (prefs[Keys.QUIZ_STREAK] ?: 0) + 1 else 0
            val best = maxOf(prefs[Keys.QUIZ_BEST_STREAK] ?: 0, newStreak)
            prefs[Keys.QUIZ_ANSWERED] = answered
            prefs[Keys.QUIZ_CORRECT] = correct
            prefs[Keys.QUIZ_STREAK] = newStreak
            prefs[Keys.QUIZ_BEST_STREAK] = best
        }
    }

    suspend fun resetQuizStats() {
        context.dataStore.edit { prefs ->
            prefs[Keys.QUIZ_ANSWERED] = 0
            prefs[Keys.QUIZ_CORRECT] = 0
            prefs[Keys.QUIZ_STREAK] = 0
            prefs[Keys.QUIZ_BEST_STREAK] = 0
        }
    }

    suspend fun resetAllPreferences() {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = ThemeMode.DARK.name
            prefs[Keys.LANGUAGE] = AppLanguage.ENGLISH.name
            prefs[Keys.REAL_TIME] = true
            prefs[Keys.GROUP_DIGITS] = true
            prefs[Keys.SOUND_EFFECTS] = false
        }
    }
}
