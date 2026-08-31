package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DashboardDateRange
import com.example.data.model.TransactionEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopPayUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: ShopPayUiState,
    onDateRangeSelected: (DashboardDateRange) -> Unit = {},
    onPeriodSelected: (Int) -> Unit,
    onPeriodTypeChange: (String) -> Unit = {},
    onMonthsSelected: (Int) -> Unit = {},
    onChartStyleChange: (String) -> Unit = {},
    onChartMetricChange: (String) -> Unit = {},
    onAddTransactionClick: () -> Unit,
    onImportCsvClick: () -> Unit,
    onViewAllTransactions: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onOpenAiAssistant: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    val currency = state.profile.currency
    val metrics = state.metrics
    val periodMetrics = metrics.periodMetrics

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElegantPurpleDark)
                                .border(1.dp, ElegantLilac.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = state.profile.shopName.take(2).uppercase()
                            Text(
                                text = if (initials.isNotBlank()) initials else "SP",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElegantLilac
                                )
                            )
                        }
                        Column {
                            Text(
                                text = state.profile.shopName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "Welcome back, ${state.profile.ownerName.split(" ").firstOrNull() ?: "Merchant"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier.testTag("btn_dashboard_notifications")
                    ) {
                        BadgedBox(
                            badge = {
                                if (state.notifications.isNotEmpty()) {
                                    Badge(
                                        containerColor = ElegantLilac,
                                        contentColor = ElegantPurpleDeep
                                    ) {
                                        Text("${state.notifications.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = TextSecondary
                            )
                        }
                    }
                    IconButton(
                        onClick = onImportCsvClick,
                        modifier = Modifier.testTag("btn_dashboard_import_csv")
                    ) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = "Import CSV",
                            tint = ElegantLilac
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransactionClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Payment", fontWeight = FontWeight.Bold) },
                containerColor = ElegantLilac,
                contentColor = ElegantPurpleDeep,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("fab_add_transaction")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
                .testTag("dashboard_scroll_content"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Range Filter Bar (Today | This Week | This Month)
            item {
                DateRangeFilterBar(
                    selectedRange = state.selectedDateRange,
                    onRangeSelected = onDateRangeSelected
                )
            }

            // Elegant Dark Hero Card (Nova-Vault Style Total Revenue / Balance)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_hero_card"),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4F378B),
                                        Color(0xFF381E72),
                                        Color(0xFF261447)
                                    )
                                )
                            )
                            .border(1.dp, Color(0x33D0BCFF), RoundedCornerShape(28.dp))
                            .padding(22.dp)
                    ) {
                        // Decorative ambient glow
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(ElegantLilac.copy(alpha = 0.08f))
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = periodMetrics.periodTitle,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    ),
                                    color = ElegantLilac
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x26FFFFFF)
                                ) {
                                    Text(
                                        text = "${periodMetrics.targetProgressPercent}% of goal",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                text = "$currency${String.format(Locale.US, "%,.2f", periodMetrics.revenue)}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val growthBg = if (periodMetrics.growthPercent >= 0) ElegantGreenContainer else ElegantRedContainer
                                    val growthColor = if (periodMetrics.growthPercent >= 0) ElegantGreen else ElegantRed
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = growthBg
                                    ) {
                                        Text(
                                            text = "${String.format(Locale.US, "%+.1f", periodMetrics.growthPercent)}% ${periodMetrics.comparisonLabel}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = growthColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Target: $currency${String.format(Locale.US, "%,.0f", periodMetrics.periodTarget)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xCCFFFFFF)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Action Grid (Send / Receive / AI / Statement)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionItem(
                        icon = Icons.Default.Add,
                        label = "Add Entry",
                        onClick = onAddTransactionClick,
                        testTag = "action_quick_add"
                    )
                    QuickActionItem(
                        icon = Icons.Default.CloudUpload,
                        label = "Import CSV",
                        onClick = onImportCsvClick,
                        testTag = "action_quick_import"
                    )
                    QuickActionItem(
                        icon = Icons.Default.SmartToy,
                        label = "Ask AI",
                        onClick = onOpenAiAssistant,
                        testTag = "action_quick_ai"
                    )
                    QuickActionItem(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        label = "History",
                        onClick = onViewAllTransactions,
                        testTag = "action_quick_history"
                    )
                }
            }

            // KPI Cards 2x2 Layout
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiCard(
                            title = "${state.selectedDateRange.displayName}'s Income",
                            value = "$currency${String.format(Locale.US, "%,.0f", periodMetrics.revenue)}",
                            badgeText = "${String.format(Locale.US, "%+.1f", periodMetrics.growthPercent)}%",
                            isPositiveBadge = periodMetrics.growthPercent >= 0,
                            icon = Icons.Default.Payments,
                            accentColor = ElegantGreen,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_today_income"
                        )
                        KpiCard(
                            title = "Transactions",
                            value = "${periodMetrics.transactionCount}",
                            subtitle = "${state.selectedDateRange.displayName} sales",
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            accentColor = ElegantLilac,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_today_txns"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiCard(
                            title = "Avg Ticket",
                            value = "$currency${String.format(Locale.US, "%,.0f", periodMetrics.averageTicket)}",
                            subtitle = "Per customer",
                            icon = Icons.Default.ShoppingCart,
                            accentColor = ElegantBlue,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_avg_ticket"
                        )
                        KpiCard(
                            title = "${state.selectedDateRange.shortName} Target",
                            value = "${periodMetrics.targetProgressPercent}%",
                            badgeText = if (periodMetrics.targetRemaining == 0.0) "Achieved" else "In Progress",
                            isPositiveBadge = periodMetrics.targetRemaining == 0.0,
                            icon = Icons.Default.Flag,
                            accentColor = ElegantLilacLight,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_monthly_target"
                        )
                    }
                }
            }

            // Monthly Target Card
            item {
                TargetProgressCard(
                    metrics = metrics,
                    currency = currency,
                    periodMetrics = periodMetrics
                )
            }

            // Interactive Revenue Chart (Recharts-Grade Daily & Monthly Visualizer)
            item {
                PaymentRevenueTrendsChart(
                    dailyPoints = state.dailyRevenuePoints,
                    monthlyPoints = state.monthlyRevenuePoints,
                    currency = currency,
                    selectedPeriodType = state.chartPeriodType,
                    onPeriodTypeChange = onPeriodTypeChange,
                    selectedDays = state.chartDays,
                    onDaysSelected = onPeriodSelected,
                    selectedMonths = state.chartMonths,
                    onMonthsSelected = onMonthsSelected,
                    chartStyle = state.chartStyle,
                    onChartStyleChange = onChartStyleChange,
                    chartMetric = state.chartMetric,
                    onChartMetricChange = onChartMetricChange
                )
            }

            // Payment Method Breakdown Card
            item {
                PaymentMethodDonutCard(
                    metrics = metrics,
                    currency = currency,
                    periodMetrics = periodMetrics
                )
            }

            // Prominent AI Insight Card
            if (state.aiInsights.isNotEmpty()) {
                item {
                    val topInsight = state.aiInsights.first()
                    AiInsightCard(
                        insight = topInsight,
                        onActionClick = onOpenAiAssistant
                    )
                }
            }

            // AI Assistant Quick Access Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenAiAssistant)
                        .testTag("ai_assistant_banner"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(ElegantPurpleDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = ElegantLilac
                                )
                            }
                            Column {
                                Text(
                                    text = "Ask ShopPay AI Assistant",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Revenue forecast & business intelligence",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                }
            }

            // Recent Transactions Component
            item {
                RecentTransactionsComponent(
                    transactions = state.allTransactions.ifEmpty { state.recentTransactions },
                    currency = currency,
                    title = "Recent Transactions",
                    subtitle = "Scrollable list of latest incoming customer payments",
                    showFilters = true,
                    showSearch = true,
                    maxDisplayCount = 10,
                    onTransactionClick = onTransactionClick,
                    onViewAllClick = onViewAllTransactions,
                    onAddPaymentClick = onAddTransactionClick
                )
            }

            // Bottom Spacing for FAB
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, Color(0x1AD0BCFF), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = ElegantLilac,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = TextSecondary
        )
    }
}
