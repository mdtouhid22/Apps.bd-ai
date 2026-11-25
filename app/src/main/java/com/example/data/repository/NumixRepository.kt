package com.example.data.repository

import com.example.data.local.ConversionHistoryEntity
import com.example.data.local.HistoryDao
import com.example.data.preferences.AppLanguage
import com.example.data.preferences.PreferencesRepository
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferences
import com.example.domain.ConversionResult
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NumixRepository(
    private val historyDao: HistoryDao,
    private val preferencesRepository: PreferencesRepository
) {
    val allHistory: Flow<List<ConversionHistoryEntity>> = historyDao.getAllHistory()
    val favorites: Flow<List<ConversionHistoryEntity>> = historyDao.getFavorites()
    val preferences: Flow<UserPreferences> = preferencesRepository.preferencesFlow

    suspend fun saveConversion(result: ConversionResult, isFavorite: Boolean = false): Long {
        val entity = ConversionHistoryEntity(
            originalNumber = result.normalizedSource,
            sourceBase = result.sourceSystem.base,
            convertedNumber = result.valueFor(result.targetSystem),
            targetBase = result.targetSystem.base,
            binaryValue = result.binary,
            octalValue = result.octal,
            decimalValue = result.decimal,
            hexValue = result.hexadecimal,
            isFavorite = isFavorite,
            timestamp = System.currentTimeMillis()
        )
        return historyDao.insertHistory(entity)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        historyDao.setFavorite(id, isFavorite)
    }

    suspend fun deleteHistoryItem(id: Long) {
        historyDao.deleteById(id)
    }

    suspend fun clearAllHistory() {
        historyDao.clearAll()
    }

    suspend fun setThemeMode(mode: ThemeMode) = preferencesRepository.setThemeMode(mode)
    suspend fun setLanguage(language: AppLanguage) = preferencesRepository.setLanguage(language)
    suspend fun setRealTimeConversion(enabled: Boolean) = preferencesRepository.setRealTimeConversion(enabled)
    suspend fun setGroupDigits(enabled: Boolean) = preferencesRepository.setGroupDigits(enabled)
    suspend fun setSoundEffects(enabled: Boolean) = preferencesRepository.setSoundEffects(enabled)
    suspend fun recordQuizResult(isCorrect: Boolean) = preferencesRepository.recordQuizResult(isCorrect)
    suspend fun resetQuizStats() = preferencesRepository.resetQuizStats()
    suspend fun resetAllPreferences() = preferencesRepository.resetAllPreferences()

    fun buildCsvExport(items: List<ConversionHistoryEntity>): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val sb = StringBuilder()
        sb.appendLine("OriginalNumber,SourceBase,ConvertedNumber,TargetBase,Binary,Octal,Decimal,Hexadecimal,Favorite,DateTime")
        for (item in items) {
            val dateStr = sdf.format(Date(item.timestamp))
            sb.appendLine(
                listOf(
                    csvEscape(item.originalNumber),
                    item.sourceBase.toString(),
                    csvEscape(item.convertedNumber),
                    item.targetBase.toString(),
                    csvEscape(item.binaryValue),
                    csvEscape(item.octalValue),
                    csvEscape(item.decimalValue),
                    csvEscape(item.hexValue),
                    item.isFavorite.toString(),
                    csvEscape(dateStr)
                ).joinToString(",")
            )
        }
        return sb.toString()
    }

    private fun csvEscape(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
