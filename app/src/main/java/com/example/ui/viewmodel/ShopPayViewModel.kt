package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.model.*
import com.example.data.repository.ShopPayRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class NotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: String,
    val type: String = "INFO" // INFO, SUCCESS, WARNING
)

data class ShopPayUiState(
    val isLoading: Boolean = true,
    val profile: ShopProfile = ShopProfile(),
    val selectedDateRange: DashboardDateRange = DashboardDateRange.THIS_MONTH,
    val allTransactions: List<TransactionEntity> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val metrics: DashboardMetrics = DashboardMetrics(),
    val analytics: BusinessAnalyticsSummary = BusinessAnalyticsSummary(
        dailyAverage = 0.0, weeklyAverage = 0.0, monthlyAverage = 0.0,
        highestRevenueDay = "-", highestRevenueAmount = 0.0,
        lowestRevenueDay = "-", lowestRevenueAmount = 0.0,
        highestTransactionAmount = 0.0, averageTransactionValue = 0.0,
        totalTransactions = 0, totalRevenue = 0.0,
        weekendAverage = 0.0, weekdayAverage = 0.0,
        dominantPaymentMethod = "UPI", dominantPaymentPercent = 60
    ),
    val dailyRevenuePoints: List<DailyRevenuePoint> = emptyList(),
    val monthlyRevenuePoints: List<MonthlyRevenuePoint> = emptyList(),
    val chartPeriodType: String = "DAILY", // "DAILY", "MONTHLY"
    val chartDays: Int = 30, // 7, 14, 30, 90
    val chartMonths: Int = 12, // 3, 6, 12
    val chartStyle: String = "AREA", // "AREA", "BAR"
    val chartMetric: String = "REVENUE", // "REVENUE", "COUNT", "BREAKDOWN"
    val aiInsights: List<AiInsightEntity> = emptyList(),
    val unusualTransactions: List<TransactionEntity> = emptyList(),
    val forecast: IncomeForecast = IncomeForecast(
        expectedTomorrowMin = 11500.0, expectedTomorrowMax = 13200.0,
        expectedNext7DaysMin = 82000.0, expectedNext7DaysMax = 94000.0,
        trendPercentage = 8.4, confidenceNote = "Based on your 14-day transaction trend."
    ),
    val searchQuery: String = "",
    val filterPaymentMethod: String? = null,
    val filterStatus: String? = null,
    val filterCategory: String? = null,
    val filterOnlyUnusual: Boolean = false,
    val sortOrder: String = "DATE_DESC",
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            isUser = false,
            text = "Namaste! I'm your ShopPay AI financial assistant 🤖. Ask me anything about your digital collections, best days, customer payment methods, or revenue targets."
        )
    ),
    val isAiThinking: Boolean = false,
    val importValidationResult: ImportValidationResult? = null,
    val notifications: List<NotificationItem> = listOf(
        NotificationItem(title = "Payment Received", message = "₹850 received via UPI QR Scan.", timestamp = "10:42 AM", type = "SUCCESS"),
        NotificationItem(title = "Milestone Reached", message = "Today's income crossed ₹10,000.", timestamp = "12:30 PM", type = "INFO"),
        NotificationItem(title = "Weekend Notice", message = "Weekend revenue is trending 28% higher than weekdays.", timestamp = "Yesterday", type = "INFO")
    ),
    val toastMessage: String? = null
)

class ShopPayViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ShopPayRepository(application)
    private val aiService = GeminiAiService()

    private val _uiState = MutableStateFlow(ShopPayUiState())
    val uiState: StateFlow<ShopPayUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeIfEmpty()
            observeData()
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.shopProfile,
                repository.getAllTransactions(),
                repository.getAiInsights(),
                repository.getUnusualTransactions()
            ) { profile, transactions, insights, unusual ->
                val currProfile = profile ?: ShopProfile()
                val metrics = repository.calculateDashboardMetrics(transactions, currProfile, _uiState.value.selectedDateRange)
                val analytics = repository.calculateBusinessAnalytics(transactions)
                val dailyPoints = repository.getDailyRevenuePoints(transactions, _uiState.value.chartDays)
                val monthlyPoints = repository.getMonthlyRevenuePoints(transactions, _uiState.value.chartMonths)
                val forecast = repository.generateIncomeForecast(transactions)
                
                val filtered = applyFilters(
                    transactions = transactions,
                    query = _uiState.value.searchQuery,
                    payment = _uiState.value.filterPaymentMethod,
                    status = _uiState.value.filterStatus,
                    category = _uiState.value.filterCategory,
                    onlyUnusual = _uiState.value.filterOnlyUnusual,
                    sort = _uiState.value.sortOrder
                )

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        profile = currProfile,
                        allTransactions = transactions,
                        recentTransactions = transactions.take(10),
                        filteredTransactions = filtered,
                        metrics = metrics,
                        analytics = analytics,
                        dailyRevenuePoints = dailyPoints,
                        monthlyRevenuePoints = monthlyPoints,
                        aiInsights = insights,
                        unusualTransactions = unusual,
                        forecast = forecast
                    )
                }
            }.collect()
        }
    }

    fun setDashboardDateRange(range: DashboardDateRange) {
        viewModelScope.launch {
            val metrics = repository.calculateDashboardMetrics(
                _uiState.value.allTransactions,
                _uiState.value.profile,
                range
            )
            val chartDays = when (range) {
                DashboardDateRange.TODAY -> 7
                DashboardDateRange.THIS_WEEK -> 7
                DashboardDateRange.THIS_MONTH -> 30
            }
            val dailyPoints = repository.getDailyRevenuePoints(_uiState.value.allTransactions, chartDays)
            _uiState.update {
                it.copy(
                    selectedDateRange = range,
                    metrics = metrics,
                    chartDays = chartDays,
                    dailyRevenuePoints = dailyPoints
                )
            }
        }
    }

    fun setChartPeriodType(type: String) {
        _uiState.update { it.copy(chartPeriodType = type) }
    }

    fun setChartDays(days: Int) {
        viewModelScope.launch {
            val points = repository.getDailyRevenuePoints(_uiState.value.allTransactions, days)
            _uiState.update { it.copy(chartDays = days, dailyRevenuePoints = points) }
        }
    }

    fun setChartMonths(months: Int) {
        viewModelScope.launch {
            val points = repository.getMonthlyRevenuePoints(_uiState.value.allTransactions, months)
            _uiState.update { it.copy(chartMonths = months, monthlyRevenuePoints = points) }
        }
    }

    fun setChartStyle(style: String) {
        _uiState.update { it.copy(chartStyle = style) }
    }

    fun setChartMetric(metric: String) {
        _uiState.update { it.copy(chartMetric = metric) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            val filtered = applyFilters(
                transactions = state.allTransactions,
                query = query,
                payment = state.filterPaymentMethod,
                status = state.filterStatus,
                category = state.filterCategory,
                onlyUnusual = state.filterOnlyUnusual,
                sort = state.sortOrder
            )
            state.copy(searchQuery = query, filteredTransactions = filtered)
        }
    }

    fun setPaymentMethodFilter(method: String?) {
        _uiState.update { state ->
            val filtered = applyFilters(
                transactions = state.allTransactions,
                query = state.searchQuery,
                payment = method,
                status = state.filterStatus,
                category = state.filterCategory,
                onlyUnusual = state.filterOnlyUnusual,
                sort = state.sortOrder
            )
            state.copy(filterPaymentMethod = method, filteredTransactions = filtered)
        }
    }

    fun setStatusFilter(status: String?) {
        _uiState.update { state ->
            val filtered = applyFilters(
                transactions = state.allTransactions,
                query = state.searchQuery,
                payment = state.filterPaymentMethod,
                status = status,
                category = state.filterCategory,
                onlyUnusual = state.filterOnlyUnusual,
                sort = state.sortOrder
            )
            state.copy(filterStatus = status, filteredTransactions = filtered)
        }
    }

    fun setCategoryFilter(category: String?) {
        _uiState.update { state ->
            val filtered = applyFilters(
                transactions = state.allTransactions,
                query = state.searchQuery,
                payment = state.filterPaymentMethod,
                status = state.filterStatus,
                category = category,
                onlyUnusual = state.filterOnlyUnusual,
                sort = state.sortOrder
            )
            state.copy(filterCategory = category, filteredTransactions = filtered)
        }
    }

    fun setOnlyUnusualFilter(onlyUnusual: Boolean) {
        _uiState.update { state ->
            val filtered = applyFilters(
                transactions = state.allTransactions,
                query = state.searchQuery,
                payment = state.filterPaymentMethod,
                status = state.filterStatus,
                category = state.filterCategory,
                onlyUnusual = onlyUnusual,
                sort = state.sortOrder
            )
            state.copy(filterOnlyUnusual = onlyUnusual, filteredTransactions = filtered)
        }
    }

    fun setSortOrder(sort: String) {
        _uiState.update { state ->
            val filtered = applyFilters(
                transactions = state.allTransactions,
                query = state.searchQuery,
                payment = state.filterPaymentMethod,
                status = state.filterStatus,
                category = state.filterCategory,
                onlyUnusual = state.filterOnlyUnusual,
                sort = sort
            )
            state.copy(sortOrder = sort, filteredTransactions = filtered)
        }
    }

    private fun applyFilters(
        transactions: List<TransactionEntity>,
        query: String,
        payment: String?,
        status: String?,
        category: String?,
        onlyUnusual: Boolean,
        sort: String
    ): List<TransactionEntity> {
        var list = transactions.asSequence()

        if (query.isNotBlank()) {
            val q = query.lowercase().trim()
            list = list.filter {
                it.customerName.lowercase().contains(q) ||
                it.transactionId.lowercase().contains(q) ||
                it.customerNote.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.amount.toString().contains(q)
            }
        }

        if (!payment.isNullOrBlank()) {
            list = list.filter { it.paymentMethod.equals(payment, ignoreCase = true) }
        }

        if (!status.isNullOrBlank()) {
            list = list.filter { it.status.equals(status, ignoreCase = true) }
        }

        if (!category.isNullOrBlank()) {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }

        if (onlyUnusual) {
            list = list.filter { it.isUnusual }
        }

        val sorted = when (sort) {
            "DATE_ASC" -> list.sortedWith(compareBy<TransactionEntity> { it.transactionDate }.thenBy { it.transactionTime })
            "AMOUNT_DESC" -> list.sortedByDescending { it.amount }
            "AMOUNT_ASC" -> list.sortedBy { it.amount }
            else -> list.sortedWith(compareByDescending<TransactionEntity> { it.transactionDate }.thenByDescending { it.transactionTime })
        }

        return sorted.toList()
    }

    fun addTransaction(
        txn: TransactionEntity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.addTransaction(txn)
            if (result.isSuccess) {
                showToast("Transaction ${txn.transactionId} added successfully!")
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to add transaction")
            }
        }
    }

    fun updateTransaction(txn: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(txn)
            showToast("Transaction ${txn.transactionId} updated.")
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id, _uiState.value.profile.id)
            showToast("Transaction deleted.")
        }
    }

    fun validateCsvContent(csvString: String) {
        viewModelScope.launch {
            val result = repository.parseAndValidateCsv(csvString, _uiState.value.profile.id)
            _uiState.update { it.copy(importValidationResult = result) }
        }
    }

    fun clearImportValidation() {
        _uiState.update { it.copy(importValidationResult = null) }
    }

    fun confirmCsvImport(transactions: List<TransactionEntity>, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.importValidatedTransactions(transactions)
            _uiState.update { it.copy(importValidationResult = null) }
            showToast("Successfully imported ${transactions.size} transactions!")
            onDone()
        }
    }

    fun askAiAssistant(question: String) {
        if (question.isBlank()) return
        val userMsg = ChatMessage(isUser = true, text = question)
        _uiState.update { 
            it.copy(
                chatMessages = it.chatMessages + userMsg,
                isAiThinking = true
            )
        }

        viewModelScope.launch {
            val state = _uiState.value
            val reply = aiService.askAssistant(
                userPrompt = question,
                profile = state.profile,
                metrics = state.metrics,
                analytics = state.analytics,
                forecast = state.forecast
            )

            val aiMsg = ChatMessage(
                isUser = false,
                text = reply,
                suggestedActions = listOf(
                    "Show peak business days",
                    "How to reach monthly target?",
                    "Check payment breakdown"
                )
            )

            _uiState.update { 
                it.copy(
                    chatMessages = it.chatMessages + aiMsg,
                    isAiThinking = false
                )
            }
        }
    }

    fun saveShopProfile(profile: ShopProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
            showToast("Shop profile saved successfully!")
        }
    }

    fun updateMonthlyTarget(target: Double) {
        viewModelScope.launch {
            repository.updateMonthlyTarget(_uiState.value.profile.id, target)
            showToast("Monthly target updated to ${_uiState.value.profile.currency}${String.format(Locale.US, "%,.0f", target)}")
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.seedDemoData()
            showToast("Reset to full demo dataset (120+ transactions).")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData(_uiState.value.profile.id)
            showToast("All shop transactions cleared.")
        }
    }

    fun getExportCsvString(): String {
        return repository.exportTransactionsCsv(_uiState.value.allTransactions)
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
