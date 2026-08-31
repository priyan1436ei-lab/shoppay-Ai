package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionStatus
import com.example.ui.components.TransactionRowCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.ShopPayUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    state: ShopPayUiState,
    onSearchChange: (String) -> Unit,
    onPaymentFilterChange: (String?) -> Unit,
    onStatusFilterChange: (String?) -> Unit,
    onUnusualFilterToggle: (Boolean) -> Unit,
    onSortChange: (String) -> Unit,
    onAddTransactionClick: () -> Unit,
    onImportCsvClick: () -> Unit,
    onExportCsvClick: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit
) {
    val currency = state.profile.currency
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(
                        onClick = onExportCsvClick,
                        modifier = Modifier.testTag("btn_export_csv")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export CSV", tint = ElegantLilac)
                    }
                    IconButton(
                        onClick = onImportCsvClick,
                        modifier = Modifier.testTag("btn_txns_import_csv")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Import CSV", tint = ElegantLilac)
                    }
                    IconButton(
                        onClick = onAddTransactionClick,
                        modifier = Modifier.testTag("btn_txns_add")
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Transaction", tint = ElegantLilac)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
                .testTag("transactions_screen")
        ) {
            // Search Bar & Sort Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search customer, ID, note, amount...", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                    trailingIcon = {
                        if (state.searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_txns_search")
                )

                // Sort Dropdown Button
                Box {
                    OutlinedIconButton(
                        onClick = { sortMenuExpanded = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("btn_txns_sort")
                    ) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort", tint = ElegantLilac)
                    }

                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Newest Date First", color = TextPrimary) },
                            onClick = {
                                onSortChange("DATE_DESC")
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Oldest Date First", color = TextPrimary) },
                            onClick = {
                                onSortChange("DATE_ASC")
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Amount: High to Low", color = TextPrimary) },
                            onClick = {
                                onSortChange("AMOUNT_DESC")
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Amount: Low to High", color = TextPrimary) },
                            onClick = {
                                onSortChange("AMOUNT_ASC")
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Filter Chips Horizontal Scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All Filter
                val isAllSelected = state.filterPaymentMethod == null && state.filterStatus == null && !state.filterOnlyUnusual
                FilterChip(
                    selected = isAllSelected,
                    onClick = {
                        onPaymentFilterChange(null)
                        onStatusFilterChange(null)
                        onUnusualFilterToggle(false)
                    },
                    label = { Text("All (${state.allTransactions.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElegantPurpleDark,
                        selectedLabelColor = ElegantLilac,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Needs Review / Anomaly Chip
                FilterChip(
                    selected = state.filterOnlyUnusual,
                    onClick = { onUnusualFilterToggle(!state.filterOnlyUnusual) },
                    label = { Text("⚠️ Needs Review (${state.unusualTransactions.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElegantAmberContainer,
                        selectedLabelColor = ElegantAmber,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Payment Method Chips
                listOf(PaymentMethod.UPI, PaymentMethod.CARD, PaymentMethod.BANK_TRANSFER).forEach { method ->
                    val isSelected = state.filterPaymentMethod == method.name
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onPaymentFilterChange(if (isSelected) null else method.name)
                        },
                        label = { Text(method.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElegantPurpleDark,
                            selectedLabelColor = ElegantLilac,
                            containerColor = DarkSurfaceElevated,
                            labelColor = TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Status Chips
                listOf(TransactionStatus.SUCCESSFUL, TransactionStatus.PENDING, TransactionStatus.REFUNDED).forEach { status ->
                    val isSelected = state.filterStatus == status.name
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onStatusFilterChange(if (isSelected) null else status.name)
                        },
                        label = { Text(status.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElegantPurpleDark,
                            selectedLabelColor = ElegantLilac,
                            containerColor = DarkSurfaceElevated,
                            labelColor = TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Results count banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${state.filteredTransactions.size} transactions",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
                val totalFilteredAmount = state.filteredTransactions.sumOf { it.amount }
                Text(
                    text = "Total: $currency${String.format(java.util.Locale.US, "%,.2f", totalFilteredAmount)}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }

            // Transactions List
            if (state.filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No matching transactions found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Try adjusting your search query or active filter chips.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.filteredTransactions, key = { it.id }) { txn ->
                        TransactionRowCard(
                            txn = txn,
                            currency = currency,
                            onClick = { onTransactionClick(txn) }
                        )
                    }
                }
            }
        }
    }
}
