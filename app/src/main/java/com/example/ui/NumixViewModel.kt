package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ConversionHistoryEntity
import com.example.data.local.NumixDatabase
import com.example.data.preferences.AppLanguage
import com.example.data.preferences.PreferencesRepository
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferences
import com.example.data.repository.NumixRepository
import com.example.domain.ConversionExplanation
import com.example.domain.ConversionResult
import com.example.domain.NumberConverterEngine
import com.example.domain.NumberSystem
import com.example.domain.QuizDifficulty
import com.example.domain.QuizEngine
import com.example.domain.QuizInputMode
import com.example.domain.QuizQuestion
import com.example.domain.StepByStepExplanationEngine
import com.example.domain.ValidationResult
import com.example.localization.NavDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConverterUiState(
    val inputText: String = "45",
    val sourceSystem: NumberSystem = NumberSystem.DECIMAL,
    val targetSystem: NumberSystem = NumberSystem.BINARY,
    val validation: ValidationResult = ValidationResult.Empty,
    val result: ConversionResult? = null,
    val explanation: ConversionExplanation? = null,
    val activeStepIndex: Int = 0,
    val showAllStepsExpanded: Boolean = true,
    val showWhyItWorks: Boolean = false
)

data class QuizUiState(
    val difficulty: QuizDifficulty = QuizDifficulty.BEGINNER,
    val preferredMode: QuizInputMode? = null,
    val currentQuestion: QuizQuestion = QuizEngine.generateQuestion(QuizDifficulty.BEGINNER),
    val selectedOption: String? = null,
    val typedAnswer: String = "",
    val isSubmitted: Boolean = false,
    val isCorrect: Boolean? = null,
    val sessionAnswered: Int = 0,
    val sessionCorrect: Int = 0
)

class NumixViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NumixDatabase.getInstance(application)
    private val preferencesRepo = PreferencesRepository(application)
    private val repository = NumixRepository(database.historyDao(), preferencesRepo)

    val preferences: StateFlow<UserPreferences> = repository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences()
    )

    val allHistory: StateFlow<List<ConversionHistoryEntity>> = repository.allHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favorites: StateFlow<List<ConversionHistoryEntity>> = repository.favorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentDestination = MutableStateFlow(NavDestination.DASHBOARD)
    val currentDestination: StateFlow<NavDestination> = _currentDestination.asStateFlow()

    private val _converterState = MutableStateFlow(ConverterUiState())
    val converterState: StateFlow<ConverterUiState> = _converterState.asStateFlow()

    private val _quizState = MutableStateFlow(QuizUiState())
    val quizState: StateFlow<QuizUiState> = _quizState.asStateFlow()

    private val _selectedLessonSystem = MutableStateFlow(NumberSystem.BINARY)
    val selectedLessonSystem: StateFlow<NumberSystem> = _selectedLessonSystem.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Global search state for quick-jump dialog
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredHistory: StateFlow<List<ConversionHistoryEntity>> = combine(
        allHistory,
        _searchQuery
    ) { items, query ->
        val q = query.trim().lowercase()
        if (q.isEmpty()) items
        else items.filter {
            it.originalNumber.lowercase().contains(q) ||
                it.convertedNumber.lowercase().contains(q) ||
                it.decimalValue.contains(q) ||
                it.hexValue.lowercase().contains(q)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        performConversion(saveToHistory = false)
    }

    fun navigateTo(dest: NavDestination) {
        _currentDestination.value = dest
    }

    fun openLesson(system: NumberSystem) {
        _selectedLessonSystem.value = system
        _currentDestination.value = NavDestination.LEARN
    }

    fun selectLessonSystem(system: NumberSystem) {
        _selectedLessonSystem.value = system
    }

    fun updateInput(newText: String) {
        _converterState.update { it.copy(inputText = newText) }
        if (preferences.value.realTimeConversion) {
            performConversion(saveToHistory = false)
        }
    }

    fun appendCharacter(ch: String) {
        updateInput(_converterState.value.inputText + ch)
    }

    fun backspaceInput() {
        val current = _converterState.value.inputText
        if (current.isNotEmpty()) {
            updateInput(current.dropLast(1))
        }
    }

    fun clearInput() {
        _converterState.update {
            it.copy(
                inputText = "",
                validation = ValidationResult.Empty,
                result = null,
                explanation = null,
                activeStepIndex = 0
            )
        }
    }

    fun selectSourceSystem(system: NumberSystem) {
        _converterState.update { it.copy(sourceSystem = system) }
        if (preferences.value.realTimeConversion || _converterState.value.inputText.isNotBlank()) {
            performConversion(saveToHistory = false)
        }
    }

    fun selectTargetSystem(system: NumberSystem) {
        _converterState.update { it.copy(targetSystem = system) }
        if (preferences.value.realTimeConversion || _converterState.value.result != null) {
            performConversion(saveToHistory = false)
        }
    }

    fun swapSystems() {
        val current = _converterState.value
        val newInput = current.result?.valueFor(current.targetSystem) ?: current.inputText
        _converterState.update {
            it.copy(
                sourceSystem = current.targetSystem,
                targetSystem = current.sourceSystem,
                inputText = newInput,
                activeStepIndex = 0
            )
        }
        performConversion(saveToHistory = false)
    }

    fun applyQuickPreset(
        input: String,
        source: NumberSystem,
        target: NumberSystem,
        openConverter: Boolean = false
    ) {
        _converterState.update {
            it.copy(
                inputText = input,
                sourceSystem = source,
                targetSystem = target,
                activeStepIndex = 0
            )
        }
        performConversion(saveToHistory = true)
        if (openConverter) {
            _currentDestination.value = NavDestination.CONVERTER
        }
    }

    fun convertAndSave() {
        performConversion(saveToHistory = true)
    }

    private fun performConversion(saveToHistory: Boolean) {
        val state = _converterState.value
        val (validation, result) = NumberConverterEngine.convert(
            rawInput = state.inputText,
            sourceSystem = state.sourceSystem,
            targetSystem = state.targetSystem
        )
        val explanation = result?.let { StepByStepExplanationEngine.generate(it) }
        _converterState.update {
            it.copy(
                validation = validation,
                result = result,
                explanation = explanation,
                activeStepIndex = 0
            )
        }
        if (saveToHistory && result != null) {
            viewModelScope.launch {
                val latest = allHistory.value.firstOrNull()
                val isDuplicate = latest != null &&
                    latest.originalNumber == result.normalizedSource &&
                    latest.sourceBase == result.sourceSystem.base &&
                    latest.targetBase == result.targetSystem.base
                if (!isDuplicate) {
                    repository.saveConversion(result, isFavorite = false)
                }
            }
        }
    }

    fun saveCurrentAsFavorite() {
        val result = _converterState.value.result ?: return
        viewModelScope.launch {
            repository.saveConversion(result, isFavorite = true)
            val bn = preferences.value.language == AppLanguage.BANGLA
            showToast(if (bn) "প্রিয় তালিকায় সংরক্ষিত হয়েছে" else "Saved to favorites")
        }
    }

    fun toggleFavorite(item: ConversionHistoryEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(item.id, !item.isFavorite)
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            val bn = preferences.value.language == AppLanguage.BANGLA
            showToast(if (bn) "সব ইতিহাস মুছে ফেলা হয়েছে" else "Conversion history cleared")
        }
    }

    fun reuseHistoryItem(item: ConversionHistoryEntity) {
        applyQuickPreset(
            input = item.originalNumber,
            source = NumberSystem.fromBase(item.sourceBase),
            target = NumberSystem.fromBase(item.targetBase),
            openConverter = true
        )
    }

    fun setExplanationStep(index: Int) {
        val maxIdx = (_converterState.value.explanation?.steps?.size ?: 1) - 1
        _converterState.update {
            it.copy(activeStepIndex = index.coerceIn(0, maxOf(0, maxIdx)))
        }
    }

    fun toggleAllStepsExpanded() {
        _converterState.update { it.copy(showAllStepsExpanded = !it.showAllStepsExpanded) }
    }

    fun toggleWhyItWorks() {
        _converterState.update { it.copy(showWhyItWorks = !it.showWhyItWorks) }
    }

    // Quiz actions
    fun setQuizDifficulty(difficulty: QuizDifficulty) {
        _quizState.update {
            it.copy(
                difficulty = difficulty,
                currentQuestion = QuizEngine.generateQuestion(difficulty, it.preferredMode),
                selectedOption = null,
                typedAnswer = "",
                isSubmitted = false,
                isCorrect = null
            )
        }
    }

    fun setQuizMode(mode: QuizInputMode?) {
        _quizState.update {
            it.copy(
                preferredMode = mode,
                currentQuestion = QuizEngine.generateQuestion(it.difficulty, mode),
                selectedOption = null,
                typedAnswer = "",
                isSubmitted = false,
                isCorrect = null
            )
        }
    }

    fun selectQuizOption(option: String) {
        if (_quizState.value.isSubmitted) return
        _quizState.update { it.copy(selectedOption = option) }
    }

    fun updateQuizTypedAnswer(text: String) {
        if (_quizState.value.isSubmitted) return
        _quizState.update { it.copy(typedAnswer = text) }
    }

    fun submitQuizAnswer() {
        val state = _quizState.value
        if (state.isSubmitted) return
        val answer = if (state.currentQuestion.inputMode == QuizInputMode.MULTIPLE_CHOICE) {
            state.selectedOption.orEmpty()
        } else {
            state.typedAnswer
        }
        if (answer.isBlank()) return

        val correct = QuizEngine.checkAnswer(answer, state.currentQuestion)
        _quizState.update {
            it.copy(
                isSubmitted = true,
                isCorrect = correct,
                sessionAnswered = it.sessionAnswered + 1,
                sessionCorrect = it.sessionCorrect + if (correct) 1 else 0
            )
        }
        viewModelScope.launch {
            repository.recordQuizResult(correct)
        }
    }

    fun nextQuizQuestion() {
        _quizState.update {
            it.copy(
                currentQuestion = QuizEngine.generateQuestion(it.difficulty, it.preferredMode),
                selectedOption = null,
                typedAnswer = "",
                isSubmitted = false,
                isCorrect = null
            )
        }
    }

    // Preferences actions
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun cycleTheme() {
        val next = when (preferences.value.themeMode) {
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.SYSTEM
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
        setThemeMode(next)
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun toggleLanguage() {
        val next = if (preferences.value.language == AppLanguage.ENGLISH) {
            AppLanguage.BANGLA
        } else {
            AppLanguage.ENGLISH
        }
        setLanguage(next)
    }

    fun setRealTimeConversion(enabled: Boolean) {
        viewModelScope.launch {
            repository.setRealTimeConversion(enabled)
            if (enabled) performConversion(saveToHistory = false)
        }
    }

    fun setGroupDigits(enabled: Boolean) {
        viewModelScope.launch { repository.setGroupDigits(enabled) }
    }

    fun setSoundEffects(enabled: Boolean) {
        viewModelScope.launch { repository.setSoundEffects(enabled) }
    }

    fun resetAllPreferences() {
        viewModelScope.launch {
            repository.resetAllPreferences()
            val bn = preferences.value.language == AppLanguage.BANGLA
            showToast(if (bn) "সেটিংস রিসেট করা হয়েছে" else "Preferences reset to defaults")
        }
    }

    fun resetQuizStats() {
        viewModelScope.launch {
            repository.resetQuizStats()
            _quizState.update { it.copy(sessionAnswered = 0, sessionCorrect = 0) }
        }
    }

    fun buildCsvContent(): String = repository.buildCsvExport(allHistory.value)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
