package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionStatus
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * 'Recent Transactions' component displaying a scrollable list of individual payments,
 * including customer name, formatted timestamp, amount, payment channel, and status badge.
 */
@Composable
fun RecentTransactionsComponent(
    transactions: List<TransactionEntity>,
    currency: String,
    modifier: Modifier = Modifier,
    title: String = "Recent Transactions",
    subtitle: String? = null,
    showFilters: Boolean = true,
    showSearch: Boolean = false,
    maxDisplayCount: Int = 15,
    onTransactionClick: (TransactionEntity) -> Unit = {},
    onViewAllClick: (() -> Unit)? = null,
    onAddPaymentClick: (() -> Unit)? = null
) {
    var selectedMethodFilter by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Filter logic
    val filteredTransactions = remember(transactions, selectedMethodFilter, searchQuery) {
        transactions.filter { txn ->
            val matchesFilter = selectedMethodFilter == null || txn.paymentMethod.equals(selectedMethodFilter, ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                txn.customerName.contains(searchQuery, ignoreCase = true) ||
                txn.transactionId.contains(searchQuery, ignoreCase = true) ||
                txn.category.contains(searchQuery, ignoreCase = true) ||
                txn.description.contains(searchQuery, ignoreCase = true) ||
                txn.customerNote.contains(searchQuery, ignoreCase = true)
            }
            matchesFilter && matchesSearch
        }.take(maxDisplayCount)
    }

    val totalVolumeInView = remember(filteredTransactions) {
        filteredTransactions.filter { it.status == TransactionStatus.SUCCESSFUL.name }.sumOf { it.amount }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recent_transactions_component")
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title, Payment Counter Badge, and View All Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ElegantPurpleDark
                        ) {
                            Text(
                                text = "${transactions.size}",
                                color = ElegantLilac,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = subtitle ?: "Latest customer payments & settlements",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (onAddPaymentClick != null) {
                        IconButton(
                            onClick = onAddPaymentClick,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElegantPurpleDark)
                                .testTag("btn_recent_add_payment")
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Payment",
                                tint = ElegantLilac,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (onViewAllClick != null) {
                        TextButton(
                            onClick = onViewAllClick,
                            modifier = Modifier.testTag("btn_recent_view_all")
                        ) {
                            Text(
                                text = "View All",
                                color = ElegantLilac,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = ElegantLilac,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Optional Search Bar
            if (showSearch) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by customer, category, note...", color = TextMuted, style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated,
                        focusedBorderColor = ElegantLilac,
                        unfocusedBorderColor = DarkOutlineSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_recent_search")
                )
            }

            // Quick Filter Chips (All, UPI, Card, Bank Transfer, Wallet, Cash)
            if (showFilters && transactions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedMethodFilter == null,
                        onClick = { selectedMethodFilter = null },
                        label = { Text("All Channels (${transactions.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElegantPurpleDark,
                            selectedLabelColor = ElegantLilac,
                            containerColor = DarkSurfaceElevated,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedMethodFilter == null,
                            borderColor = if (selectedMethodFilter == null) ElegantLilac else DarkOutlineSubtle
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    listOf(
                        PaymentMethod.UPI to "UPI",
                        PaymentMethod.CARD to "Card",
                        PaymentMethod.BANK_TRANSFER to "Bank",
                        PaymentMethod.WALLET to "Wallet"
                    ).forEach { (method, shortLabel) ->
                        val isSelected = selectedMethodFilter.equals(method.name, ignoreCase = true)
                        val count = transactions.count { it.paymentMethod.equals(method.name, ignoreCase = true) }
                        if (count > 0) {
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedMethodFilter = if (isSelected) null else method.name
                                },
                                label = { Text("$shortLabel ($count)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElegantPurpleDark,
                                    selectedLabelColor = ElegantLilac,
                                    containerColor = DarkSurfaceElevated,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) ElegantLilac else DarkOutlineSubtle
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // Scrollable List of Individual Payments
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedMethodFilter != null) "No matching transactions found" else "No recent payments recorded in database",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        if (onAddPaymentClick != null && transactions.isEmpty()) {
                            Button(
                                onClick = onAddPaymentClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElegantLilac,
                                    contentColor = ElegantPurpleDeep
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text("Record First Payment", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredTransactions.forEach { txn ->
                        RecentPaymentItemCard(
                            txn = txn,
                            currency = currency,
                            onClick = { onTransactionClick(txn) }
                        )
                    }
                }

                // Mini summary footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Showing ${filteredTransactions.size} payment${if (filteredTransactions.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = "Total: $currency${String.format(Locale.US, "%,.2f", totalVolumeInView)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ElegantGreen
                    )
                }
            }
        }
    }
}

/**
 * Individual Payment Row Card with Customer Name, Timestamp, Amount, and Channel badge.
 */
@Composable
fun RecentPaymentItemCard(
    txn: TransactionEntity,
    currency: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val methodIcon = when (txn.paymentMethod) {
        PaymentMethod.UPI.name -> Icons.Default.QrCodeScanner
        PaymentMethod.CARD.name -> Icons.Default.CreditCard
        PaymentMethod.BANK_TRANSFER.name -> Icons.Default.AccountBalance
        PaymentMethod.WALLET.name -> Icons.Default.AccountBalanceWallet
        PaymentMethod.CASH.name -> Icons.Default.Payments
        else -> Icons.Default.Paid
    }

    val (statusColor, statusBg) = when (txn.status) {
        TransactionStatus.SUCCESSFUL.name -> ElegantGreen to ElegantGreenContainer
        TransactionStatus.PENDING.name -> ElegantAmber to ElegantAmberContainer
        TransactionStatus.REFUNDED.name -> ElegantLilac to ElegantPurpleDark
        else -> ElegantRed to ElegantRedContainer
    }

    // Dynamic avatar background and initial generation
    val customerInitials = remember(txn.customerName) {
        val parts = txn.customerName.trim().split(" ").filter { it.isNotBlank() }
        if (parts.size >= 2) {
            "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        } else if (parts.isNotEmpty()) {
            parts[0].take(2).uppercase()
        } else {
            "WC"
        }
    }

    val avatarGradient = remember(txn.customerName) {
        val hash = abs(txn.customerName.hashCode())
        when (hash % 4) {
            0 -> Brush.linearGradient(listOf(Color(0xFF6750A4), Color(0xFF9A82DB)))
            1 -> Brush.linearGradient(listOf(Color(0xFF381E72), Color(0xFF7D5260)))
            2 -> Brush.linearGradient(listOf(Color(0xFF24441F), Color(0xFF5E8D4E)))
            else -> Brush.linearGradient(listOf(Color(0xFF1B3A60), Color(0xFF4C7EB8)))
        }
    }

    // Format formatted timestamp (e.g., "Today, 11:15" or "2026-08-31 • 11:15")
    val formattedTimestamp = remember(txn.transactionDate, txn.transactionTime) {
        formatFriendlyTimestamp(txn.transactionDate, txn.transactionTime)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(16.dp))
            .testTag("recent_payment_item_${txn.transactionId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Customer Avatar + Name + Timestamp + Payment Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Customer Avatar Circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(avatarGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = customerInitials,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White
                    )
                }

                // Info Column: Customer Name, Timestamp, Channel & Category/Description
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Customer Name + Needs Review tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = txn.customerName.ifBlank { "Walk-in Customer" },
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (txn.isUnusual) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ElegantAmberContainer
                            ) {
                                Text(
                                    text = "Alert",
                                    color = ElegantAmber,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    // Timestamp & Payment Method Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = methodIcon,
                            contentDescription = txn.paymentMethod,
                            tint = ElegantLilac,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${txn.paymentMethod} • $formattedTimestamp",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Category and Description
                    val detailText = when {
                        txn.description.isNotBlank() && txn.category.isNotBlank() -> "${txn.category} • ${txn.description}"
                        txn.description.isNotBlank() -> txn.description
                        txn.customerNote.isNotBlank() -> "${txn.category} • ${txn.customerNote}"
                        txn.category.isNotBlank() -> txn.category
                        else -> txn.transactionId
                    }

                    Text(
                        text = detailText,
                        style = MaterialTheme.typography.labelSmall,
                        color = ElegantLilacLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: Amount and Status Pill
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "${if (txn.status == TransactionStatus.REFUNDED.name) "-" else "+"}$currency${String.format(Locale.US, "%,.2f", txn.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = when (txn.status) {
                        TransactionStatus.SUCCESSFUL.name -> ElegantGreen
                        TransactionStatus.REFUNDED.name -> ElegantLilac
                        TransactionStatus.PENDING.name -> ElegantAmber
                        else -> ElegantRed
                    }
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

/**
 * Formats date and time into user-friendly representation like "Today, 10:42 AM", "Yesterday, 3:15 PM"
 */
private fun formatFriendlyTimestamp(dateStr: String, timeStr: String): String {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = dateFormat.format(Date())
        
        val calYesterday = Calendar.getInstance()
        calYesterday.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(calYesterday.time)

        val prefix = when (dateStr) {
            todayStr -> "Today"
            yesterdayStr -> "Yesterday"
            else -> {
                val parsed = dateFormat.parse(dateStr)
                if (parsed != null) {
                    val displayFormat = SimpleDateFormat("d MMM", Locale.US)
                    displayFormat.format(parsed)
                } else {
                    dateStr
                }
            }
        }

        "$prefix, $timeStr"
    } catch (_: Exception) {
        "$dateStr $timeStr"
    }
}

