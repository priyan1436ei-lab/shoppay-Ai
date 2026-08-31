package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaymentMethod(val displayName: String, val code: String) {
    UPI("UPI", "UPI"),
    CARD("Card", "CARD"),
    BANK_TRANSFER("Bank Transfer", "BANK_TRANSFER"),
    WALLET("Digital Wallet", "WALLET"),
    CASH("Cash", "CASH");

    companion object {
        fun fromString(value: String): PaymentMethod {
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || 
                it.code.equals(value, ignoreCase = true) || 
                it.displayName.equals(value, ignoreCase = true) 
            } ?: UPI
        }
    }
}

enum class TransactionStatus(val displayName: String) {
    SUCCESSFUL("Successful"),
    PENDING("Pending"),
    FAILED("Failed"),
    REFUNDED("Refunded");

    companion object {
        fun fromString(value: String): TransactionStatus {
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || 
                it.displayName.equals(value, ignoreCase = true) 
            } ?: SUCCESSFUL
        }
    }
}

enum class ShopCategory(val displayName: String, val iconName: String) {
    GROCERY("Grocery & Kirana", "Storefront"),
    RESTAURANT("Restaurant & Cafe", "Restaurant"),
    RETAIL("General Retail", "ShoppingBag"),
    CLOTHING("Clothing & Apparel", "Checkroom"),
    ELECTRONICS("Electronics & Mobile", "Devices"),
    BAKERY("Bakery & Sweets", "Cake"),
    PHARMACY("Pharmacy & Medical", "Medication"),
    OTHER("Other Business", "Business");

    companion object {
        fun fromString(value: String): ShopCategory {
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || 
                it.displayName.equals(value, ignoreCase = true) 
            } ?: GROCERY
        }
    }
}

@Entity(tableName = "shops")
data class ShopProfile(
    @PrimaryKey val id: String = "shop_default",
    val ownerName: String = "Rajesh Sharma",
    val shopName: String = "Sharma Grocery & Mart",
    val category: String = ShopCategory.GROCERY.name,
    val currency: String = "₹",
    val monthlyRevenueTarget: Double = 400000.0,
    val email: String = "rajesh.store@shoppay.ai",
    val isDemoAccount: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shopId: String = "shop_default",
    val transactionId: String,
    val customerName: String = "Walk-in Customer",
    val amount: Double,
    val category: String = "Sales",
    val description: String = "",
    val paymentMethod: String = PaymentMethod.UPI.name,
    val status: String = TransactionStatus.SUCCESSFUL.name,
    val transactionDate: String, // YYYY-MM-DD (Date)
    val transactionTime: String = "12:00", // HH:mm
    val customerNote: String = "",
    val isUnusual: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_insights")
data class AiInsightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shopId: String = "shop_default",
    val insightType: String, // REVENUE_TREND, PAYMENT_DOMINANCE, WEEKEND_SPIKE, ANOMALY, TARGET_PROGRESS
    val title: String,
    val description: String,
    val severity: String = "INFO", // INFO, POSITIVE, WARNING
    val actionSuggestion: String = "",
    val metricValue: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedActions: List<String> = emptyList()
)

data class ImportValidationResult(
    val validTransactions: List<TransactionEntity>,
    val duplicateCount: Int,
    val invalidCount: Int,
    val failedCount: Int,
    val errorMessages: List<String>
)

data class IncomeForecast(
    val expectedTomorrowMin: Double,
    val expectedTomorrowMax: Double,
    val expectedNext7DaysMin: Double,
    val expectedNext7DaysMax: Double,
    val trendPercentage: Double,
    val confidenceNote: String
)

enum class DashboardDateRange(val displayName: String, val shortName: String, val subtitle: String) {
    TODAY("Today", "Today", "Today's performance"),
    THIS_WEEK("This Week", "Week", "Current week (Mon - Today)"),
    THIS_MONTH("This Month", "Month", "Current month performance");

    companion object {
        fun fromString(value: String): DashboardDateRange {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true) ||
                it.shortName.equals(value, ignoreCase = true)
            } ?: THIS_MONTH
        }
    }
}

data class PeriodRevenueMetrics(
    val dateRange: DashboardDateRange = DashboardDateRange.THIS_MONTH,
    val revenue: Double = 0.0,
    val previousPeriodRevenue: Double = 0.0,
    val growthPercent: Double = 0.0,
    val transactionCount: Int = 0,
    val averageTicket: Double = 0.0,
    val periodTarget: Double = 0.0,
    val targetProgressPercent: Int = 0,
    val targetRemaining: Double = 0.0,
    val upiTotal: Double = 0.0,
    val upiPercent: Int = 0,
    val cardTotal: Double = 0.0,
    val cardPercent: Int = 0,
    val bankTotal: Double = 0.0,
    val bankPercent: Int = 0,
    val walletTotal: Double = 0.0,
    val walletPercent: Int = 0,
    val cashTotal: Double = 0.0,
    val cashPercent: Int = 0,
    val periodTitle: String = "TOTAL MONTHLY REVENUE",
    val comparisonLabel: String = "vs last month",
    val dateRangeSubtitle: String = ""
)

data class DashboardMetrics(
    val todayIncome: Double = 0.0,
    val yesterdayIncome: Double = 0.0,
    val todayGrowthPercent: Double = 0.0,
    val todayTransactionCount: Int = 0,
    val averageTransactionValue: Double = 0.0,
    val monthlyRevenue: Double = 0.0,
    val monthlyTarget: Double = 400000.0,
    val targetProgressPercent: Int = 0,
    val targetRemaining: Double = 0.0,
    val upiTotal: Double = 0.0,
    val upiPercent: Int = 0,
    val cardTotal: Double = 0.0,
    val cardPercent: Int = 0,
    val bankTotal: Double = 0.0,
    val bankPercent: Int = 0,
    val walletTotal: Double = 0.0,
    val walletPercent: Int = 0,
    val cashTotal: Double = 0.0,
    val cashPercent: Int = 0,
    val unusualTransactionsCount: Int = 0,
    val periodMetrics: PeriodRevenueMetrics = PeriodRevenueMetrics()
)

data class DailyRevenuePoint(
    val date: String,
    val label: String,
    val revenue: Double,
    val transactionCount: Int,
    val upiRevenue: Double = 0.0,
    val cardRevenue: Double = 0.0,
    val bankRevenue: Double = 0.0,
    val otherRevenue: Double = 0.0,
    val avgTicket: Double = 0.0
)

data class MonthlyRevenuePoint(
    val monthKey: String, // e.g. "2026-08"
    val label: String,    // e.g. "Aug 2026"
    val shortLabel: String = "", // e.g. "Aug"
    val revenue: Double,
    val transactionCount: Int,
    val upiRevenue: Double = 0.0,
    val cardRevenue: Double = 0.0,
    val bankRevenue: Double = 0.0,
    val otherRevenue: Double = 0.0,
    val avgTicket: Double = 0.0
)

data class BusinessAnalyticsSummary(
    val dailyAverage: Double,
    val weeklyAverage: Double,
    val monthlyAverage: Double,
    val highestRevenueDay: String,
    val highestRevenueAmount: Double,
    val lowestRevenueDay: String,
    val lowestRevenueAmount: Double,
    val highestTransactionAmount: Double,
    val averageTransactionValue: Double,
    val totalTransactions: Int,
    val totalRevenue: Double,
    val weekendAverage: Double,
    val weekdayAverage: Double,
    val dominantPaymentMethod: String,
    val dominantPaymentPercent: Int
)
