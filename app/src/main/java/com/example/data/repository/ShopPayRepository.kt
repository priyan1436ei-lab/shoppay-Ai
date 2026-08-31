package com.example.data.repository

import android.content.Context
import com.example.data.local.ShopPayDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class ShopPayRepository(context: Context) {
    private val db = ShopPayDatabase.getDatabase(context)
    private val transactionDao = db.transactionDao()
    private val shopProfileDao = db.shopProfileDao()
    private val aiInsightDao = db.aiInsightDao()

    val shopProfile: Flow<ShopProfile?> = shopProfileDao.getActiveProfile()

    fun getAllTransactions(shopId: String = "shop_default"): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions(shopId)

    fun getRecentTransactions(shopId: String = "shop_default", limit: Int = 10): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions(shopId, limit)

    fun getDailyTransactionsFlow(shopId: String = "shop_default", date: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForDateFlow(shopId, date)

    fun getTransactionsByCategoryFlow(shopId: String = "shop_default", category: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByCategory(shopId, category)

    suspend fun getDailyTotal(shopId: String = "shop_default", date: String): Double = withContext(Dispatchers.IO) {
        transactionDao.getDailyTotalAmount(shopId, date) ?: 0.0
    }

    fun getAiInsights(shopId: String = "shop_default"): Flow<List<AiInsightEntity>> =
        aiInsightDao.getInsights(shopId)

    fun getUnusualTransactions(shopId: String = "shop_default"): Flow<List<TransactionEntity>> =
        transactionDao.getUnusualTransactions(shopId)

    suspend fun initializeIfEmpty() = withContext(Dispatchers.IO) {
        val profile = shopProfileDao.getActiveProfileDirect()
        if (profile == null) {
            seedDemoData()
        }
    }

    suspend fun saveProfile(profile: ShopProfile) = withContext(Dispatchers.IO) {
        shopProfileDao.saveProfile(profile)
        refreshAiInsights(profile.id)
    }

    suspend fun updateMonthlyTarget(shopId: String, target: Double) = withContext(Dispatchers.IO) {
        shopProfileDao.updateMonthlyTarget(shopId, target)
        refreshAiInsights(shopId)
    }

    suspend fun addTransaction(transaction: TransactionEntity): Result<Long> = withContext(Dispatchers.IO) {
        // Validate
        if (transaction.amount <= 0) {
            return@withContext Result.failure(IllegalArgumentException("Amount must be greater than 0"))
        }
        val existing = transactionDao.findByTransactionId(transaction.shopId, transaction.transactionId)
        if (existing != null) {
            return@withContext Result.failure(IllegalStateException("Transaction ID ${transaction.transactionId} already exists"))
        }
        val id = transactionDao.insertTransaction(transaction)
        refreshAiInsights(transaction.shopId)
        Result.success(id)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction)
        refreshAiInsights(transaction.shopId)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction)
        refreshAiInsights(transaction.shopId)
    }

    suspend fun deleteTransactionById(id: Long, shopId: String = "shop_default") = withContext(Dispatchers.IO) {
        transactionDao.deleteById(id)
        refreshAiInsights(shopId)
    }

    suspend fun calculatePeriodMetrics(
        transactions: List<TransactionEntity>,
        profile: ShopProfile,
        dateRange: DashboardDateRange = DashboardDateRange.THIS_MONTH
    ): PeriodRevenueMetrics = withContext(Dispatchers.Default) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayDate = Date()
        val todayStr = dateFormat.format(todayDate)
        val successful = transactions.filter { it.status == TransactionStatus.SUCCESSFUL.name }

        when (dateRange) {
            DashboardDateRange.TODAY -> {
                val calYesterday = Calendar.getInstance()
                calYesterday.add(Calendar.DAY_OF_YEAR, -1)
                val yesterdayStr = dateFormat.format(calYesterday.time)

                val periodTxns = successful.filter { it.transactionDate == todayStr }
                val prevTxns = successful.filter { it.transactionDate == yesterdayStr }

                val periodRevenue = periodTxns.sumOf { it.amount }
                val prevRevenue = prevTxns.sumOf { it.amount }
                val growthPercent = if (prevRevenue > 0) {
                    ((periodRevenue - prevRevenue) / prevRevenue) * 100.0
                } else if (periodRevenue > 0) 100.0 else 0.0

                val target = (profile.monthlyRevenueTarget / 30.0).coerceAtLeast(1.0)
                val targetProgressPercent = ((periodRevenue / target) * 100).roundToInt()
                val targetRemaining = (target - periodRevenue).coerceAtLeast(0.0)

                val totalAmt = periodRevenue.coerceAtLeast(1.0)
                val upiTotal = periodTxns.filter { it.paymentMethod.equals(PaymentMethod.UPI.name, true) }.sumOf { it.amount }
                val cardTotal = periodTxns.filter { it.paymentMethod.equals(PaymentMethod.CARD.name, true) }.sumOf { it.amount }
                val bankTotal = periodTxns.filter { it.paymentMethod.equals(PaymentMethod.BANK_TRANSFER.name, true) }.sumOf { it.amount }
                val walletTotal = periodTxns.filter { it.paymentMethod.equals(PaymentMethod.WALLET.name, true) }.sumOf { it.amount }
                val cashTotal = periodTxns.filter { it.paymentMethod.equals(PaymentMethod.CASH.name, true) }.sumOf { it.amount }

                val readableDate = SimpleDateFormat("d MMM yyyy", Locale.US).format(todayDate)

                PeriodRevenueMetrics(
                    dateRange = DashboardDateRange.TODAY,
                    revenue = periodRevenue,
                    previousPeriodRevenue = prevRevenue,
                    growthPercent = growthPercent,
                    transactionCount = periodTxns.size,
                    averageTicket = if (periodTxns.isNotEmpty()) periodRevenue / periodTxns.size else 0.0,
                    periodTarget = target,
                    targetProgressPercent = targetProgressPercent,
                    targetRemaining = targetRemaining,
                    upiTotal = upiTotal,
                    upiPercent = ((upiTotal / totalAmt) * 100).roundToInt(),
                    cardTotal = cardTotal,
                    cardPercent = ((cardTotal / totalAmt) * 100).roundToInt(),
                    bankTotal = bankTotal,
                    bankPercent = ((bankTotal / totalAmt) * 100).roundToInt(),
                    walletTotal = walletTotal,
                    walletPercent = ((walletTotal / totalAmt) * 100).roundToInt(),
                    cashTotal = cashTotal,
                    cashPercent = ((cashTotal / totalAmt) * 100).roundToInt(),
                    periodTitle = "TODAY'S REVENUE",
                    comparisonLabel = "vs yesterday",
                    dateRangeSubtitle = "Today ($readableDate)"
                )
            }
            DashboardDateRange.THIS_WEEK -> {
                // Last 7 days including today
                val weekDates = mutableSetOf<String>()
                val cal = Calendar.getInstance()
                for (d in 0 until 7) {
                    weekDates.add(dateFormat.format(cal.time))
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                }

                // Previous 7 days
                val prevWeekDates = mutableSetOf<String>()
                for (d in 0 until 7) {
                    prevWeekDates.add(dateFormat.format(cal.time))
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                }

                val periodTxns = successful.filter { it.transactionDate in weekDates }
                val prevTxns = successful.filter { it.transactionDate in prevWeekDates }

                val periodRevenue = periodTxns.sumOf { it.amount }
                val prevRevenue = prevTxns.sumOf { it.amount }
                val growthPercent = if (prevRevenue > 0) {
                    ((periodRevenue - prevRevenue) / prevRevenue) * 100.0
                } else if (periodRevenue > 0) 100.0 else 0.0

                val target = (profile.monthlyRevenueTarget / 4.0).coerceAtLeast(1.0)
                val targetProgressPercent = ((periodRevenue / target) * 100).roundToInt()
                val targetRemaining = (target - periodRevenue).coerceAtLeast(0.0)

                val totalAmt = periodRevenue.coerceAtLeast(1.0)
                val upiTotal = periodTxns.filter { it.paymentMethod.equals(PaymentMethod.UPI.name, true) }.sumOf { it.amount }
                val cardTotal = periodTxns.filter { it.paymentMethod.equals(PaymentMethod.CARD.name, true) }.sumOf { it.amount }
                val bankTotal = periodTxns.filter { it.paymentMethod.equals(PaymentMethod.BANK_TRANSFER.name, true) }.sumOf { it.amount }
                val walletTotal = periodTxns.filter { it.paymentMethod.equals(PaymentMethod.WALLET.name, true) }.sumOf { it.amount }
                val cashTotal = periodTxns.filter { it.paymentMethod.equals(PaymentMethod.CASH.name, true) }.sumOf { it.amount }

                PeriodRevenueMetrics(
                    dateRange = DashboardDateRange.THIS_WEEK,
                    revenue = periodRevenue,
                    previousPeriodRevenue = prevRevenue,
                    growthPercent = growthPercent,
                    transactionCount = periodTxns.size,
                    averageTicket = if (periodTxns.isNotEmpty()) periodRevenue / periodTxns.size else 0.0,
                    periodTarget = target,
                    targetProgressPercent = targetProgressPercent,
                    targetRemaining = targetRemaining,
                    upiTotal = upiTotal,
                    upiPercent = ((upiTotal / totalAmt) * 100).roundToInt(),
                    cardTotal = cardTotal,
                    cardPercent = ((cardTotal / totalAmt) * 100).roundToInt(),
                    bankTotal = bankTotal,
                    bankPercent = ((bankTotal / totalAmt) * 100).roundToInt(),
                    walletTotal = walletTotal,
                    walletPercent = ((walletTotal / totalAmt) * 100).roundToInt(),
                    cashTotal = cashTotal,
                    cashPercent = ((cashTotal / totalAmt) * 100).roundToInt(),
                    periodTitle = "THIS WEEK'S REVENUE",
                    comparisonLabel = "vs prior 7 days",
                    dateRangeSubtitle = "Last 7 Days (Rolling Week)"
                )
            }
            DashboardDateRange.THIS_MONTH -> {
                val currentMonthPrefix = todayStr.substring(0, 7) // "YYYY-MM"
                
                // Previous month prefix
                val calPrevMonth = Calendar.getInstance()
                calPrevMonth.add(Calendar.MONTH, -1)
                val prevMonthPrefix = dateFormat.format(calPrevMonth.time).substring(0, 7)

                val periodTxns = successful.filter { it.transactionDate.startsWith(currentMonthPrefix) }
                val prevTxns = successful.filter { it.transactionDate.startsWith(prevMonthPrefix) }

                val periodRevenue = periodTxns.sumOf { it.amount }
                val prevRevenue = prevTxns.sumOf { it.amount }
                val growthPercent = if (prevRevenue > 0) {
                    ((periodRevenue - prevRevenue) / prevRevenue) * 100.0
                } else if (periodRevenue > 0) 100.0 else 0.0

                val target = profile.monthlyRevenueTarget.coerceAtLeast(1.0)
                val targetProgressPercent = if (target > 0) ((periodRevenue / target) * 100).roundToInt() else 0
                val targetRemaining = (target - periodRevenue).coerceAtLeast(0.0)

                val totalAmt = periodRevenue.coerceAtLeast(1.0)
                val upiTotal = periodTxns.filter { it.paymentMethod.equals(PaymentMethod.UPI.name, true) }.sumOf { it.amount }
                val cardTotal = periodTxns.filter { it.paymentMethod.equals(PaymentMethod.CARD.name, true) }.sumOf { it.amount }
                val bankTotal = periodTxns.filter { it.paymentMethod.equals(PaymentMethod.BANK_TRANSFER.name, true) }.sumOf { it.amount }
                val walletTotal = periodTxns.filter { it.paymentMethod.equals(PaymentMethod.WALLET.name, true) }.sumOf { it.amount }
                val cashTotal = periodTxns.filter { it.paymentMethod.equals(PaymentMethod.CASH.name, true) }.sumOf { it.amount }

                val monthDisplay = SimpleDateFormat("MMMM yyyy", Locale.US).format(todayDate)

                PeriodRevenueMetrics(
                    dateRange = DashboardDateRange.THIS_MONTH,
                    revenue = periodRevenue,
                    previousPeriodRevenue = prevRevenue,
                    growthPercent = growthPercent,
                    transactionCount = periodTxns.size,
                    averageTicket = if (periodTxns.isNotEmpty()) periodRevenue / periodTxns.size else 0.0,
                    periodTarget = target,
                    targetProgressPercent = targetProgressPercent,
                    targetRemaining = targetRemaining,
                    upiTotal = upiTotal,
                    upiPercent = ((upiTotal / totalAmt) * 100).roundToInt(),
                    cardTotal = cardTotal,
                    cardPercent = ((cardTotal / totalAmt) * 100).roundToInt(),
                    bankTotal = bankTotal,
                    bankPercent = ((bankTotal / totalAmt) * 100).roundToInt(),
                    walletTotal = walletTotal,
                    walletPercent = ((walletTotal / totalAmt) * 100).roundToInt(),
                    cashTotal = cashTotal,
                    cashPercent = ((cashTotal / totalAmt) * 100).roundToInt(),
                    periodTitle = "TOTAL MONTHLY REVENUE",
                    comparisonLabel = "vs last month",
                    dateRangeSubtitle = "Current Month ($monthDisplay)"
                )
            }
        }
    }

    suspend fun calculateDashboardMetrics(
        transactions: List<TransactionEntity>,
        profile: ShopProfile,
        selectedDateRange: DashboardDateRange = DashboardDateRange.THIS_MONTH
    ): DashboardMetrics = withContext(Dispatchers.Default) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = dateFormat.format(Date())
        
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(cal.time)

        val successful = transactions.filter { it.status == TransactionStatus.SUCCESSFUL.name }
        
        val todayTxns = successful.filter { it.transactionDate == todayStr }
        val yesterdayTxns = successful.filter { it.transactionDate == yesterdayStr }

        val todayIncome = todayTxns.sumOf { it.amount }
        val yesterdayIncome = yesterdayTxns.sumOf { it.amount }

        val todayGrowthPercent = if (yesterdayIncome > 0) {
            ((todayIncome - yesterdayIncome) / yesterdayIncome) * 100.0
        } else if (todayIncome > 0) 100.0 else 0.0

        val todayCount = todayTxns.size
        val avgTxn = if (successful.isNotEmpty()) successful.sumOf { it.amount } / successful.size else 0.0

        // Current Month calculation
        val currentMonthPrefix = todayStr.substring(0, 7) // "YYYY-MM"
        val monthlyTxns = successful.filter { it.transactionDate.startsWith(currentMonthPrefix) }
        val monthlyRevenue = monthlyTxns.sumOf { it.amount }

        val target = profile.monthlyRevenueTarget
        val targetProgressPercent = if (target > 0) ((monthlyRevenue / target) * 100).roundToInt() else 0
        val targetRemaining = (target - monthlyRevenue).coerceAtLeast(0.0)

        // Payment method breakdown
        val totalAmount = successful.sumOf { it.amount }.coerceAtLeast(1.0)
        val upiTotal = successful.filter { it.paymentMethod.equals(PaymentMethod.UPI.name, true) }.sumOf { it.amount }
        val cardTotal = successful.filter { it.paymentMethod.equals(PaymentMethod.CARD.name, true) }.sumOf { it.amount }
        val bankTotal = successful.filter { it.paymentMethod.equals(PaymentMethod.BANK_TRANSFER.name, true) }.sumOf { it.amount }
        val walletTotal = successful.filter { it.paymentMethod.equals(PaymentMethod.WALLET.name, true) }.sumOf { it.amount }
        val cashTotal = successful.filter { it.paymentMethod.equals(PaymentMethod.CASH.name, true) }.sumOf { it.amount }

        val unusualCount = transactions.count { it.isUnusual }

        val periodMetrics = calculatePeriodMetrics(transactions, profile, selectedDateRange)

        DashboardMetrics(
            todayIncome = todayIncome,
            yesterdayIncome = yesterdayIncome,
            todayGrowthPercent = todayGrowthPercent,
            todayTransactionCount = todayCount,
            averageTransactionValue = avgTxn,
            monthlyRevenue = monthlyRevenue,
            monthlyTarget = target,
            targetProgressPercent = targetProgressPercent,
            targetRemaining = targetRemaining,
            upiTotal = upiTotal,
            upiPercent = ((upiTotal / totalAmount) * 100).roundToInt(),
            cardTotal = cardTotal,
            cardPercent = ((cardTotal / totalAmount) * 100).roundToInt(),
            bankTotal = bankTotal,
            bankPercent = ((bankTotal / totalAmount) * 100).roundToInt(),
            walletTotal = walletTotal,
            walletPercent = ((walletTotal / totalAmount) * 100).roundToInt(),
            cashTotal = cashTotal,
            cashPercent = ((cashTotal / totalAmount) * 100).roundToInt(),
            unusualTransactionsCount = unusualCount,
            periodMetrics = periodMetrics
        )
    }

    suspend fun getDailyRevenuePoints(
        transactions: List<TransactionEntity>,
        days: Int = 30
    ): List<DailyRevenuePoint> = withContext(Dispatchers.Default) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val displayFormat = SimpleDateFormat("dd MMM", Locale.US)
        
        val points = mutableListOf<DailyRevenuePoint>()
        val successful = transactions.filter { it.status == TransactionStatus.SUCCESSFUL.name }
        val groupedByDate = successful.groupBy { it.transactionDate }

        // Go backwards from today
        for (i in (days - 1) downTo 0) {
            val dateCal = Calendar.getInstance()
            dateCal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = dateFormat.format(dateCal.time)
            val labelStr = displayFormat.format(dateCal.time)

            val txns = groupedByDate[dateStr] ?: emptyList()
            val rev = txns.sumOf { it.amount }
            val count = txns.size
            val upiRev = txns.filter { it.paymentMethod == PaymentMethod.UPI.name }.sumOf { it.amount }
            val cardRev = txns.filter { it.paymentMethod == PaymentMethod.CARD.name }.sumOf { it.amount }
            val bankRev = txns.filter { it.paymentMethod == PaymentMethod.BANK_TRANSFER.name }.sumOf { it.amount }
            val otherRev = rev - upiRev - cardRev - bankRev
            val avgTicket = if (count > 0) rev / count else 0.0

            points.add(
                DailyRevenuePoint(
                    date = dateStr,
                    label = labelStr,
                    revenue = rev,
                    transactionCount = count,
                    upiRevenue = upiRev,
                    cardRevenue = cardRev,
                    bankRevenue = bankRev,
                    otherRevenue = otherRev.coerceAtLeast(0.0),
                    avgTicket = avgTicket
                )
            )
        }
        points
    }

    suspend fun getMonthlyRevenuePoints(
        transactions: List<TransactionEntity>,
        months: Int = 12
    ): List<MonthlyRevenuePoint> = withContext(Dispatchers.Default) {
        val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.US)
        val displayFormat = SimpleDateFormat("MMM yyyy", Locale.US)
        val shortFormat = SimpleDateFormat("MMM", Locale.US)
        
        val points = mutableListOf<MonthlyRevenuePoint>()
        val successful = transactions.filter { it.status == TransactionStatus.SUCCESSFUL.name }
        val groupedByMonth = successful.groupBy { txn ->
            if (txn.transactionDate.length >= 7) txn.transactionDate.substring(0, 7) else ""
        }

        // Go backwards from current month
        for (i in (months - 1) downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -i)
            val monthKey = monthKeyFormat.format(cal.time)
            val label = displayFormat.format(cal.time)
            val shortLabel = shortFormat.format(cal.time)

            val txns = groupedByMonth[monthKey] ?: emptyList()
            val rev = txns.sumOf { it.amount }
            val count = txns.size
            val upiRev = txns.filter { it.paymentMethod == PaymentMethod.UPI.name }.sumOf { it.amount }
            val cardRev = txns.filter { it.paymentMethod == PaymentMethod.CARD.name }.sumOf { it.amount }
            val bankRev = txns.filter { it.paymentMethod == PaymentMethod.BANK_TRANSFER.name }.sumOf { it.amount }
            val otherRev = rev - upiRev - cardRev - bankRev
            val avgTicket = if (count > 0) rev / count else 0.0

            points.add(
                MonthlyRevenuePoint(
                    monthKey = monthKey,
                    label = label,
                    shortLabel = shortLabel,
                    revenue = rev,
                    transactionCount = count,
                    upiRevenue = upiRev,
                    cardRevenue = cardRev,
                    bankRevenue = bankRev,
                    otherRevenue = otherRev.coerceAtLeast(0.0),
                    avgTicket = avgTicket
                )
            )
        }
        points
    }

    suspend fun calculateBusinessAnalytics(
        transactions: List<TransactionEntity>
    ): BusinessAnalyticsSummary = withContext(Dispatchers.Default) {
        val successful = transactions.filter { it.status == TransactionStatus.SUCCESSFUL.name }
        val groupedByDate = successful.groupBy { it.transactionDate }
        
        val totalRev = successful.sumOf { it.amount }
        val totalCount = successful.size
        val avgTxn = if (totalCount > 0) totalRev / totalCount else 0.0

        val daysCount = groupedByDate.keys.size.coerceAtLeast(1)
        val dailyAvg = totalRev / daysCount
        val weeklyAvg = dailyAvg * 7
        val monthlyAvg = dailyAvg * 30

        var highestDate = "-"
        var highestAmount = 0.0
        var lowestDate = "-"
        var lowestAmount = if (groupedByDate.isNotEmpty()) Double.MAX_VALUE else 0.0

        groupedByDate.forEach { (date, txns) ->
            val sum = txns.sumOf { it.amount }
            if (sum > highestAmount) {
                highestAmount = sum
                highestDate = date
            }
            if (sum < lowestAmount) {
                lowestAmount = sum
                lowestDate = date
            }
        }
        if (lowestAmount == Double.MAX_VALUE) lowestAmount = 0.0

        val highestTxn = successful.maxOfOrNull { it.amount } ?: 0.0

        // Weekend vs Weekday analysis
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        var weekendSum = 0.0
        var weekendDays = 0
        var weekdaySum = 0.0
        var weekdayDays = 0

        groupedByDate.forEach { (dateStr, txns) ->
            try {
                val parsed = dateFormat.parse(dateStr)
                if (parsed != null) {
                    val cal = Calendar.getInstance().apply { time = parsed }
                    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                    val sum = txns.sumOf { it.amount }
                    if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                        weekendSum += sum
                        weekendDays++
                    } else {
                        weekdaySum += sum
                        weekdayDays++
                    }
                }
            } catch (_: Exception) {}
        }

        val weekendAvg = if (weekendDays > 0) weekendSum / weekendDays else 0.0
        val weekdayAvg = if (weekdayDays > 0) weekdaySum / weekdayDays else 0.0

        // Dominant payment method
        val paymentGroups = successful.groupBy { it.paymentMethod }
        var domMethod = PaymentMethod.UPI.displayName
        var domPercent = 60
        if (totalRev > 0) {
            val maxPayment = paymentGroups.maxByOrNull { it.value.sumOf { txn -> txn.amount } }
            if (maxPayment != null) {
                domMethod = PaymentMethod.fromString(maxPayment.key).displayName
                domPercent = ((maxPayment.value.sumOf { it.amount } / totalRev) * 100).roundToInt()
            }
        }

        BusinessAnalyticsSummary(
            dailyAverage = dailyAvg,
            weeklyAverage = weeklyAvg,
            monthlyAverage = monthlyAvg,
            highestRevenueDay = highestDate,
            highestRevenueAmount = highestAmount,
            lowestRevenueDay = lowestDate,
            lowestRevenueAmount = lowestAmount,
            highestTransactionAmount = highestTxn,
            averageTransactionValue = avgTxn,
            totalTransactions = totalCount,
            totalRevenue = totalRev,
            weekendAverage = weekendAvg,
            weekdayAverage = weekdayAvg,
            dominantPaymentMethod = domMethod,
            dominantPaymentPercent = domPercent
        )
    }

    suspend fun generateIncomeForecast(
        transactions: List<TransactionEntity>
    ): IncomeForecast = withContext(Dispatchers.Default) {
        val successful = transactions.filter { it.status == TransactionStatus.SUCCESSFUL.name }
        val dailyPoints = getDailyRevenuePoints(successful, 14)
        val recent7 = dailyPoints.takeLast(7).map { it.revenue }
        val previous7 = dailyPoints.take(7).map { it.revenue }

        val recentAvg = if (recent7.isNotEmpty()) recent7.average() else 12500.0
        val prevAvg = if (previous7.isNotEmpty()) previous7.average() else 11000.0

        val trend = if (prevAvg > 0) ((recentAvg - prevAvg) / prevAvg) * 100.0 else 5.0

        val tomorrowMin = (recentAvg * 0.92).coerceAtLeast(100.0)
        val tomorrowMax = (recentAvg * 1.08).coerceAtLeast(tomorrowMin + 500)

        val next7Min = (recentAvg * 7 * 0.94).coerceAtLeast(1000.0)
        val next7Max = (recentAvg * 7 * 1.08).coerceAtLeast(next7Min + 3000)

        IncomeForecast(
            expectedTomorrowMin = tomorrowMin,
            expectedTomorrowMax = tomorrowMax,
            expectedNext7DaysMin = next7Min,
            expectedNext7DaysMax = next7Max,
            trendPercentage = trend,
            confidenceNote = "Based on your 14-day moving average and payment frequency."
        )
    }

    suspend fun refreshAiInsights(shopId: String) = withContext(Dispatchers.IO) {
        val profile = shopProfileDao.getActiveProfileDirect() ?: ShopProfile()
        val allTxns = transactionDao.getAllTransactions(shopId).firstOrNull() ?: emptyList()
        val metrics = calculateDashboardMetrics(allTxns, profile)
        val analytics = calculateBusinessAnalytics(allTxns)

        val insights = mutableListOf<AiInsightEntity>()

        // 1. Weekend vs Weekday Insight
        if (analytics.weekendAverage > analytics.weekdayAverage && analytics.weekdayAverage > 0) {
            val boost = (((analytics.weekendAverage - analytics.weekdayAverage) / analytics.weekdayAverage) * 100).roundToInt()
            insights.add(
                AiInsightEntity(
                    shopId = shopId,
                    insightType = "WEEKEND_SPIKE",
                    title = "Weekend Sales Momentum",
                    description = "Your weekend revenue is $boost% higher than your weekday average. Consider increasing fast-moving stock before Saturday.",
                    severity = "POSITIVE",
                    actionSuggestion = "Stock up on top-selling inventory by Friday afternoon.",
                    metricValue = "+$boost% on weekends"
                )
            )
        }

        // 2. Payment Dominance Insight
        if (metrics.upiPercent >= 50) {
            insights.add(
                AiInsightEntity(
                    shopId = shopId,
                    insightType = "PAYMENT_DOMINANCE",
                    title = "UPI Payment Dominance",
                    description = "UPI accounts for ${metrics.upiPercent}% (${profile.currency}${String.format(Locale.US, "%,.0f", metrics.upiTotal)}) of your received payments. Customers strongly prefer QR code payments.",
                    severity = "INFO",
                    actionSuggestion = "Keep your UPI QR standee clean and near checkout counter.",
                    metricValue = "${metrics.upiPercent}% UPI"
                )
            )
        }

        // 3. Monthly Target Progress
        if (metrics.targetProgressPercent >= 70) {
            insights.add(
                AiInsightEntity(
                    shopId = shopId,
                    insightType = "TARGET_PROGRESS",
                    title = "Monthly Target in Sight",
                    description = "You've achieved ${metrics.targetProgressPercent}% of your ${profile.currency}${String.format(Locale.US, "%,.0f", metrics.monthlyTarget)} monthly revenue target. Only ${profile.currency}${String.format(Locale.US, "%,.0f", metrics.targetRemaining)} remaining to cross your milestone!",
                    severity = "POSITIVE",
                    actionSuggestion = "Run a mid-week customer special to seal the milestone early.",
                    metricValue = "${metrics.targetProgressPercent}% achieved"
                )
            )
        } else {
            insights.add(
                AiInsightEntity(
                    shopId = shopId,
                    insightType = "TARGET_PROGRESS",
                    title = "Revenue Target Pace",
                    description = "Current monthly revenue is ${profile.currency}${String.format(Locale.US, "%,.0f", metrics.monthlyRevenue)} against your target of ${profile.currency}${String.format(Locale.US, "%,.0f", metrics.monthlyTarget)}.",
                    severity = "INFO",
                    actionSuggestion = "Promote high-margin combo offers to boost daily average.",
                    metricValue = "${metrics.targetProgressPercent}% pace"
                )
            )
        }

        // 4. Anomaly Insight
        val unusual = allTxns.filter { it.isUnusual }
        if (unusual.isNotEmpty()) {
            val highest = unusual.maxByOrNull { it.amount }
            insights.add(
                AiInsightEntity(
                    shopId = shopId,
                    insightType = "ANOMALY",
                    title = "Unusual Activity Detected",
                    description = "${unusual.size} transaction(s) flagged for review, including ${profile.currency}${String.format(Locale.US, "%,.2f", highest?.amount ?: 0.0)} (${highest?.customerNote.takeIf { !it.isNullOrBlank() } ?: "Large Single Ticket"}).",
                    severity = "WARNING",
                    actionSuggestion = "Review unusual transactions in Transactions -> Needs Review filter.",
                    metricValue = "${unusual.size} flagged"
                )
            )
        }

        aiInsightDao.deleteInsightsForShop(shopId)
        aiInsightDao.insertInsights(insights)
    }

    suspend fun parseAndValidateCsv(csvContent: String, shopId: String = "shop_default"): ImportValidationResult = withContext(Dispatchers.IO) {
        val lines = csvContent.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return@withContext ImportValidationResult(emptyList(), 0, 0, 0, listOf("CSV content is empty"))
        }

        val existingIds = transactionDao.getAllTransactionIds(shopId).toHashSet()
        val fileSeenIds = HashSet<String>()
        val validList = mutableListOf<TransactionEntity>()
        val errors = mutableListOf<String>()
        var duplicateCount = 0
        var invalidCount = 0
        var failedCount = 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = dateFormat.format(Date())

        val headerLine = lines.first().lowercase()
        val hasHeader = headerLine.contains("transaction_id") || headerLine.contains("amount")
        val startIndex = if (hasHeader) 1 else 0

        for (i in startIndex until lines.size) {
            val rowNum = i + 1
            val line = lines[i]
            val cols = line.split(",").map { it.trim().trim('"') }

            if (cols.isEmpty() || cols.all { it.isBlank() }) continue

            // Flexible columns:
            // Format 1: transaction_id, customer_name, amount, date, time, payment_method, status, category, note
            // Format 2: transaction_id, amount, date, time, payment_method, status, category, note
            val rawTxnId = cols.getOrNull(0) ?: ""
            
            // Check if col[1] is numeric amount or customer name
            val isCol1Amount = cols.getOrNull(1)?.toDoubleOrNull() != null
            val rawCustomerName: String
            val rawAmount: String
            val rawDate: String
            val rawTime: String
            val rawMethod: String
            val rawStatus: String
            val rawCategory: String
            val rawNote: String

            if (isCol1Amount) {
                rawCustomerName = "Walk-in Customer"
                rawAmount = cols.getOrNull(1) ?: ""
                rawDate = cols.getOrNull(2) ?: todayStr
                rawTime = cols.getOrNull(3) ?: "12:00"
                rawMethod = cols.getOrNull(4) ?: "UPI"
                rawStatus = cols.getOrNull(5) ?: "SUCCESSFUL"
                rawCategory = cols.getOrNull(6) ?: "Sales"
                rawNote = cols.getOrNull(7) ?: ""
            } else {
                rawCustomerName = cols.getOrNull(1) ?: "Walk-in Customer"
                rawAmount = cols.getOrNull(2) ?: ""
                rawDate = cols.getOrNull(3) ?: todayStr
                rawTime = cols.getOrNull(4) ?: "12:00"
                rawMethod = cols.getOrNull(5) ?: "UPI"
                rawStatus = cols.getOrNull(6) ?: "SUCCESSFUL"
                rawCategory = cols.getOrNull(7) ?: "Sales"
                rawNote = cols.getOrNull(8) ?: ""
            }

            if (rawTxnId.isBlank()) {
                invalidCount++
                errors.add("Row $rowNum: Missing Transaction ID")
                continue
            }

            val amount = rawAmount.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                invalidCount++
                errors.add("Row $rowNum: Invalid amount '$rawAmount'")
                continue
            }

            if (existingIds.contains(rawTxnId) || fileSeenIds.contains(rawTxnId)) {
                duplicateCount++
                errors.add("Row $rowNum: Duplicate Transaction ID '$rawTxnId'")
                continue
            }

            fileSeenIds.add(rawTxnId)
            val isUnusual = amount >= 15000.0

            validList.add(
                TransactionEntity(
                    shopId = shopId,
                    transactionId = rawTxnId,
                    customerName = rawCustomerName.ifBlank { "Walk-in Customer" },
                    amount = amount,
                    category = rawCategory,
                    description = rawNote,
                    paymentMethod = PaymentMethod.fromString(rawMethod).name,
                    status = TransactionStatus.fromString(rawStatus).name,
                    transactionDate = rawDate,
                    transactionTime = rawTime,
                    customerNote = rawNote,
                    isUnusual = isUnusual
                )
            )
        }

        ImportValidationResult(
            validTransactions = validList,
            duplicateCount = duplicateCount,
            invalidCount = invalidCount,
            failedCount = failedCount,
            errorMessages = errors
        )
    }

    suspend fun importValidatedTransactions(transactions: List<TransactionEntity>) = withContext(Dispatchers.IO) {
        if (transactions.isNotEmpty()) {
            transactionDao.insertTransactions(transactions)
            refreshAiInsights(transactions.first().shopId)
        }
    }

    fun exportTransactionsCsv(transactions: List<TransactionEntity>): String {
        val sb = StringBuilder()
        sb.append("transaction_id,customer_name,amount,date,time,payment_method,status,category,note,is_unusual\n")
        transactions.forEach { txn ->
            sb.append("\"${txn.transactionId}\",")
            sb.append("\"${txn.customerName.replace("\"", "\"\"")}\",")
            sb.append("${txn.amount},")
            sb.append("\"${txn.transactionDate}\",")
            sb.append("\"${txn.transactionTime}\",")
            sb.append("\"${txn.paymentMethod}\",")
            sb.append("\"${txn.status}\",")
            sb.append("\"${txn.category}\",")
            sb.append("\"${txn.customerNote.replace("\"", "\"\"")}\",")
            sb.append("${txn.isUnusual}\n")
        }
        return sb.toString()
    }

    suspend fun seedDemoData() = withContext(Dispatchers.IO) {
        val defaultProfile = ShopProfile(
            id = "shop_default",
            ownerName = "Rajesh Sharma",
            shopName = "Sharma Grocery & Mart",
            category = ShopCategory.GROCERY.name,
            currency = "₹",
            monthlyRevenueTarget = 400000.0,
            email = "rajesh.store@shoppay.ai",
            isDemoAccount = true
        )
        shopProfileDao.clearProfiles()
        shopProfileDao.saveProfile(defaultProfile)
        transactionDao.deleteAllForShop(defaultProfile.id)

        val transactions = mutableListOf<TransactionEntity>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)

        val cal = Calendar.getInstance()
        val random = Random(42) // deterministic seed for consistent, beautiful data

        val sampleNotes = listOf(
            "Grocery basket", "Cold drinks & snacks", "Monthly ration", "Dairy & milk",
            "Household supplies", "Dry fruits box", "Personal care", "Tea & spices",
            "Cereals & pulses", "Biscuits combo", "Online UPI scan", "Payment via card"
        )

        // Generate 120 realistic transactions over the last 90 days
        for (i in 89 downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = dateFormat.format(dayCal.time)
            val dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK)
            val isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)

            // Weekend has more transactions
            val txnsToday = if (isWeekend) random.nextInt(2, 5) else random.nextInt(1, 3)

            val sampleCustomers = listOf(
                "Amit Verma", "Pooja Sharma", "Rohan Mehta", "Sunil Gupta", 
                "Ananya Iyer", "Vikram Patel", "Meera Nair", "Deepak Rao", 
                "Sneha Joshi", "Rahul Kapoor", "Priya Sen", "Arjun Reddy",
                "Kavita Deshmukh", "Sanjay Singhal", "Neha Choudhary", "Manish Malhotra"
            )

            for (j in 0 until txnsToday) {
                val hour = random.nextInt(8, 22)
                val minute = random.nextInt(0, 60)
                val timeStr = String.format(Locale.US, "%02d:%02d", hour, minute)

                val txnNum = 10000 + transactions.size + 1
                val txnId = "TXN-$txnNum"
                val customerName = sampleCustomers[random.nextInt(sampleCustomers.size)]

                // Payment method distribution: ~60% UPI, 25% Card, 10% Bank Transfer, 5% Wallet
                val methodRoll = random.nextInt(100)
                val method = when {
                    methodRoll < 62 -> PaymentMethod.UPI.name
                    methodRoll < 87 -> PaymentMethod.CARD.name
                    methodRoll < 97 -> PaymentMethod.BANK_TRANSFER.name
                    else -> PaymentMethod.WALLET.name
                }

                // Realistic amounts
                val amountTier = random.nextInt(100)
                val amount = when {
                    amountTier < 30 -> (random.nextInt(5, 30) * 10).toDouble() // ₹50 - ₹300
                    amountTier < 65 -> (random.nextInt(30, 90) * 10).toDouble() // ₹300 - ₹900
                    amountTier < 90 -> (random.nextInt(90, 250) * 10).toDouble() // ₹900 - ₹2,500
                    amountTier < 98 -> (random.nextInt(250, 500) * 10).toDouble() // ₹2,500 - ₹5,000
                    else -> 24500.0 // 1-2 unusual large wholesale order
                }

                val isUnusual = amount >= 15000.0
                val note = if (isUnusual) "Wholesale Festival Bulk Order" else sampleNotes[random.nextInt(sampleNotes.size)]

                // Status distribution
                val statusRoll = random.nextInt(100)
                val status = when {
                    statusRoll < 93 -> TransactionStatus.SUCCESSFUL.name
                    statusRoll < 96 -> TransactionStatus.PENDING.name
                    statusRoll < 98 -> TransactionStatus.REFUNDED.name
                    else -> TransactionStatus.FAILED.name
                }

                transactions.add(
                    TransactionEntity(
                        shopId = defaultProfile.id,
                        transactionId = txnId,
                        customerName = customerName,
                        amount = amount,
                        category = if (status == TransactionStatus.REFUNDED.name) "Refund" else "Sales",
                        description = note,
                        paymentMethod = method,
                        status = status,
                        transactionDate = dateStr,
                        transactionTime = timeStr,
                        customerNote = note,
                        isUnusual = isUnusual
                    )
                )
            }
        }

        // Add today's live sample transactions for immediate dashboard responsiveness
        val todayCal = Calendar.getInstance()
        val todayStr = dateFormat.format(todayCal.time)
        val todayTxns = listOf(
            TransactionEntity(
                shopId = defaultProfile.id,
                transactionId = "TXN-10293",
                customerName = "Pooja Sharma",
                amount = 850.0,
                category = "Sales",
                description = "Fresh Dairy & Grocery Basket",
                paymentMethod = PaymentMethod.UPI.name,
                status = TransactionStatus.SUCCESSFUL.name,
                transactionDate = todayStr,
                transactionTime = "10:42",
                customerNote = "Fresh Dairy & Grocery Basket",
                isUnusual = false
            ),
            TransactionEntity(
                shopId = defaultProfile.id,
                transactionId = "TXN-10294",
                customerName = "Rohan Mehta",
                amount = 2450.0,
                category = "Sales",
                description = "Monthly Household Supplies",
                paymentMethod = PaymentMethod.CARD.name,
                status = TransactionStatus.SUCCESSFUL.name,
                transactionDate = todayStr,
                transactionTime = "11:15",
                customerNote = "Monthly Household Supplies",
                isUnusual = false
            ),
            TransactionEntity(
                shopId = defaultProfile.id,
                transactionId = "TXN-10295",
                customerName = "Amit Verma",
                amount = 120.0,
                category = "Sales",
                description = "Cold Drinks & Snacks",
                paymentMethod = PaymentMethod.UPI.name,
                status = TransactionStatus.SUCCESSFUL.name,
                transactionDate = todayStr,
                transactionTime = "11:58",
                customerNote = "Cold Drinks & Snacks",
                isUnusual = false
            ),
            TransactionEntity(
                shopId = defaultProfile.id,
                transactionId = "TXN-10296",
                customerName = "Sanjay Singhal",
                amount = 5400.0,
                category = "Sales",
                description = "Bulk Restaurant Spices Order",
                paymentMethod = PaymentMethod.BANK_TRANSFER.name,
                status = TransactionStatus.SUCCESSFUL.name,
                transactionDate = todayStr,
                transactionTime = "12:30",
                customerNote = "Bulk Restaurant Spices Order",
                isUnusual = false
            ),
            TransactionEntity(
                shopId = defaultProfile.id,
                transactionId = "TXN-10297",
                customerName = "Ananya Iyer",
                amount = 450.0,
                category = "Sales",
                description = "Snacks & Tea",
                paymentMethod = PaymentMethod.UPI.name,
                status = TransactionStatus.SUCCESSFUL.name,
                transactionDate = todayStr,
                transactionTime = "13:10",
                customerNote = "Snacks & Tea",
                isUnusual = false
            )
        )
        transactions.addAll(todayTxns)

        transactionDao.insertTransactions(transactions)
        refreshAiInsights(defaultProfile.id)
    }

    suspend fun clearAllData(shopId: String = "shop_default") = withContext(Dispatchers.IO) {
        transactionDao.deleteAllForShop(shopId)
        aiInsightDao.deleteInsightsForShop(shopId)
    }
}
