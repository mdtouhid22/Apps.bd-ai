package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.preferences.AppLanguage
import com.example.data.preferences.ThemeMode
import com.example.domain.NumberConverterEngine
import com.example.domain.NumberSystem
import com.example.localization.AppStrings
import com.example.localization.NavDestination
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TableScreen
import com.example.ui.theme.LocalNumixColors
import com.example.ui.theme.NumixTheme
import kotlinx.coroutines.launch

private data class NavMeta(
    val dest: NavDestination,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val allNavItems = listOf(
    NavMeta(NavDestination.DASHBOARD, Icons.Rounded.Dashboard, Icons.Outlined.Dashboard),
    NavMeta(NavDestination.CONVERTER, Icons.Rounded.Calculate, Icons.Outlined.Calculate),
    NavMeta(NavDestination.LEARN, Icons.Rounded.MenuBook, Icons.Outlined.MenuBook),
    NavMeta(NavDestination.QUIZ, Icons.Rounded.Quiz, Icons.Outlined.Quiz),
    NavMeta(NavDestination.TABLE, Icons.Rounded.TableChart, Icons.Outlined.TableChart),
    NavMeta(NavDestination.HISTORY, Icons.Rounded.History, Icons.Outlined.History),
    NavMeta(NavDestination.SETTINGS, Icons.Rounded.Settings, Icons.Outlined.Settings),
    NavMeta(NavDestination.ABOUT, Icons.Rounded.Info, Icons.Outlined.Info)
)

private val bottomNavItems = allNavItems.take(5)

@Composable
fun NumixApp(
    viewModel: NumixViewModel = viewModel()
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val currentDest by viewModel.currentDestination.collectAsStateWithLifecycle()
    val converterState by viewModel.converterState.collectAsStateWithLifecycle()
    val quizState by viewModel.quizState.collectAsStateWithLifecycle()
    val allHistory by viewModel.allHistory.collectAsStateWithLifecycle()
    val filteredHistory by viewModel.filteredHistory.collectAsStateWithLifecycle()
    val selectedLessonSystem by viewModel.selectedLessonSystem.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showQuickSearch by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        val msg = toastMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    BackHandler(enabled = currentDest != NavDestination.DASHBOARD) {
        viewModel.navigateTo(NavDestination.DASHBOARD)
    }

    NumixTheme(themeMode = preferences.themeMode) {
        val lang = preferences.language
        val bn = lang == AppLanguage.BANGLA

        if (showQuickSearch) {
            QuickSearchDialog(
                language = lang,
                onDismiss = { showQuickSearch = false },
                onConvertQuery = { input, src, tgt ->
                    showQuickSearch = false
                    viewModel.applyQuickPreset(input, src, tgt, openConverter = true)
                },
                onNavigate = { dest ->
                    showQuickSearch = false
                    viewModel.navigateTo(dest)
                }
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val isExpanded = maxWidth >= 900.dp
            val isMedium = maxWidth >= 600.dp && maxWidth < 900.dp
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            val mainScaffold: @Composable () -> Unit = {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        NumixTopBar(
                            showMenuButton = !isExpanded && !isMedium,
                            themeMode = preferences.themeMode,
                            language = lang,
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onSearchClick = { showQuickSearch = true },
                            onCycleTheme = { viewModel.cycleTheme() },
                            onToggleLanguage = { viewModel.toggleLanguage() },
                            onSettingsClick = { viewModel.navigateTo(NavDestination.SETTINGS) }
                        )
                    },
                    bottomBar = {
                        if (!isExpanded && !isMedium) {
                            NavigationBar(
                                windowInsets = WindowInsets.navigationBars,
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                bottomNavItems.forEach { item ->
                                    val selected = currentDest == item.dest
                                    val label = AppStrings.navShortLabel(item.dest, lang)
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { viewModel.navigateTo(item.dest) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        modifier = Modifier.testTag("bottom_nav_${item.dest.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (isMedium) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))
                                allNavItems.forEach { item ->
                                    val selected = currentDest == item.dest
                                    val label = AppStrings.navShortLabel(item.dest, lang)
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { viewModel.navigateTo(item.dest) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = label
                                            )
                                        },
                                        label = { Text(label, maxLines = 1) },
                                        modifier = Modifier.testTag("rail_nav_${item.dest.name.lowercase()}")
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            AnimatedContent(
                                targetState = currentDest,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "screenTransition",
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .widthIn(max = 940.dp)
                                    .fillMaxWidth()
                            ) { dest ->
                                when (dest) {
                                    NavDestination.DASHBOARD -> DashboardScreen(
                                        converterState = converterState,
                                        preferences = preferences,
                                        recentHistory = allHistory,
                                        onInputChange = viewModel::updateInput,
                                        onSelectSource = viewModel::selectSourceSystem,
                                        onSelectTarget = viewModel::selectTargetSystem,
                                        onSwap = viewModel::swapSystems,
                                        onConvertAndOpen = {
                                            viewModel.convertAndSave()
                                            viewModel.navigateTo(NavDestination.CONVERTER)
                                        },
                                        onApplyPreset = { input, src, tgt ->
                                            viewModel.applyQuickPreset(input, src, tgt, openConverter = true)
                                        },
                                        onOpenLesson = viewModel::openLesson,
                                        onReuseHistory = viewModel::reuseHistoryItem,
                                        onNavigate = viewModel::navigateTo
                                    )

                                    NavDestination.CONVERTER -> ConverterScreen(
                                        state = converterState,
                                        preferences = preferences,
                                        onInputChange = viewModel::updateInput,
                                        onAppendChar = viewModel::appendCharacter,
                                        onBackspace = viewModel::backspaceInput,
                                        onClear = viewModel::clearInput,
                                        onSelectSource = viewModel::selectSourceSystem,
                                        onSelectTarget = viewModel::selectTargetSystem,
                                        onSwap = viewModel::swapSystems,
                                        onConvertAndSave = viewModel::convertAndSave,
                                        onSaveFavorite = viewModel::saveCurrentAsFavorite,
                                        onToggleRealTime = viewModel::setRealTimeConversion,
                                        onStepChange = viewModel::setExplanationStep,
                                        onToggleAllSteps = viewModel::toggleAllStepsExpanded,
                                        onToggleWhyItWorks = viewModel::toggleWhyItWorks,
                                        onShowToast = viewModel::showToast
                                    )

                                    NavDestination.LEARN -> LearnScreen(
                                        selectedSystem = selectedLessonSystem,
                                        language = lang,
                                        onSelectSystem = viewModel::selectLessonSystem,
                                        onTryInConverter = { input, src, tgt ->
                                            viewModel.applyQuickPreset(input, src, tgt, openConverter = true)
                                        },
                                        onStartQuiz = { viewModel.navigateTo(NavDestination.QUIZ) }
                                    )

                                    NavDestination.QUIZ -> QuizScreen(
                                        quizState = quizState,
                                        preferences = preferences,
                                        onSelectDifficulty = viewModel::setQuizDifficulty,
                                        onSelectMode = viewModel::setQuizMode,
                                        onSelectOption = viewModel::selectQuizOption,
                                        onTypedAnswerChange = viewModel::updateQuizTypedAnswer,
                                        onSubmit = viewModel::submitQuizAnswer,
                                        onNextQuestion = viewModel::nextQuizQuestion,
                                        onResetStats = {
                                            viewModel.resetQuizStats()
                                            viewModel.showToast(if (bn) "কুইজ পরিসংখ্যান রিসেট হয়েছে" else "Quiz stats reset")
                                        }
                                    )

                                    NavDestination.TABLE -> TableScreen(
                                        language = lang,
                                        onOpenInConverter = { input, src, tgt ->
                                            viewModel.applyQuickPreset(input, src, tgt, openConverter = true)
                                        },
                                        onShowToast = viewModel::showToast
                                    )

                                    NavDestination.HISTORY -> HistoryScreen(
                                        historyItems = filteredHistory,
                                        language = lang,
                                        searchQuery = searchQuery,
                                        onSearchChange = viewModel::updateSearchQuery,
                                        onReuse = viewModel::reuseHistoryItem,
                                        onToggleFavorite = viewModel::toggleFavorite,
                                        onDeleteItem = viewModel::deleteHistoryItem,
                                        onClearAll = viewModel::clearAllHistory,
                                        buildCsv = viewModel::buildCsvContent,
                                        onShowToast = viewModel::showToast
                                    )

                                    NavDestination.SETTINGS -> SettingsScreen(
                                        preferences = preferences,
                                        onThemeChange = viewModel::setThemeMode,
                                        onLanguageChange = viewModel::setLanguage,
                                        onRealTimeChange = viewModel::setRealTimeConversion,
                                        onGroupDigitsChange = viewModel::setGroupDigits,
                                        onSoundEffectsChange = viewModel::setSoundEffects,
                                        onClearHistory = viewModel::clearAllHistory,
                                        onResetPreferences = viewModel::resetAllPreferences
                                    )

                                    NavDestination.ABOUT -> AboutScreen(language = lang)
                                }
                            }
                        }
                    }
                }
            }

            if (isExpanded) {
                PermanentNavigationDrawer(
                    drawerContent = {
                        PermanentDrawerSheet(
                            modifier = Modifier.width(260.dp),
                            drawerContainerColor = MaterialTheme.colorScheme.surface
                        ) {
                            DrawerContent(
                                currentDest = currentDest,
                                language = lang,
                                onSelect = { viewModel.navigateTo(it) }
                            )
                        }
                    }
                ) {
                    mainScaffold()
                }
            } else {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = MaterialTheme.colorScheme.surface
                        ) {
                            DrawerContent(
                                currentDest = currentDest,
                                language = lang,
                                onSelect = { dest ->
                                    viewModel.navigateTo(dest)
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                ) {
                    mainScaffold()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumixTopBar(
    showMenuButton: Boolean,
    themeMode: ThemeMode,
    language: AppLanguage,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCycleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val ext = LocalNumixColors.current
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        ),
        navigationIcon = {
            if (showMenuButton) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("top_bar_menu_btn")
                ) {
                    Icon(Icons.Rounded.Menu, contentDescription = "Open navigation menu")
                }
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_numix_logo),
                    contentDescription = "NumiX logo",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Text(
                    text = "NumiX",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("top_bar_search_btn")
            ) {
                Icon(Icons.Rounded.Search, contentDescription = "Quick search")
            }
            Surface(
                onClick = onToggleLanguage,
                color = ext.binaryColor.copy(alpha = 0.14f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("top_bar_lang_toggle")
            ) {
                Text(
                    text = if (language == AppLanguage.BANGLA) "বাং" else "EN",
                    style = MaterialTheme.typography.labelMedium,
                    color = ext.binaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            IconButton(
                onClick = onCycleTheme,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("top_bar_theme_toggle")
            ) {
                val icon = when (themeMode) {
                    ThemeMode.DARK -> Icons.Rounded.DarkMode
                    ThemeMode.LIGHT -> Icons.Rounded.LightMode
                    ThemeMode.SYSTEM -> Icons.Rounded.SettingsBrightness
                }
                Icon(icon, contentDescription = "Switch theme")
            }
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("top_bar_settings_btn")
            ) {
                Icon(Icons.Rounded.Settings, contentDescription = "Settings")
            }
        }
    )
}

@Composable
private fun DrawerContent(
    currentDest: NavDestination,
    language: AppLanguage,
    onSelect: (NavDestination) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_numix_logo),
                contentDescription = "NumiX logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Column {
                Text("NumiX", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = if (language == AppLanguage.BANGLA) "সংখ্যা পদ্ধতি ও গণিত" else "Number Systems Studio",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        allNavItems.forEach { item ->
            val selected = currentDest == item.dest
            val label = AppStrings.navLabel(item.dest, language)
            NavigationDrawerItem(
                label = { Text(label) },
                selected = selected,
                onClick = { onSelect(item.dest) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = label
                    )
                },
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag("drawer_nav_${item.dest.name.lowercase()}")
            )
        }
    }
}

@Composable
private fun QuickSearchDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onConvertQuery: (String, NumberSystem, NumberSystem) -> Unit,
    onNavigate: (NavDestination) -> Unit
) {
    val bn = language == AppLanguage.BANGLA
    var query by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (bn) "দ্রুত অনুসন্ধান ও রূপান্তর" else "Quick Search & Jump") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(if (bn) "সংখ্যা লিখুন (যেমন 45, 0x2D, 101101)" else "Enter number (e.g. 45, 0x2D, 101101)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_search_input")
                )
                val trimmed = query.trim()
                if (trimmed.isNotEmpty()) {
                    val detectedSource = when {
                        trimmed.startsWith("0x", ignoreCase = true) -> NumberSystem.HEXADECIMAL
                        trimmed.startsWith("0b", ignoreCase = true) -> NumberSystem.BINARY
                        trimmed.startsWith("0o", ignoreCase = true) -> NumberSystem.OCTAL
                        trimmed.any { it.uppercaseChar() in 'A'..'F' } -> NumberSystem.HEXADECIMAL
                        trimmed.all { it == '0' || it == '1' || it == '-' } && trimmed.length >= 4 -> NumberSystem.BINARY
                        else -> NumberSystem.DECIMAL
                    }
                    val clean = trimmed.removePrefix("0x").removePrefix("0X")
                        .removePrefix("0b").removePrefix("0B")
                        .removePrefix("0o").removePrefix("0O")
                    val (_, conv) = NumberConverterEngine.convert(clean, detectedSource, NumberSystem.DECIMAL)
                    if (conv != null) {
                        Button(
                            onClick = {
                                val tgt = if (detectedSource == NumberSystem.DECIMAL) NumberSystem.BINARY else NumberSystem.DECIMAL
                                onConvertQuery(clean, detectedSource, tgt)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (bn) "কনভার্টারে খুলুন: ${conv.normalizedSource}${detectedSource.subscript}"
                                else "Convert ${conv.normalizedSource}${detectedSource.subscript} → All Bases"
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onNavigate(NavDestination.TABLE) }) {
                        Text(if (bn) "০–২৫৫ সারণি" else "0–255 Table")
                    }
                    TextButton(onClick = { onNavigate(NavDestination.LEARN) }) {
                        Text(if (bn) "পাঠশালা" else "Lessons")
                    }
                    TextButton(onClick = { onNavigate(NavDestination.HISTORY) }) {
                        Text(if (bn) "ইতিহাস" else "History")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (bn) "বন্ধ করুন" else "Close")
            }
        }
    )
}
