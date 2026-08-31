package com.example.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DashboardDateRange
import com.example.data.model.TransactionEntity
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopPayViewModel
import java.util.Locale

enum class MainTab(val title: String, val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector, val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    TRANSACTIONS("Payments", Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong),
    ANALYTICS("Analytics", Icons.Filled.Insights, Icons.Outlined.Insights),
    AI_ASSISTANT("AI Assistant", Icons.Filled.SmartToy, Icons.Outlined.SmartToy),
    REPORTS("Reports", Icons.Filled.Assessment, Icons.Outlined.Assessment),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainAppScreen(
    viewModel: ShopPayViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Navigation State
    var currentScreen by remember { mutableStateOf("MAIN") } // LANDING, AUTH, ONBOARDING, MAIN
    var currentTab by remember { mutableStateOf(MainTab.DASHBOARD) }

    // Dialog States
    var showAddEditDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var selectedDetailTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var showCsvImportDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    // Toast handler
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    when (currentScreen) {
        "LANDING" -> {
            LandingScreen(
                onGetStarted = { currentScreen = "AUTH" },
                onViewDemo = {
                    currentScreen = "MAIN"
                    currentTab = MainTab.DASHBOARD
                }
            )
        }
        "AUTH" -> {
            AuthScreen(
                onAuthSuccess = { currentScreen = "ONBOARDING" },
                onFastDemoLogin = {
                    currentScreen = "MAIN"
                    currentTab = MainTab.DASHBOARD
                },
                onBackToLanding = { currentScreen = "LANDING" }
            )
        }
        "ONBOARDING" -> {
            OnboardingScreen(
                currentProfile = state.profile,
                onCompleteOnboarding = { updatedProfile ->
                    viewModel.saveShopProfile(updatedProfile)
                    currentScreen = "MAIN"
                    currentTab = MainTab.DASHBOARD
                }
            )
        }
        "MAIN" -> {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = DarkBg,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .testTag("bottom_nav_bar")
                    ) {
                        MainTab.entries.forEach { tab ->
                            val isSelected = currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ElegantLilac,
                                    selectedTextColor = ElegantLilac,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted,
                                    indicatorColor = ElegantPurpleDark
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        MainTab.DASHBOARD -> {
                            DashboardScreen(
                                state = state,
                                onDateRangeSelected = { viewModel.setDashboardDateRange(it) },
                                onPeriodSelected = { viewModel.setChartDays(it) },
                                onPeriodTypeChange = { viewModel.setChartPeriodType(it) },
                                onMonthsSelected = { viewModel.setChartMonths(it) },
                                onChartStyleChange = { viewModel.setChartStyle(it) },
                                onChartMetricChange = { viewModel.setChartMetric(it) },
                                onAddTransactionClick = {
                                    transactionToEdit = null
                                    showAddEditDialog = true
                                },
                                onImportCsvClick = {
                                    viewModel.clearImportValidation()
                                    showCsvImportDialog = true
                                },
                                onViewAllTransactions = {
                                    currentTab = MainTab.TRANSACTIONS
                                },
                                onTransactionClick = { txn ->
                                    selectedDetailTransaction = txn
                                },
                                onOpenAiAssistant = {
                                    currentTab = MainTab.AI_ASSISTANT
                                },
                                onOpenNotifications = {
                                    showNotificationsDialog = true
                                }
                            )
                        }
                        MainTab.TRANSACTIONS -> {
                            TransactionsScreen(
                                state = state,
                                onSearchChange = { viewModel.updateSearchQuery(it) },
                                onPaymentFilterChange = { viewModel.setPaymentMethodFilter(it) },
                                onStatusFilterChange = { viewModel.setStatusFilter(it) },
                                onUnusualFilterToggle = { viewModel.setOnlyUnusualFilter(it) },
                                onSortChange = { viewModel.setSortOrder(it) },
                                onAddTransactionClick = {
                                    transactionToEdit = null
                                    showAddEditDialog = true
                                },
                                onImportCsvClick = {
                                    viewModel.clearImportValidation()
                                    showCsvImportDialog = true
                                },
                                onExportCsvClick = {
                                    val csv = viewModel.getExportCsvString()
                                    clipboardManager.setText(AnnotatedString(csv))
                                    viewModel.showToast("CSV copied to clipboard (${state.allTransactions.size} rows)")
                                },
                                onTransactionClick = { txn ->
                                    selectedDetailTransaction = txn
                                }
                            )
                        }
                        MainTab.ANALYTICS -> {
                            AnalyticsScreen(
                                state = state,
                                onPeriodSelected = { viewModel.setChartDays(it) },
                                onPeriodTypeChange = { viewModel.setChartPeriodType(it) },
                                onMonthsSelected = { viewModel.setChartMonths(it) },
                                onChartStyleChange = { viewModel.setChartStyle(it) },
                                onChartMetricChange = { viewModel.setChartMetric(it) },
                                onViewUnusualTransactions = {
                                    viewModel.setOnlyUnusualFilter(true)
                                    currentTab = MainTab.TRANSACTIONS
                                }
                            )
                        }
                        MainTab.AI_ASSISTANT -> {
                            AiAssistantScreen(
                                state = state,
                                onSendMessage = { viewModel.askAiAssistant(it) }
                            )
                        }
                        MainTab.REPORTS -> {
                            ReportsScreen(
                                state = state,
                                onExportCsvClick = {
                                    val csv = viewModel.getExportCsvString()
                                    clipboardManager.setText(AnnotatedString(csv))
                                    viewModel.showToast("Financial Statement CSV copied to clipboard!")
                                },
                                onShareReport = {
                                    val curr = state.profile.currency
                                    val shareText = """
                                        📊 ${state.profile.shopName} - Business Intelligence Report
                                        Owner: ${state.profile.ownerName}
                                        Today's Income: $curr${String.format(Locale.US, "%,.2f", state.metrics.todayIncome)}
                                        Monthly Revenue: $curr${String.format(Locale.US, "%,.2f", state.metrics.monthlyRevenue)} (${state.metrics.targetProgressPercent}% of target)
                                        Dominant Channel: ${state.analytics.dominantPaymentMethod} (${state.analytics.dominantPaymentPercent}%)
                                        Highest Revenue Day: ${state.analytics.highestRevenueDay}
                                        Generated by ShopPay AI.
                                    """.trimIndent()

                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share Business Report")
                                    context.startActivity(shareIntent)
                                }
                            )
                        }
                        MainTab.SETTINGS -> {
                            SettingsScreen(
                                currentProfile = state.profile,
                                onSaveProfile = { viewModel.saveShopProfile(it) },
                                onResetDemoData = { viewModel.resetDemoData() },
                                onClearAllData = { viewModel.clearAllData() },
                                onExportCsv = {
                                    val csv = viewModel.getExportCsvString()
                                    clipboardManager.setText(AnnotatedString(csv))
                                    viewModel.showToast("Exported statement copied to clipboard!")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (showAddEditDialog) {
        AddEditTransactionDialog(
            transactionToEdit = transactionToEdit,
            currency = state.profile.currency,
            onDismiss = {
                showAddEditDialog = false
                transactionToEdit = null
            },
            onSave = { txn ->
                if (transactionToEdit != null) {
                    viewModel.updateTransaction(txn)
                    showAddEditDialog = false
                    transactionToEdit = null
                } else {
                    viewModel.addTransaction(
                        txn = txn,
                        onSuccess = {
                            showAddEditDialog = false
                            transactionToEdit = null
                        },
                        onError = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            },
            onDelete = { txn ->
                viewModel.deleteTransaction(txn.id)
                showAddEditDialog = false
                transactionToEdit = null
            }
        )
    }

    if (showCsvImportDialog) {
        CsvImportDialog(
            validationResult = state.importValidationResult,
            onValidateCsv = { csvString ->
                viewModel.validateCsvContent(csvString)
            },
            onConfirmImport = { transactions ->
                viewModel.confirmCsvImport(transactions) {
                    showCsvImportDialog = false
                }
            },
            onDismiss = {
                showCsvImportDialog = false
                viewModel.clearImportValidation()
            }
        )
    }

    if (selectedDetailTransaction != null) {
        TransactionDetailDialog(
            txn = selectedDetailTransaction!!,
            currency = state.profile.currency,
            onDismiss = { selectedDetailTransaction = null },
            onEdit = {
                transactionToEdit = selectedDetailTransaction
                selectedDetailTransaction = null
                showAddEditDialog = true
            },
            onDelete = {
                selectedDetailTransaction?.let { viewModel.deleteTransaction(it.id) }
                selectedDetailTransaction = null
            }
        )
    }

    if (showNotificationsDialog) {
        NotificationsDialog(
            notifications = state.notifications,
            onDismiss = { showNotificationsDialog = false }
        )
    }
}
