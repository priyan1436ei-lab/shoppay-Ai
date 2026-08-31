package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KpiCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopPayUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    state: ShopPayUiState,
    onExportCsvClick: () -> Unit,
    onShareReport: () -> Unit
) {
    val currency = state.profile.currency
    val metrics = state.metrics
    val analytics = state.analytics

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Financial Reports",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(
                        onClick = onExportCsvClick,
                        modifier = Modifier.testTag("btn_reports_export_csv")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export CSV", tint = ElegantLilac)
                    }
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
                .testTag("reports_screen"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Executive Statement Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(22.dp))
                        .testTag("executive_summary_card"),
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
                                    text = state.profile.shopName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Monthly Financial Statement",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ElegantPurpleDark
                            ) {
                                Text(
                                    text = "Active Merchant",
                                    color = ElegantLilac,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = DarkOutlineSubtle)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Total Revenue", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Text(
                                    "$currency${String.format(Locale.US, "%,.2f", metrics.monthlyRevenue)}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = ElegantGreen
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Transactions", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Text(
                                    "${analytics.totalTransactions}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Avg Ticket Size", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Text(
                                    "$currency${String.format(Locale.US, "%,.2f", analytics.averageTransactionValue)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Target Achievement", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Text(
                                    "${metrics.targetProgressPercent}% of Goal",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (metrics.targetProgressPercent >= 50) ElegantGreen else ElegantAmber
                                )
                            }
                        }
                    }
                }
            }

            // Payment Channel Breakdown Table
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(22.dp)),
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
                        Text(
                            text = "Channel Audit Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        ReportTableRow(
                            channel = "UPI / QR Code",
                            revenue = "$currency${String.format(Locale.US, "%,.2f", metrics.upiTotal)}",
                            share = "${metrics.upiPercent}%",
                            color = ElegantLilac
                        )

                        ReportTableRow(
                            channel = "Debit / Credit Cards",
                            revenue = "$currency${String.format(Locale.US, "%,.2f", metrics.cardTotal)}",
                            share = "${metrics.cardPercent}%",
                            color = ElegantBlue
                        )

                        ReportTableRow(
                            channel = "Bank Transfers / NEFT",
                            revenue = "$currency${String.format(Locale.US, "%,.2f", metrics.bankTotal)}",
                            share = "${metrics.bankPercent}%",
                            color = ElegantAmber
                        )
                    }
                }
            }

            // Quick Actions: Export & Share
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onShareReport,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantLilac),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_share_report")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Report")
                    }

                    Button(
                        onClick = onExportCsvClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantLilac,
                            contentColor = ElegantPurpleDeep
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_export_statement")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV", fontWeight = FontWeight.Bold)
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
private fun ReportTableRow(
    channel: String,
    revenue: String,
    share: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                    .background(color, RoundedCornerShape(3.dp))
            )
            Text(channel, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                revenue,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextSecondary
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Text(
                    text = share,
                    color = color,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
