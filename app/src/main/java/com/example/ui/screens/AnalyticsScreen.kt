package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KpiCard
import com.example.ui.components.PaymentMethodDonutCard
import com.example.ui.components.PaymentRevenueTrendsChart
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopPayUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    state: ShopPayUiState,
    onPeriodSelected: (Int) -> Unit = {},
    onPeriodTypeChange: (String) -> Unit = {},
    onMonthsSelected: (Int) -> Unit = {},
    onChartStyleChange: (String) -> Unit = {},
    onChartMetricChange: (String) -> Unit = {},
    onViewUnusualTransactions: () -> Unit
) {
    val currency = state.profile.currency
    val a = state.analytics
    val metrics = state.metrics

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Business Analytics",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
                .testTag("analytics_screen"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Interactive Revenue Trends Visualizer
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
            // Averages Breakdown Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(22.dp))
                        .testTag("analytics_averages_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Revenue Velocity & Averages",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AverageBox(label = "Daily Avg", amount = "$currency${String.format(Locale.US, "%,.0f", a.dailyAverage)}", modifier = Modifier.weight(1f))
                            AverageBox(label = "Weekly Avg", amount = "$currency${String.format(Locale.US, "%,.0f", a.weeklyAverage)}", modifier = Modifier.weight(1f))
                            AverageBox(label = "Monthly Avg", amount = "$currency${String.format(Locale.US, "%,.0f", a.monthlyAverage)}", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Weekend vs Weekday Performance Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(22.dp))
                        .testTag("weekend_weekday_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Weekend vs Weekday Sales",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Comparison of customer shopping patterns",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            if (a.weekendAverage > a.weekdayAverage && a.weekdayAverage > 0) {
                                val boost = (((a.weekendAverage - a.weekdayAverage) / a.weekdayAverage) * 100).toInt()
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ElegantGreenContainer
                                ) {
                                    Text(
                                        text = "+$boost% Weekend",
                                        color = ElegantGreen,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = DarkSurfaceElevated,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Weekend Daily Avg", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("$currency${String.format(Locale.US, "%,.0f", a.weekendAverage)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ElegantLilac)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = DarkSurfaceElevated,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Weekday Daily Avg", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("$currency${String.format(Locale.US, "%,.0f", a.weekdayAverage)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Peak Day & High-Water Mark Card
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        title = "Peak Day",
                        value = "$currency${String.format(Locale.US, "%,.0f", a.highestRevenueAmount)}",
                        subtitle = a.highestRevenueDay,
                        icon = Icons.Default.EmojiEvents,
                        accentColor = ElegantAmber,
                        modifier = Modifier.weight(1f)
                    )

                    KpiCard(
                        title = "Max Single Ticket",
                        value = "$currency${String.format(Locale.US, "%,.0f", a.highestTransactionAmount)}",
                        subtitle = "Largest ticket",
                        icon = Icons.Default.Star,
                        accentColor = ElegantLilacLight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Payment Breakdown Card
            item {
                PaymentMethodDonutCard(
                    metrics = metrics,
                    currency = currency
                )
            }

            // Anomaly / Risk Review Banner
            if (metrics.unusualTransactionsCount > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0x33FFDDAE), RoundedCornerShape(22.dp))
                            .testTag("anomaly_alert_card"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = ElegantAmber)
                                Text(
                                    text = "Anomalies & Large Tickets (${metrics.unusualTransactionsCount})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ElegantAmber
                                )
                            }
                            Text(
                                text = "We detected ${metrics.unusualTransactionsCount} transaction(s) with unusually large amounts. Verify that these match customer invoices.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Button(
                                onClick = onViewUnusualTransactions,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElegantPurpleDark,
                                    contentColor = ElegantLilac
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Review Flagged Transactions", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun AverageBox(label: String, amount: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DarkSurfaceElevated,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }
    }
}
