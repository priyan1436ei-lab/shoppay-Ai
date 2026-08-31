package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    transactionToEdit: TransactionEntity? = null,
    currency: String = "₹",
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    onDelete: ((TransactionEntity) -> Unit)? = null
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
    val now = Date()

    var amountText by remember { mutableStateOf(transactionToEdit?.amount?.toString() ?: "") }
    var customerNameText by remember { mutableStateOf(transactionToEdit?.customerName ?: "") }
    var txnIdText by remember {
        mutableStateOf(
            transactionToEdit?.transactionId ?: "TXN-${System.currentTimeMillis() % 100000}"
        )
    }
    var selectedMethod by remember {
        mutableStateOf(transactionToEdit?.paymentMethod ?: PaymentMethod.UPI.name)
    }
    var selectedStatus by remember {
        mutableStateOf(transactionToEdit?.status ?: TransactionStatus.SUCCESSFUL.name)
    }
    var selectedCategory by remember {
        mutableStateOf(transactionToEdit?.category ?: "Sales")
    }
    var dateText by remember {
        mutableStateOf(transactionToEdit?.transactionDate ?: dateFormat.format(now))
    }
    var timeText by remember {
        mutableStateOf(transactionToEdit?.transactionTime ?: timeFormat.format(now))
    }
    var noteText by remember { mutableStateOf(transactionToEdit?.customerNote ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp)
                .border(1.dp, Color(0x33D0BCFF), RoundedCornerShape(24.dp))
                .testTag("add_edit_transaction_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (transactionToEdit != null) "Edit Transaction" else "Add New Transaction",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElegantRedContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = ElegantRed,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Amount ($currency)") },
                    placeholder = { Text("e.g. 850") },
                    leadingIcon = {
                        Text(
                            currency,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ElegantLilac,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_transaction_amount"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Customer Name
                OutlinedTextField(
                    value = customerNameText,
                    onValueChange = { customerNameText = it },
                    label = { Text("Customer Name") },
                    placeholder = { Text("e.g. Rajesh Kumar, Walk-in Customer") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = ElegantLilac)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_name"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Transaction ID
                OutlinedTextField(
                    value = txnIdText,
                    onValueChange = { txnIdText = it },
                    label = { Text("Transaction ID") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_transaction_id"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Payment Method Selector
                Text(
                    text = "Payment Channel",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(PaymentMethod.UPI, PaymentMethod.CARD, PaymentMethod.BANK_TRANSFER).forEach { method ->
                        val isSelected = selectedMethod == method.name
                        OutlinedButton(
                            onClick = { selectedMethod = method.name },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) ElegantPurpleDark else DarkSurfaceElevated,
                                contentColor = if (isSelected) ElegantLilac else TextSecondary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (isSelected) ElegantLilac else DarkOutlineSubtle
                                )
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                method.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                // Status Selector
                Text(
                    text = "Transaction Status",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(TransactionStatus.SUCCESSFUL, TransactionStatus.PENDING, TransactionStatus.REFUNDED).forEach { status ->
                        val isSelected = selectedStatus == status.name
                        OutlinedButton(
                            onClick = { selectedStatus = status.name },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) ElegantPurpleDark else DarkSurfaceElevated,
                                contentColor = if (isSelected) ElegantLilac else TextSecondary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (isSelected) ElegantLilac else DarkOutlineSubtle
                                )
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                status.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                // Date & Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = { timeText = it },
                        label = { Text("Time (HH:mm)") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Customer Note
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Customer Note / Item description") },
                    placeholder = { Text("e.g. Grocery basket, Online scan") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (transactionToEdit != null && onDelete != null) {
                        OutlinedButton(
                            onClick = { onDelete(transactionToEdit) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete")
                        }
                    }

                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                errorMessage = "Please enter a valid amount greater than 0"
                                return@Button
                            }
                            if (txnIdText.isBlank()) {
                                errorMessage = "Transaction ID is required"
                                return@Button
                            }

                            val isUnusual = amount >= 15000.0
                            val finalCustomer = customerNameText.trim().ifBlank { "Walk-in Customer" }
                            val txn = transactionToEdit?.copy(
                                amount = amount,
                                customerName = finalCustomer,
                                transactionId = txnIdText.trim(),
                                paymentMethod = selectedMethod,
                                status = selectedStatus,
                                category = selectedCategory,
                                description = noteText.trim(),
                                transactionDate = dateText.trim(),
                                transactionTime = timeText.trim(),
                                customerNote = noteText.trim(),
                                isUnusual = isUnusual
                            ) ?: TransactionEntity(
                                transactionId = txnIdText.trim(),
                                customerName = finalCustomer,
                                amount = amount,
                                category = selectedCategory,
                                description = noteText.trim(),
                                paymentMethod = selectedMethod,
                                status = selectedStatus,
                                transactionDate = dateText.trim(),
                                transactionTime = timeText.trim(),
                                customerNote = noteText.trim(),
                                isUnusual = isUnusual
                            )

                            onSave(txn)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantLilac,
                            contentColor = ElegantPurpleDeep
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_save_transaction")
                    ) {
                        Text("Save Transaction", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CsvImportDialog(
    validationResult: ImportValidationResult?,
    onValidateCsv: (String) -> Unit,
    onConfirmImport: (List<TransactionEntity>) -> Unit,
    onDismiss: () -> Unit
) {
    val sampleCsv = """
        transaction_id,amount,date,time,payment_method,status,category,note
        TXN-20001,450.0,2026-08-31,14:20,UPI,SUCCESSFUL,Sales,Milk & Dairy Pack
        TXN-20002,1250.0,2026-08-31,14:45,CARD,SUCCESSFUL,Sales,Monthly Dry Fruits
        TXN-20003,780.0,2026-08-31,15:10,UPI,SUCCESSFUL,Sales,Spices & Oil Can
        TXN-20004,3200.0,2026-08-31,15:30,BANK_TRANSFER,SUCCESSFUL,Sales,Restaurant Order
        TXN-20005,180.0,2026-08-31,16:00,UPI,SUCCESSFUL,Sales,Cold Drinks & Chips
    """.trimIndent()

    var csvText by remember { mutableStateOf(sampleCsv) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .padding(vertical = 16.dp)
                .border(1.dp, Color(0x33D0BCFF), RoundedCornerShape(24.dp))
                .testTag("csv_import_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Import Transactions CSV",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Upload or paste statement data",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Validation Status Banner
                if (validationResult != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Validation Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ResultPill(label = "Ready to Import", count = validationResult.validTransactions.size, color = ElegantGreen)
                                ResultPill(label = "Duplicates", count = validationResult.duplicateCount, color = ElegantAmber)
                                ResultPill(label = "Invalid Rows", count = validationResult.invalidCount, color = ElegantRed)
                            }

                            if (validationResult.errorMessages.isNotEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = DarkOutlineSubtle)
                                Text(
                                    text = "Issues Detected:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ElegantRed
                                )
                                validationResult.errorMessages.take(3).forEach { err ->
                                    Text(
                                        text = "• $err",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // CSV input area
                OutlinedTextField(
                    value = csvText,
                    onValueChange = { csvText = it },
                    label = { Text("CSV Content") },
                    placeholder = { Text("transaction_id,amount,date,time,payment_method,status") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("input_csv_content"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onValidateCsv(csvText) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantLilac),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_validate_csv")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Validate CSV")
                    }

                    if (validationResult != null && validationResult.validTransactions.isNotEmpty()) {
                        Button(
                            onClick = { onConfirmImport(validationResult.validTransactions) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElegantLilac,
                                contentColor = ElegantPurpleDeep
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_confirm_import")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import (${validationResult.validTransactions.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultPill(label: String, count: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
fun TransactionDetailDialog(
    txn: TransactionEntity,
    currency: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .border(1.dp, Color(0x33D0BCFF), RoundedCornerShape(24.dp))
                .testTag("transaction_detail_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Big Amount Callout
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Received Amount",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Text(
                            text = "$currency${String.format(Locale.US, "%,.2f", txn.amount)}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (txn.status == TransactionStatus.SUCCESSFUL.name) ElegantGreen else TextPrimary
                        )
                        if (txn.isUnusual) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ElegantAmberContainer,
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Text(
                                    text = "⚠️ Flagged: Needs Review",
                                    color = ElegantAmber,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // Key Info Rows
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(label = "Customer", value = txn.customerName.ifBlank { "Walk-in Customer" })
                    DetailRow(label = "Transaction ID", value = txn.transactionId)
                    DetailRow(label = "Payment Channel", value = txn.paymentMethod)
                    DetailRow(label = "Status", value = txn.status)
                    DetailRow(label = "Category", value = txn.category)
                    DetailRow(label = "Date & Time", value = "${txn.transactionDate} at ${txn.transactionTime}")
                    if (txn.customerNote.isNotBlank()) {
                        DetailRow(label = "Note", value = txn.customerNote)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete")
                    }
                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElegantLilac,
                            contentColor = ElegantPurpleDeep
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Edit", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary
        )
    }
}
