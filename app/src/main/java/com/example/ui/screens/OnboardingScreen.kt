package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.data.model.ShopCategory
import com.example.data.model.ShopProfile
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    currentProfile: ShopProfile,
    onCompleteOnboarding: (ShopProfile) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var shopName by remember { mutableStateOf(currentProfile.shopName) }
    var ownerName by remember { mutableStateOf(currentProfile.ownerName) }
    var selectedCategory by remember { mutableStateOf(ShopCategory.GROCERY) }
    var selectedCurrency by remember { mutableStateOf("₹") }
    var selectedTarget by remember { mutableStateOf(400000.0) }
    var customTargetText by remember { mutableStateOf("400000") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp)
            .testTag("onboarding_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Step Progress Indicator
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shop Setup Wizard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Step $step of 4",
                    style = MaterialTheme.typography.labelMedium,
                    color = ElegantLilac,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (step / 4f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = ElegantLilac,
                trackColor = DarkSurfaceElevated
            )
        }

        // Step Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (step) {
                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "What is your shop called?",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "We'll personalize your financial reports and intelligence with your store name.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        OutlinedTextField(
                            value = shopName,
                            onValueChange = { shopName = it },
                            label = { Text("Shop Name") },
                            placeholder = { Text("e.g. Sharma Grocery & Mart", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_onboarding_shop_name")
                        )

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Owner Name") },
                            placeholder = { Text("e.g. Rajesh Sharma", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_onboarding_owner_name")
                        )
                    }
                }
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Select Business Category",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "This tunes the AI insight models for your retail sector.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ShopCategory.entries.forEach { category ->
                                val isSelected = selectedCategory == category
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedCategory = category }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) ElegantLilac else Color(0x14FFFFFF),
                                            shape = RoundedCornerShape(14.dp)
                                        ),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) ElegantPurpleDark else DarkSurface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = category.displayName,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) ElegantLilac else TextPrimary
                                        )
                                        if (isSelected) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElegantLilac)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Select Preferred Currency",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Default is Indian Rupee (₹ INR).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        listOf("₹" to "Indian Rupee (INR ₹)", "$" to "US Dollar (USD $)", "€" to "Euro (EUR €)", "£" to "British Pound (GBP £)").forEach { (symbol, label) ->
                            val isSelected = selectedCurrency == symbol
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCurrency = symbol }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) ElegantLilac else Color(0x14FFFFFF),
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) ElegantPurpleDark else DarkSurface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                        color = if (isSelected) ElegantLilac else TextPrimary
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElegantLilac)
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Monthly Revenue Target",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Set a goal to track your monthly progress and AI milestone recommendations.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        listOf(100000.0 to "$selectedCurrency 1,00,000", 250000.0 to "$selectedCurrency 2,50,000", 400000.0 to "$selectedCurrency 4,00,000", 1000000.0 to "$selectedCurrency 10,00,000").forEach { (amount, label) ->
                            val isSelected = selectedTarget == amount
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedTarget = amount
                                        customTargetText = amount.toLong().toString()
                                    }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) ElegantLilac else Color(0x14FFFFFF),
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) ElegantPurpleDark else DarkSurface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                        color = if (isSelected) ElegantLilac else TextPrimary
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElegantLilac)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = customTargetText,
                            onValueChange = {
                                customTargetText = it
                                it.toDoubleOrNull()?.let { num -> selectedTarget = num }
                            },
                            label = { Text("Custom Monthly Goal ($selectedCurrency)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_custom_target")
                        )
                    }
                }
            }
        }

        // Bottom Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (step > 1) {
                OutlinedButton(
                    onClick = { step-- },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElegantLilac),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Back")
                }
            }

            Button(
                onClick = {
                    if (step < 4) {
                        step++
                    } else {
                        val newProfile = currentProfile.copy(
                            shopName = shopName.ifBlank { "My Retail Store" },
                            ownerName = ownerName.ifBlank { "Shop Owner" },
                            category = selectedCategory.name,
                            currency = selectedCurrency,
                            monthlyRevenueTarget = selectedTarget
                        )
                        onCompleteOnboarding(newProfile)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElegantLilac,
                    contentColor = ElegantPurpleDeep
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("btn_onboarding_next")
            ) {
                Text(
                    text = if (step == 4) "Finish & Open Dashboard" else "Next Step",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
