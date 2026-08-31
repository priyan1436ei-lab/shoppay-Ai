package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyRevenuePoint
import com.example.data.model.MonthlyRevenuePoint
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * Unified data point representing either a daily or monthly revenue entry
 * for the Recharts-grade visualization engine.
 */
data class ChartTrendPoint(
    val key: String,
    val label: String,
    val shortLabel: String,
    val revenue: Double,
    val transactionCount: Int,
    val upiRevenue: Double = 0.0,
    val cardRevenue: Double = 0.0,
    val bankRevenue: Double = 0.0,
    val otherRevenue: Double = 0.0,
    val avgTicket: Double = 0.0
)

@Composable
fun PaymentRevenueTrendsChart(
    dailyPoints: List<DailyRevenuePoint>,
    monthlyPoints: List<MonthlyRevenuePoint>,
    currency: String,
    selectedPeriodType: String = "DAILY", // "DAILY", "MONTHLY"
    onPeriodTypeChange: (String) -> Unit = {},
    selectedDays: Int = 30,
    onDaysSelected: (Int) -> Unit = {},
    selectedMonths: Int = 12,
    onMonthsSelected: (Int) -> Unit = {},
    chartStyle: String = "AREA", // "AREA", "BAR"
    onChartStyleChange: (String) -> Unit = {},
    chartMetric: String = "REVENUE", // "REVENUE", "COUNT", "BREAKDOWN"
    onChartMetricChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Map data points based on period type
    val trendPoints: List<ChartTrendPoint> = remember(selectedPeriodType, dailyPoints, monthlyPoints) {
        if (selectedPeriodType == "MONTHLY") {
            monthlyPoints.map {
                ChartTrendPoint(
                    key = it.monthKey,
                    label = it.label,
                    shortLabel = it.shortLabel.ifBlank { it.label.take(3) },
                    revenue = it.revenue,
                    transactionCount = it.transactionCount,
                    upiRevenue = it.upiRevenue,
                    cardRevenue = it.cardRevenue,
                    bankRevenue = it.bankRevenue,
                    otherRevenue = it.otherRevenue,
                    avgTicket = it.avgTicket
                )
            }
        } else {
            dailyPoints.map {
                ChartTrendPoint(
                    key = it.date,
                    label = it.label,
                    shortLabel = it.label.split(" ").firstOrNull() ?: it.label,
                    revenue = it.revenue,
                    transactionCount = it.transactionCount,
                    upiRevenue = it.upiRevenue,
                    cardRevenue = it.cardRevenue,
                    bankRevenue = it.bankRevenue,
                    otherRevenue = it.otherRevenue,
                    avgTicket = it.avgTicket
                )
            }
        }
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    // Summary statistics
    val totalRevenue = remember(trendPoints) { trendPoints.sumOf { it.revenue } }
    val totalTransactions = remember(trendPoints) { trendPoints.sumOf { it.transactionCount } }
    val avgRevenue = remember(trendPoints) {
        if (trendPoints.isNotEmpty()) totalRevenue / trendPoints.size else 0.0
    }
    val peakPoint = remember(trendPoints) { trendPoints.maxByOrNull { it.revenue } }

    // Trend percentage calculation (first half vs second half)
    val trendGrowthPercent = remember(trendPoints) {
        if (trendPoints.size >= 4) {
            val half = trendPoints.size / 2
            val firstHalfSum = trendPoints.take(half).sumOf { it.revenue }
            val secondHalfSum = trendPoints.drop(half).sumOf { it.revenue }
            if (firstHalfSum > 0) {
                ((secondHalfSum - firstHalfSum) / firstHalfSum) * 100.0
            } else 0.0
        } else 0.0
    }

    Card(
        modifier = modifier
            .testTag("payment_revenue_trend_chart_card")
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Header: Title & Daily/Monthly Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ElegantPurpleDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (chartStyle == "AREA") Icons.AutoMirrored.Filled.ShowChart else Icons.Default.BarChart,
                                contentDescription = null,
                                tint = ElegantLilac,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Revenue Trends",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = if (selectedPeriodType == "MONTHLY") "Monthly collection overview" else "Daily merchant inflow analytics",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                // Daily vs Monthly Tab Segmented Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf("DAILY" to "Daily", "MONTHLY" to "Monthly").forEach { (type, label) ->
                        val isSelected = selectedPeriodType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (isSelected) ElegantPurpleDark else Color.Transparent)
                                .clickable {
                                    onPeriodTypeChange(type)
                                    selectedIndex = null
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("tab_trend_$type")
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) ElegantLilac else TextMuted,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Controls Ribbon: Period Filter Buttons & Chart Mode Switchers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Range selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (selectedPeriodType == "DAILY") {
                        listOf(7 to "7D", 14 to "14D", 30 to "30D", 90 to "90D").forEach { (days, label) ->
                            val isSelected = selectedDays == days
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ElegantPurpleDeep else Color.Transparent)
                                    .clickable {
                                        onDaysSelected(days)
                                        selectedIndex = null
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("filter_days_$label")
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) ElegantLilac else TextMuted,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    } else {
                        listOf(3 to "3M", 6 to "6M", 12 to "12M").forEach { (months, label) ->
                            val isSelected = selectedMonths == months
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ElegantPurpleDeep else Color.Transparent)
                                    .clickable {
                                        onMonthsSelected(months)
                                        selectedIndex = null
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("filter_months_$label")
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) ElegantLilac else TextMuted,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }

                // Chart Style & Metric Mode Switchers
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Metric Selector (Revenue vs Count vs Breakdown)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceElevated)
                            .padding(2.dp)
                    ) {
                        listOf("REVENUE" to currency, "COUNT" to "#", "BREAKDOWN" to "Mix").forEach { (metric, label) ->
                            val isSelected = chartMetric == metric
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ElegantPurpleDark else Color.Transparent)
                                    .clickable { onChartMetricChange(metric) }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                                    .testTag("metric_$metric")
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) ElegantLilac else TextMuted,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }

                    // Style Selector: Area Spline vs Bar Chart
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceElevated)
                            .padding(2.dp)
                    ) {
                        IconButton(
                            onClick = { onChartStyleChange("AREA") },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (chartStyle == "AREA") ElegantPurpleDark else Color.Transparent)
                                .testTag("btn_chart_style_area")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = "Area Spline",
                                tint = if (chartStyle == "AREA") ElegantLilac else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { onChartStyleChange("BAR") },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (chartStyle == "BAR") ElegantPurpleDark else Color.Transparent)
                                .testTag("btn_chart_style_bar")
                        ) {
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = "Bar Chart",
                                tint = if (chartStyle == "BAR") ElegantLilac else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recharts-Style Stats Summary Ribbon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceElevated)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PERIOD INFLOW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = TextMuted
                    )
                    Text(
                        text = "$currency${formatCompactNumber(totalRevenue)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (selectedPeriodType == "MONTHLY") "MONTHLY AVG" else "DAILY AVG",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = TextMuted
                    )
                    Text(
                        text = "$currency${formatCompactNumber(avgRevenue)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ElegantLilac
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "GROWTH TREND",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = TextMuted
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (trendGrowthPercent >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (trendGrowthPercent >= 0) ElegantGreen else ElegantRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${String.format(Locale.US, "%+.1f", trendGrowthPercent)}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (trendGrowthPercent >= 0) ElegantGreen else ElegantRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recharts-Grade Interactive Tooltip Card (Populated on Touch/Drag/Selection)
            val activePoint = selectedIndex?.let { trendPoints.getOrNull(it) }
            AnimatedVisibility(
                visible = activePoint != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (activePoint != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("chart_tooltip_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = ElegantPurpleDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantLilac.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.CalendarMonth,
                                        contentDescription = null,
                                        tint = ElegantLilacLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = activePoint.label,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }

                                val variance = if (avgRevenue > 0) {
                                    ((activePoint.revenue - avgRevenue) / avgRevenue) * 100.0
                                } else 0.0

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (variance >= 0) ElegantGreenContainer else ElegantRedContainer
                                ) {
                                    Text(
                                        text = "${String.format(Locale.US, "%+.1f", variance)}% vs avg",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (variance >= 0) ElegantGreen else ElegantRed,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "$currency${String.format(Locale.US, "%,.2f", activePoint.revenue)}",
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${activePoint.transactionCount} transactions • Avg $currency${String.format(Locale.US, "%,.0f", activePoint.avgTicket)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ElegantLilacLight
                                    )
                                }

                                // Payment breakdown chips
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (activePoint.upiRevenue > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0x33D0BCFF)
                                        ) {
                                            Text(
                                                text = "UPI $currency${formatCompactNumber(activePoint.upiRevenue)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ElegantLilac,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (activePoint.cardRevenue > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0x33A8C7FA)
                                        ) {
                                            Text(
                                                text = "Card $currency${formatCompactNumber(activePoint.cardRevenue)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ElegantBlue,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Chart Canvas (Spline Area & Segmented Bar Chart Engine)
            val maxRevenue = max(trendPoints.maxOfOrNull { it.revenue } ?: 1000.0, 1000.0)
            val maxCount = max(trendPoints.maxOfOrNull { it.transactionCount.toDouble() } ?: 10.0, 10.0)
            val effectiveMax = if (chartMetric == "COUNT") maxCount else maxRevenue

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .testTag("recharts_trend_canvas_box")
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(trendPoints) {
                            detectTapGestures(
                                onTap = { offset ->
                                    val count = trendPoints.size
                                    if (count > 0) {
                                        val step = size.width / count
                                        val idx = (offset.x / step).toInt().coerceIn(0, count - 1)
                                        selectedIndex = if (selectedIndex == idx) null else idx
                                    }
                                }
                            )
                        }
                        .pointerInput(trendPoints) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val count = trendPoints.size
                                if (count > 0) {
                                    val step = size.width / count
                                    val idx = (change.position.x / step).toInt().coerceIn(0, count - 1)
                                    selectedIndex = idx
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val paddingBottom = 24f
                    val paddingTop = 12f
                    val chartHeight = height - paddingBottom - paddingTop
                    val count = trendPoints.size

                    if (count == 0) return@Canvas

                    val stepX = width / count.coerceAtLeast(1)

                    // 1. Draw Cartesian Grid Lines (Recharts <CartesianGrid>)
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = paddingTop + chartHeight * (1f - i.toFloat() / gridLines)
                        drawLine(
                            color = Color.White.copy(alpha = 0.06f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    }

                    // 2. Draw Benchmark Reference Line (Recharts <ReferenceLine> for Avg)
                    if (avgRevenue > 0 && chartMetric == "REVENUE") {
                        val avgY = paddingTop + chartHeight * (1f - (avgRevenue.toFloat() / effectiveMax.toFloat()).coerceIn(0f, 1f))
                        drawLine(
                            color = ElegantLilac.copy(alpha = 0.4f),
                            start = Offset(0f, avgY),
                            end = Offset(width, avgY),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                        )
                    }

                    // 3. Render Chart Based on Mode: AREA vs BAR
                    if (chartStyle == "AREA") {
                        drawRechartsArea(
                            points = trendPoints,
                            metric = chartMetric,
                            effectiveMax = effectiveMax,
                            width = width,
                            chartHeight = chartHeight,
                            paddingTop = paddingTop,
                            selectedIndex = selectedIndex
                        )
                    } else {
                        drawRechartsBars(
                            points = trendPoints,
                            metric = chartMetric,
                            effectiveMax = effectiveMax,
                            width = width,
                            chartHeight = chartHeight,
                            paddingTop = paddingTop,
                            selectedIndex = selectedIndex
                        )
                    }

                    // 4. Draw Active Crosshair & Indicator
                    selectedIndex?.let { index ->
                        if (index in trendPoints.indices) {
                            val point = trendPoints[index]
                            val x = (index * stepX) + (stepX / 2f)
                            val value = if (chartMetric == "COUNT") point.transactionCount.toDouble() else point.revenue
                            val y = paddingTop + chartHeight * (1f - (value.toFloat() / effectiveMax.toFloat()).coerceIn(0f, 1f))

                            // Vertical crosshair line
                            drawLine(
                                color = ElegantLilac.copy(alpha = 0.8f),
                                start = Offset(x, paddingTop),
                                end = Offset(x, height - paddingBottom),
                                strokeWidth = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )

                            // Glowing halo and central point
                            drawCircle(
                                color = ElegantLilac.copy(alpha = 0.25f),
                                radius = 12f,
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = ElegantLilac,
                                radius = 6f,
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }

            // X-Axis Date / Month Labels (Recharts <XAxis>)
            if (trendPoints.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val labelCount = min(trendPoints.size, 5)
                    val step = (trendPoints.size - 1).coerceAtLeast(1) / (labelCount - 1).coerceAtLeast(1)
                    
                    for (i in 0 until labelCount) {
                        val index = (i * step).coerceAtMost(trendPoints.size - 1)
                        val point = trendPoints[index]
                        Text(
                            text = point.shortLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (selectedIndex == index) ElegantLilac else TextMuted
                        )
                    }
                }
            }

            // Bottom Recharts-Style Interactive Legend
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    LegendItem(color = ElegantLilac, label = "UPI QR")
                    LegendItem(color = ElegantBlue, label = "Card")
                    LegendItem(color = ElegantAmber, label = "Bank")
                    LegendItem(color = ElegantGreen, label = "Cash/Other")
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = TextMuted
        )
    }
}

/**
 * Recharts Spline Area Drawing with Cubic Bézier smoothing and linear alpha gradient fill
 */
private fun DrawScope.drawRechartsArea(
    points: List<ChartTrendPoint>,
    metric: String,
    effectiveMax: Double,
    width: Float,
    chartHeight: Float,
    paddingTop: Float,
    selectedIndex: Int?
) {
    val count = points.size
    val stepX = width / count.coerceAtLeast(1)

    val coordinates = points.mapIndexed { index, point ->
        val x = (index * stepX) + (stepX / 2f)
        val value = if (metric == "COUNT") point.transactionCount.toDouble() else point.revenue
        val ratio = (value.toFloat() / effectiveMax.toFloat()).coerceIn(0f, 1f)
        val y = paddingTop + chartHeight * (1f - ratio)
        Offset(x, y)
    }

    if (coordinates.isEmpty()) return

    // Create smooth curved path
    val strokePath = Path()
    val fillPath = Path()

    strokePath.moveTo(coordinates.first().x, coordinates.first().y)
    fillPath.moveTo(coordinates.first().x, paddingTop + chartHeight)
    fillPath.lineTo(coordinates.first().x, coordinates.first().y)

    for (i in 0 until coordinates.size - 1) {
        val current = coordinates[i]
        val next = coordinates[i + 1]
        val controlPoint1 = Offset(current.x + (next.x - current.x) / 2f, current.y)
        val controlPoint2 = Offset(current.x + (next.x - current.x) / 2f, next.y)

        strokePath.cubicTo(
            controlPoint1.x, controlPoint1.y,
            controlPoint2.x, controlPoint2.y,
            next.x, next.y
        )
        fillPath.cubicTo(
            controlPoint1.x, controlPoint1.y,
            controlPoint2.x, controlPoint2.y,
            next.x, next.y
        )
    }

    fillPath.lineTo(coordinates.last().x, paddingTop + chartHeight)
    fillPath.close()

    // 1. Draw Gradient Area Fill (defs linearGradient)
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                ElegantLilac.copy(alpha = 0.35f),
                ElegantPurpleDark.copy(alpha = 0.15f),
                Color.Transparent
            ),
            startY = paddingTop,
            endY = paddingTop + chartHeight
        )
    )

    // 2. Draw Smooth Spline Stroke Line
    drawPath(
        path = strokePath,
        color = ElegantLilac,
        style = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 3. Draw Data Point Dots (Recharts <Dot>)
    coordinates.forEachIndexed { index, coord ->
        val isSelected = selectedIndex == index
        if (count <= 30 || isSelected || index % 5 == 0) {
            drawCircle(
                color = if (isSelected) ElegantGreen else ElegantLilac,
                radius = if (isSelected) 5.dp.toPx() else 3.dp.toPx(),
                center = coord
            )
            drawCircle(
                color = DarkSurface,
                radius = if (isSelected) 2.5.dp.toPx() else 1.5.dp.toPx(),
                center = coord
            )
        }
    }
}

/**
 * Recharts Bar Chart drawing with multi-channel stacked segments or single metric gradient
 */
private fun DrawScope.drawRechartsBars(
    points: List<ChartTrendPoint>,
    metric: String,
    effectiveMax: Double,
    width: Float,
    chartHeight: Float,
    paddingTop: Float,
    selectedIndex: Int?
) {
    val count = points.size
    val stepX = width / count.coerceAtLeast(1)
    val barWidth = (stepX * 0.7f).coerceIn(4f, 32f)

    points.forEachIndexed { index, point ->
        val x = (index * stepX) + (stepX - barWidth) / 2f
        val isSelected = selectedIndex == index

        if (metric == "BREAKDOWN" && point.revenue > 0) {
            // Stacked multi-channel bar
            val total = point.revenue.toFloat()
            val upiRatio = (point.upiRevenue.toFloat() / total)
            val cardRatio = (point.cardRevenue.toFloat() / total)
            val bankRatio = (point.bankRevenue.toFloat() / total)
            val otherRatio = (point.otherRevenue.toFloat() / total)

            val totalBarHeight = (point.revenue.toFloat() / effectiveMax.toFloat()) * chartHeight
            val upiHeight = totalBarHeight * upiRatio
            val cardHeight = totalBarHeight * cardRatio
            val bankHeight = totalBarHeight * bankRatio
            val otherHeight = totalBarHeight * otherRatio

            var currY = paddingTop + chartHeight

            // Draw Other segment
            if (otherHeight > 0) {
                currY -= otherHeight
                drawRoundRect(
                    color = ElegantGreen,
                    topLeft = Offset(x, currY),
                    size = Size(barWidth, otherHeight)
                )
            }
            // Draw Bank segment
            if (bankHeight > 0) {
                currY -= bankHeight
                drawRoundRect(
                    color = ElegantAmber,
                    topLeft = Offset(x, currY),
                    size = Size(barWidth, bankHeight)
                )
            }
            // Draw Card segment
            if (cardHeight > 0) {
                currY -= cardHeight
                drawRoundRect(
                    color = ElegantBlue,
                    topLeft = Offset(x, currY),
                    size = Size(barWidth, cardHeight)
                )
            }
            // Draw UPI segment (top with rounded corners)
            if (upiHeight > 0) {
                currY -= upiHeight
                drawRoundRect(
                    color = if (isSelected) ElegantLilacLight else ElegantLilac,
                    topLeft = Offset(x, currY),
                    size = Size(barWidth, upiHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        } else {
            // Single Metric Gradient Bar
            val value = if (metric == "COUNT") point.transactionCount.toDouble() else point.revenue
            val barHeight = ((value.toFloat() / effectiveMax.toFloat()) * chartHeight).coerceAtLeast(4f)
            val y = (paddingTop + chartHeight) - barHeight

            val brush = if (isSelected) {
                Brush.verticalGradient(listOf(ElegantGreen, Color(0xFF2E6325)))
            } else {
                Brush.verticalGradient(listOf(ElegantLilac, ElegantPurpleDark))
            }

            drawRoundRect(
                brush = brush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
    }
}

private fun formatCompactNumber(number: Double): String {
    return when {
        number >= 10000000 -> String.format(Locale.US, "%.2f Cr", number / 10000000.0)
        number >= 100000 -> String.format(Locale.US, "%.1f L", number / 100000.0)
        number >= 1000 -> String.format(Locale.US, "%.1fk", number / 1000.0)
        else -> String.format(Locale.US, "%.0f", number)
    }
}
