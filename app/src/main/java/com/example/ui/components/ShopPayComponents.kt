package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.max

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String? = null,
    badgeText: String? = null,
    isPositiveBadge: Boolean = true,
    icon: ImageVector,
    accentColor: Color = ElegantLilac,
    modifier: Modifier = Modifier,
    testTag: String = "kpi_card"
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary
            )

            if (badgeText != null || subtitle != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (badgeText != null) {
                        val badgeBg = if (isPositiveBadge) ElegantGreenContainer else ElegantRedContainer
                        val badgeColor = if (isPositiveBadge) ElegantGreen else ElegantRed
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeBg
                        ) {
                            Text(
                                text = badgeText,
                                color = badgeColor,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveRevenueChart(
    points: List<DailyRevenuePoint>,
    currency: String,
    selectedPeriod: Int,
    onPeriodSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier
            .testTag("revenue_chart_card")
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Revenue Trends",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Interactive daily collections",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                // Filter Pills: 7D, 30D, 90D
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf(7 to "7D", 30 to "30D", 90 to "3M").forEach { (days, label) ->
                        val isSelected = selectedPeriod == days
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ElegantPurpleDark else Color.Transparent)
                                .clickable { onPeriodSelected(days) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("filter_period_$label")
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) ElegantLilac else TextMuted,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected point inspector tooltip
            val activePoint = selectedPointIndex?.let { points.getOrNull(it) }
            AnimatedVisibility(visible = activePoint != null) {
                if (activePoint != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ElegantPurpleDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = activePoint.date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantLilacLight
                                )
                                Text(
                                    text = "$currency${String.format(Locale.US, "%,.2f", activePoint.revenue)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "${activePoint.transactionCount} transactions",
                                style = MaterialTheme.typography.bodySmall,
                                color = ElegantLilacLight
                            )
                        }
                    }
                }
            }

            // Custom Crisp Canvas Chart
            val maxRevenue = max(points.maxOfOrNull { it.revenue } ?: 1000.0, 1000.0)
            val barCount = points.size
            val barBrush = Brush.verticalGradient(
                colors = listOf(ElegantLilac, ElegantPurpleDark)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(top = 8.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            selectedPointIndex = null
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val barSpacing = width / barCount.coerceAtLeast(1)
                    val barWidth = (barSpacing * 0.65f).coerceIn(4f, 24f)

                    // Draw grid lines
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val y = height * (i.toFloat() / gridSteps)
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f
                        )
                    }

                    // Draw Bars
                    points.forEachIndexed { index, point ->
                        val barHeight = (point.revenue.toFloat() / maxRevenue.toFloat()) * (height * 0.85f)
                        val x = index * barSpacing + (barSpacing - barWidth) / 2f
                        val y = height - barHeight

                        val isSelected = selectedPointIndex == index

                        drawRoundRect(
                            brush = if (isSelected) Brush.verticalGradient(listOf(ElegantGreen, Color(0xFF2E6325))) else barBrush,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight.coerceAtLeast(4f)),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                }
            }

            // X-Axis Labels
            if (points.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = points.first().label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    if (points.size > 2) {
                        Text(
                            text = points[points.size / 2].label,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Text(
                        text = points.last().label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun DateRangeFilterBar(
    selectedRange: DashboardDateRange,
    onRangeSelected: (DashboardDateRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("date_range_filter_bar"),
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1FFFFFFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DashboardDateRange.entries.forEach { range ->
                val isSelected = range == selectedRange
                val tag = when (range) {
                    DashboardDateRange.TODAY -> "date_range_filter_today"
                    DashboardDateRange.THIS_WEEK -> "date_range_filter_this_week"
                    DashboardDateRange.THIS_MONTH -> "date_range_filter_this_month"
                }
                val icon = when (range) {
                    DashboardDateRange.TODAY -> Icons.Default.Today
                    DashboardDateRange.THIS_WEEK -> Icons.Default.DateRange
                    DashboardDateRange.THIS_MONTH -> Icons.Default.CalendarMonth
                }

                Surface(
                    selected = isSelected,
                    onClick = { onRangeSelected(range) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) ElegantPurpleDark else Color.Transparent,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ElegantLilac.copy(alpha = 0.5f)) else null,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(tag)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) ElegantLilac else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = range.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) Color.White else TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodDonutCard(
    metrics: DashboardMetrics,
    currency: String,
    periodMetrics: PeriodRevenueMetrics = metrics.periodMetrics,
    modifier: Modifier = Modifier
) {
    val displayMetrics = if (periodMetrics.transactionCount > 0) periodMetrics else null
    val upiPct = displayMetrics?.upiPercent ?: metrics.upiPercent
    val cardPct = displayMetrics?.cardPercent ?: metrics.cardPercent
    val bankPct = displayMetrics?.bankPercent ?: metrics.bankPercent
    val otherPct = (100 - upiPct - cardPct - bankPct).coerceAtLeast(0)

    val upiTot = displayMetrics?.upiTotal ?: metrics.upiTotal
    val cardTot = displayMetrics?.cardTotal ?: metrics.cardTotal
    val bankTot = displayMetrics?.bankTotal ?: metrics.bankTotal

    Card(
        modifier = modifier
            .testTag("payment_method_card")
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Payment Method Share",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Channels used (${periodMetrics.dateRange.displayName})",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElegantPurpleDark
                ) {
                    Text(
                        text = periodMetrics.dateRange.shortName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = ElegantLilac,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Multi-segment horizontal distribution bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
            ) {
                val upiWeight = upiPct.coerceAtLeast(if (cardPct == 0 && bankPct == 0 && otherPct == 0) 100 else 1).toFloat()
                val cardWeight = cardPct.coerceAtLeast(0).toFloat()
                val bankWeight = bankPct.coerceAtLeast(0).toFloat()
                val otherWeight = otherPct.coerceAtLeast(0).toFloat()

                Box(
                    modifier = Modifier
                        .weight(upiWeight)
                        .fillMaxHeight()
                        .background(ElegantLilac)
                )
                if (cardWeight > 0) {
                    Box(
                        modifier = Modifier
                            .weight(cardWeight)
                            .fillMaxHeight()
                            .background(ElegantBlue)
                    )
                }
                if (bankWeight > 0) {
                    Box(
                        modifier = Modifier
                            .weight(bankWeight)
                            .fillMaxHeight()
                            .background(ElegantAmber)
                    )
                }
                if (otherWeight > 0) {
                    Box(
                        modifier = Modifier
                            .weight(otherWeight)
                            .fillMaxHeight()
                            .background(ElegantPurpleDark)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend & breakdowns
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PaymentMethodRowItem(
                    name = "UPI / QR Code",
                    percent = "$upiPct%",
                    amount = "$currency${String.format(Locale.US, "%,.0f", upiTot)}",
                    color = ElegantLilac
                )
                PaymentMethodRowItem(
                    name = "Debit / Credit Cards",
                    percent = "$cardPct%",
                    amount = "$currency${String.format(Locale.US, "%,.0f", cardTot)}",
                    color = ElegantBlue
                )
                PaymentMethodRowItem(
                    name = "Bank Transfers / NEFT",
                    percent = "$bankPct%",
                    amount = "$currency${String.format(Locale.US, "%,.0f", bankTot)}",
                    color = ElegantAmber
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodRowItem(
    name: String,
    percent: String,
    amount: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = percent,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextSecondary
            )
        }
    }
}

@Composable
fun AiInsightCard(
    insight: AiInsightEntity,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val borderColor = when (insight.severity) {
        "POSITIVE" -> ElegantGreen.copy(alpha = 0.4f)
        "WARNING" -> ElegantAmber.copy(alpha = 0.4f)
        else -> ElegantLilac.copy(alpha = 0.4f)
    }

    val iconVector = when (insight.severity) {
        "POSITIVE" -> Icons.Default.TrendingUp
        "WARNING" -> Icons.Default.Warning
        else -> Icons.Default.AutoAwesome
    }

    val iconColor = when (insight.severity) {
        "POSITIVE" -> ElegantGreen
        "WARNING" -> ElegantAmber
        else -> ElegantLilac
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .testTag("ai_insight_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = "AI Insight",
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "AI BUSINESS INSIGHT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = iconColor
                    )
                }

                if (insight.metricValue.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = iconColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = insight.metricValue,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = iconColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = insight.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = insight.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 20.sp
            )

            if (insight.actionSuggestion.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Tip",
                            tint = ElegantLilac,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = insight.actionSuggestion,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRowCard(
    txn: TransactionEntity,
    currency: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val methodIcon = when (txn.paymentMethod) {
        PaymentMethod.UPI.name -> Icons.Default.QrCodeScanner
        PaymentMethod.CARD.name -> Icons.Default.CreditCard
        PaymentMethod.BANK_TRANSFER.name -> Icons.Default.AccountBalance
        else -> Icons.Default.Payments
    }

    val (statusColor, statusBg) = when (txn.status) {
        TransactionStatus.SUCCESSFUL.name -> ElegantGreen to ElegantGreenContainer
        TransactionStatus.PENDING.name -> ElegantAmber to ElegantAmberContainer
        TransactionStatus.REFUNDED.name -> ElegantLilac to ElegantPurpleDark
        else -> ElegantRed to ElegantRedContainer
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(20.dp))
            .testTag("transaction_item_${txn.transactionId}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x4D4F378B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = methodIcon,
                        contentDescription = txn.paymentMethod,
                        tint = ElegantLilac,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = txn.customerName.ifBlank { txn.transactionId },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        if (txn.isUnusual) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ElegantAmberContainer
                            ) {
                                Text(
                                    text = "Needs Review",
                                    color = ElegantAmber,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${txn.transactionId} • ${txn.transactionDate} ${txn.transactionTime} • ${txn.paymentMethod}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    val desc = txn.description.ifBlank { txn.customerNote }
                    if (desc.isNotBlank() || txn.category.isNotBlank()) {
                        Text(
                            text = if (desc.isNotBlank()) "${txn.category} • $desc" else txn.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = ElegantLilacLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$currency${String.format(Locale.US, "%,.2f", txn.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (txn.status == TransactionStatus.SUCCESSFUL.name) ElegantGreen else TextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBg
                ) {
                    Text(
                        text = TransactionStatus.fromString(txn.status).displayName,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TargetProgressCard(
    metrics: DashboardMetrics,
    currency: String,
    periodMetrics: PeriodRevenueMetrics = metrics.periodMetrics,
    modifier: Modifier = Modifier
) {
    val isMonthly = periodMetrics.dateRange == DashboardDateRange.THIS_MONTH
    val title = if (isMonthly) "Monthly Revenue Target" else "${periodMetrics.dateRange.displayName} Target"
    val targetVal = periodMetrics.periodTarget
    val revVal = periodMetrics.revenue
    val progressPct = periodMetrics.targetProgressPercent
    val remaining = periodMetrics.targetRemaining

    Card(
        modifier = modifier
            .testTag("target_progress_card")
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Target: $currency${String.format(Locale.US, "%,.0f", targetVal)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ElegantPurpleDark
                ) {
                    Text(
                        text = "$progressPct%",
                        color = ElegantLilac,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (progressPct / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = ElegantLilac,
                trackColor = DarkSurfaceElevated
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current: $currency${String.format(Locale.US, "%,.0f", revVal)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = TextSecondary
                )
                Text(
                    text = "Remaining: $currency${String.format(Locale.US, "%,.0f", remaining)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = if (remaining == 0.0) ElegantGreen else TextMuted
                )
            }
        }
    }
}
