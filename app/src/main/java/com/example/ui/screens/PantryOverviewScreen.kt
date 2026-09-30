package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodItem
import com.example.data.model.StorageLocation
import com.example.ui.components.GeminiRecipeSection
import com.example.ui.theme.ExpiredRed
import com.example.ui.theme.ExpiringWarning
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.FreshGreen
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

enum class PantryViewMode {
    OVERVIEW_EXPIRING_SOON,
    FULL_INVENTORY
}

enum class UrgentSubFilter {
    ALL_EXPIRING,
    CRITICAL_TODAY,
    TOMORROW,
    IN_2_TO_3_DAYS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryOverviewScreen(
    viewModel: MainViewModel,
    onNavigateToRecipes: () -> Unit,
    showAddDialogInitially: Boolean = false,
    onDismissInitialAddDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val inStockItems by viewModel.inStockItems.collectAsState()

    var viewMode by remember { mutableStateOf(PantryViewMode.OVERVIEW_EXPIRING_SOON) }
    var urgentSubFilter by remember { mutableStateOf(UrgentSubFilter.ALL_EXPIRING) }
    var selectedLocation by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog states
    var showAddEditDialog by remember { mutableStateOf(showAddDialogInitially) }
    var editingItem by remember { mutableStateOf<FoodItem?>(null) }
    var wastedItemToLog by remember { mutableStateOf<FoodItem?>(null) }
    var donationItemToLog by remember { mutableStateOf<FoodItem?>(null) }

    // Automatic filtering: items expiring within the next 3 days (daysUntilExpiry <= 3)
    val expiringWithin3Days = remember(inStockItems) {
        inStockItems.filter { it.daysUntilExpiry() <= 3 }.sortedBy { it.daysUntilExpiry() }
    }

    // Critical counts
    val criticalTodayItems = remember(expiringWithin3Days) {
        expiringWithin3Days.filter { it.daysUntilExpiry() <= 0 }
    }
    val tomorrowItems = remember(expiringWithin3Days) {
        expiringWithin3Days.filter { it.daysUntilExpiry() == 1 }
    }
    val in2To3DaysItems = remember(expiringWithin3Days) {
        expiringWithin3Days.filter { it.daysUntilExpiry() in 2..3 }
    }

    // Metrics for items at risk
    val totalValueAtRisk = remember(expiringWithin3Days) {
        expiringWithin3Days.sumOf { it.priceEstimate }
    }
    val co2ImpactKgAtRisk = remember(expiringWithin3Days) {
        // Average 0.75 kg CO2e per rescued food item
        expiringWithin3Days.size * 0.75
    }

    // Filtered items based on sub-filters
    val displayedUrgentItems = remember(expiringWithin3Days, urgentSubFilter, selectedLocation, searchQuery) {
        expiringWithin3Days.filter { item ->
            val matchesSubFilter = when (urgentSubFilter) {
                UrgentSubFilter.ALL_EXPIRING -> true
                UrgentSubFilter.CRITICAL_TODAY -> item.daysUntilExpiry() <= 0
                UrgentSubFilter.TOMORROW -> item.daysUntilExpiry() == 1
                UrgentSubFilter.IN_2_TO_3_DAYS -> item.daysUntilExpiry() in 2..3
            }
            val matchesLocation = selectedLocation == "ALL" || item.storageLocation.equals(selectedLocation, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true) || item.category.contains(searchQuery, ignoreCase = true)
            matchesSubFilter && matchesLocation && matchesSearch
        }
    }

    // Full inventory filtered items (when in FULL_INVENTORY mode)
    val fullInventoryItems = remember(inStockItems, selectedLocation, searchQuery) {
        inStockItems.filter { item ->
            val matchesLocation = selectedLocation == "ALL" || item.storageLocation.equals(selectedLocation, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true) || item.category.contains(searchQuery, ignoreCase = true)
            matchesLocation && matchesSearch
        }.sortedBy { it.daysUntilExpiry() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("pantry_overview_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar
            item {
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
                            Text(
                                text = "Pantry Overview",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (expiringWithin3Days.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (criticalTodayItems.isNotEmpty()) ExpiredRed else ExpiringWarning
                                ) {
                                    Text(
                                        text = "${expiringWithin3Days.size} at risk",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Automatic 3-Day Expiry Radar & Waste Prevention",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(38.dp)
                            .clickable {
                                editingItem = null
                                showAddEditDialog = true
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Food",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Top View Mode Switcher: [ ⚠️ Expiring (<= 3 Days) ] vs [ 📦 All Inventory ]
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (viewMode == PantryViewMode.OVERVIEW_EXPIRING_SOON)
                                (if (criticalTodayItems.isNotEmpty()) ExpiredRed else ForestGreenPrimary)
                            else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewMode = PantryViewMode.OVERVIEW_EXPIRING_SOON }
                                .testTag("tab_overview_expiring")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (criticalTodayItems.isNotEmpty()) Icons.Default.Warning else Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = if (viewMode == PantryViewMode.OVERVIEW_EXPIRING_SOON) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Expiring <= 3 Days (${expiringWithin3Days.size})",
                                    fontSize = 12.sp,
                                    fontWeight = if (viewMode == PantryViewMode.OVERVIEW_EXPIRING_SOON) FontWeight.Bold else FontWeight.Medium,
                                    color = if (viewMode == PantryViewMode.OVERVIEW_EXPIRING_SOON) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (viewMode == PantryViewMode.FULL_INVENTORY) ForestGreenPrimary else Color.Transparent,
                            modifier = Modifier
                                .weight(0.9f)
                                .clickable { viewMode = PantryViewMode.FULL_INVENTORY }
                                .testTag("tab_full_inventory")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Kitchen,
                                    contentDescription = null,
                                    tint = if (viewMode == PantryViewMode.FULL_INVENTORY) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "All Pantry (${inStockItems.size})",
                                    fontSize = 12.sp,
                                    fontWeight = if (viewMode == PantryViewMode.FULL_INVENTORY) FontWeight.Bold else FontWeight.Medium,
                                    color = if (viewMode == PantryViewMode.FULL_INVENTORY) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // --- VIEW MODE 1: PANTRY OVERVIEW (EXPIRING WITHIN 3 DAYS) ---
            if (viewMode == PantryViewMode.OVERVIEW_EXPIRING_SOON) {
                // 1. VISUAL WARNING HERO ALERT BANNER
                item {
                    ImmediateVisualWarningBanner(
                        expiringCount = expiringWithin3Days.size,
                        criticalCount = criticalTodayItems.size,
                        tomorrowCount = tomorrowItems.size,
                        totalValueAtRisk = totalValueAtRisk,
                        onCookUrgent = onNavigateToRecipes
                    )
                }

                // 2. METRICS CARDS ROW
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PantryStatCard(
                            label = "At Risk (<3d)",
                            value = "${expiringWithin3Days.size} items",
                            icon = Icons.Default.Alarm,
                            accentColor = if (criticalTodayItems.isNotEmpty()) ExpiredRed else ExpiringWarning,
                            modifier = Modifier.weight(1f)
                        )
                        PantryStatCard(
                            label = "Value at Risk",
                            value = "$${String.format(Locale.US, "%.2f", totalValueAtRisk)}",
                            icon = Icons.Default.AttachMoney,
                            accentColor = Color(0xFFD97706),
                            modifier = Modifier.weight(1f)
                        )
                        PantryStatCard(
                            label = "CO2 Impact",
                            value = "${String.format(Locale.US, "%.1f", co2ImpactKgAtRisk)} kg",
                            icon = Icons.Default.Co2,
                            accentColor = ForestGreenPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. SUB-FILTER CHIPS (All <=3d, Critical Today, Tomorrow, 2-3d)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filter by Urgency Window:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                            )
                            if (expiringWithin3Days.isNotEmpty()) {
                                Text(
                                    text = "${displayedUrgentItems.size} displayed",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (criticalTodayItems.isNotEmpty()) ExpiredRed else ExpiringWarning
                                )
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = urgentSubFilter == UrgentSubFilter.ALL_EXPIRING,
                                    onClick = { urgentSubFilter = UrgentSubFilter.ALL_EXPIRING },
                                    label = { Text("All Urgent (${expiringWithin3Days.size})") },
                                    leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = urgentSubFilter == UrgentSubFilter.CRITICAL_TODAY,
                                    onClick = { urgentSubFilter = UrgentSubFilter.CRITICAL_TODAY },
                                    label = { Text("🚨 Today / Past (${criticalTodayItems.size})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ExpiredRed.copy(alpha = 0.18f),
                                        selectedLabelColor = ExpiredRed
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = urgentSubFilter == UrgentSubFilter.TOMORROW,
                                    onClick = { urgentSubFilter = UrgentSubFilter.TOMORROW },
                                    label = { Text("⚠️ Tomorrow (${tomorrowItems.size})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ExpiringWarning.copy(alpha = 0.18f),
                                        selectedLabelColor = Color(0xFFB45309)
                                    )
                                )
                            }
                            item {
                                FilterChip(
                                    selected = urgentSubFilter == UrgentSubFilter.IN_2_TO_3_DAYS,
                                    onClick = { urgentSubFilter = UrgentSubFilter.IN_2_TO_3_DAYS },
                                    label = { Text("⏳ 2-3 Days (${in2To3DaysItems.size})") }
                                )
                            }
                        }

                        // Location Filter Chips (Fridge / Pantry / Freezer)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val locations = listOf("ALL" to "All Storage", "FRIDGE" to "🧊 Fridge", "PANTRY" to "🥫 Pantry", "FREEZER" to "❄️ Freezer")
                            items(locations) { (locKey, locLabel) ->
                                val count = if (locKey == "ALL") expiringWithin3Days.size else expiringWithin3Days.count { it.storageLocation.equals(locKey, ignoreCase = true) }
                                val isSelected = selectedLocation == locKey
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.clickable { selectedLocation = locKey }
                                ) {
                                    Text(
                                        text = "$locLabel ($count)",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. GEMINI AI ZERO-WASTE CHEF INTEGRATION
                item {
                    GeminiRecipeSection(viewModel = viewModel)
                }

                // 5. EXPIRING ITEMS LIST WITH PROMINENT VISUAL WARNINGS
                if (displayedUrgentItems.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = FreshGreen.copy(alpha = 0.15f),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = FreshGreen,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = if (expiringWithin3Days.isEmpty()) "🎉 No Items Expiring Soon!" else "No items matching filter",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (expiringWithin3Days.isEmpty())
                                        "All items in your pantry are fresh and safe beyond the 3-day window. Keep up the great food management!"
                                    else
                                        "Select 'All Urgent' above or switch storage locations to view items.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                    textAlign = TextAlign.Center
                                )

                                Button(
                                    onClick = { viewMode = PantryViewMode.FULL_INVENTORY },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                    modifier = Modifier.padding(top = 6.dp)
                                ) {
                                    Icon(Icons.Default.Kitchen, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("View All Pantry Inventory")
                                }
                            }
                        }
                    }
                } else {
                    items(displayedUrgentItems, key = { it.id }) { item ->
                        UrgentFoodItemCard(
                            item = item,
                            onConsumed = { viewModel.markItemConsumed(item) },
                            onLogWasted = { wastedItemToLog = item },
                            onDonate = { donationItemToLog = item },
                            onEdit = {
                                editingItem = item
                                showAddEditDialog = true
                            },
                            onDelete = { viewModel.deleteFoodItem(item) },
                            onCookRecipe = onNavigateToRecipes
                        )
                    }
                }
            } else {
                // --- VIEW MODE 2: FULL INVENTORY WITH SEARCH & EXPIRY STATS ---
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search all inventory items...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("full_inventory_search"),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val locations = listOf("ALL" to "All Places (${inStockItems.size})", "FRIDGE" to "🧊 Fridge", "PANTRY" to "🥫 Pantry", "FREEZER" to "❄️ Freezer")
                        items(locations) { (locKey, locLabel) ->
                            val isSelected = selectedLocation == locKey
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) ForestGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedLocation = locKey }
                            ) {
                                Text(
                                    text = locLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                if (fullInventoryItems.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📦", fontSize = 40.sp)
                                Text("No items found", fontWeight = FontWeight.Bold)
                                Button(onClick = { showAddEditDialog = true }) {
                                    Text("Add First Item")
                                }
                            }
                        }
                    }
                } else {
                    items(fullInventoryItems, key = { it.id }) { item ->
                        UrgentFoodItemCard(
                            item = item,
                            onConsumed = { viewModel.markItemConsumed(item) },
                            onLogWasted = { wastedItemToLog = item },
                            onDonate = { donationItemToLog = item },
                            onEdit = {
                                editingItem = item
                                showAddEditDialog = true
                            },
                            onDelete = { viewModel.deleteFoodItem(item) },
                            onCookRecipe = onNavigateToRecipes
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button to Add Food Item
        FloatingActionButton(
            onClick = {
                editingItem = null
                showAddEditDialog = true
            },
            containerColor = ForestGreenPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_food_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Food Item", modifier = Modifier.size(28.dp))
        }

        // Add/Edit Food Dialog
        if (showAddEditDialog) {
            AddEditFoodDialog(
                initialItem = editingItem,
                onDismiss = {
                    showAddEditDialog = false
                    editingItem = null
                    onDismissInitialAddDialog()
                },
                onSave = { savedItem ->
                    if (editingItem == null) {
                        viewModel.addFoodItem(savedItem)
                    } else {
                        viewModel.updateFoodItem(savedItem)
                    }
                    showAddEditDialog = false
                    editingItem = null
                    onDismissInitialAddDialog()
                }
            )
        }

        // Log Waste Reason Dialog
        if (wastedItemToLog != null) {
            val item = wastedItemToLog!!
            var reason by remember { mutableStateOf("Expired before eating") }
            AlertDialog(
                onDismissRequest = { wastedItemToLog = null },
                title = { Text("Log Food as Wasted") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Logging wasted items helps you discover shopping patterns and reduce future waste.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            label = { Text("Reason (e.g. Spoiled, Left too long)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.markItemWasted(item, reason)
                            wastedItemToLog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpiredRed)
                    ) {
                        Text("Confirm Wasted")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { wastedItemToLog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Donate Surplus Food Dialog
        if (donationItemToLog != null) {
            val item = donationItemToLog!!
            var destination by remember { mutableStateOf("Hope Harvest Community Shelter") }
            AlertDialog(
                onDismissRequest = { donationItemToLog = null },
                title = { Text("Donate Surplus Food") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Share this item with a local charity or community pantry before it expires.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = destination,
                            onValueChange = { destination = it },
                            label = { Text("Drop-off Location / Organization") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.markItemDonated(item, destination)
                            donationItemToLog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) {
                        Text("Mark as Donated")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { donationItemToLog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// -------------------------------------------------------------
// VISUAL WARNING COMPONENTS FOR IMMEDIATE USE ITEMS
// -------------------------------------------------------------

@Composable
fun ImmediateVisualWarningBanner(
    expiringCount: Int,
    criticalCount: Int,
    tomorrowCount: Int,
    totalValueAtRisk: Double,
    onCookUrgent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_warning")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val isCritical = criticalCount > 0
    val isWarning = expiringCount > 0

    val bannerBrush = if (isCritical) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF991B1B), // Dark Crimson
                Color(0xFFDC2626), // Strong Red
                Color(0xFFB91C1C)
            )
        )
    } else if (isWarning) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFB45309), // Amber Deep
                Color(0xFFD97706), // Warm Amber
                Color(0xFFF59E0B)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF14532D), // Deep Green
                Color(0xFF15803D), // Forest Green
                Color(0xFF16A34A)  // Fresh Green
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("visual_warning_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bannerBrush)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Top alert row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = if (isCritical) pulseAlpha * 0.35f else 0.25f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isCritical) Icons.Default.NotificationsActive else if (isWarning) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = if (isCritical)
                                    "🚨 IMMEDIATE ACTION REQUIRED"
                                else if (isWarning)
                                    "⚠️ EXPIRING WITHIN 3 DAYS"
                                else
                                    "🎉 PANTRY IS FRESH & SAFE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (isCritical)
                                    "$criticalCount ${if (criticalCount == 1) "item expires" else "items expire"} today! Use immediately."
                                else if (isWarning)
                                    "$expiringCount items need attention soon ($tomorrowCount tomorrow)."
                                else
                                    "Zero items expiring in the next 72 hours.",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    if (totalValueAtRisk > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", totalValueAtRisk)} at risk",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (isWarning) {
                    // Call to action button to rescue with recipe
                    Button(
                        onClick = onCookUrgent,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = if (isCritical) Color(0xFF991B1B) else Color(0xFFB45309)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("btn_cook_urgent_recipes")
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCritical) "👨‍🍳 Rescue Critical Items with Recipes Now" else "👨‍🍳 Cook Expiring Items with Zero-Waste Recipes",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantryStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

// -------------------------------------------------------------
// INDIVIDUAL URGENT FOOD ITEM CARD WITH VISUAL WARNINGS
// -------------------------------------------------------------

@Composable
fun UrgentFoodItemCard(
    item: FoodItem,
    onConsumed: () -> Unit,
    onLogWasted: () -> Unit,
    onDonate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCookRecipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val daysLeft = item.daysUntilExpiry()
    val isCritical = daysLeft <= 0
    val isTomorrow = daysLeft == 1
    val isSoon = daysLeft in 2..3
    val isFresh = daysLeft > 3

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    // Urgency card colors and border
    val cardBgColor = when {
        isCritical -> Color(0xFFFEF2F2) // Light red tint
        isTomorrow -> Color(0xFFFFF7ED) // Light orange tint
        isSoon -> Color(0xFFFFFBEB)     // Light yellow tint
        else -> MaterialTheme.colorScheme.surface
    }

    val cardBorderColor = when {
        isCritical -> ExpiredRed
        isTomorrow -> Color(0xFFF97316)
        isSoon -> ExpiringWarning
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    // Shelf life progress calculation (simulated purchase to expiry progress)
    val now = System.currentTimeMillis()
    val totalShelfLifeMs = max(1L, item.expiryDate - item.purchaseDate)
    val elapsedMs = now - item.purchaseDate
    val progressFloat = (elapsedMs.toFloat() / totalShelfLifeMs.toFloat()).coerceIn(0.1f, 1.0f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("urgent_item_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = androidx.compose.foundation.BorderStroke(if (isCritical || isTomorrow) 1.5.dp else 1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCritical) 3.dp else 1.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // PROMINENT VISUAL WARNING BADGE HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Urgency warning pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isCritical -> ExpiredRed
                        isTomorrow -> Color(0xFFEA580C)
                        isSoon -> Color(0xFFD97706)
                        else -> FreshGreen
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isCritical -> Icons.Default.PriorityHigh
                                isTomorrow -> Icons.Default.Alarm
                                isSoon -> Icons.Default.HourglassBottom
                                else -> Icons.Default.Check
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = when {
                                daysLeft < 0 -> "EXPIRED ${-daysLeft} DAYS AGO"
                                daysLeft == 0 -> "🚨 EXPIRES TODAY! IMMEDIATE USE"
                                daysLeft == 1 -> "⚠️ EXPIRES TOMORROW (24h left)"
                                daysLeft in 2..3 -> "⏳ EXPIRES IN $daysLeft DAYS"
                                else -> "FRESH ($daysLeft days left)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.3.sp
                        )
                    }
                }

                // Value at risk badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = "💰 $${String.format(Locale.US, "%.2f", item.priceEstimate)} at risk",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Overflow menu
                Box {
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Find Recipes") },
                            onClick = { menuExpanded = false; onCookRecipe() },
                            leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Donate Surplus") },
                            onClick = { menuExpanded = false; onDonate() },
                            leadingIcon = { Icon(Icons.Default.VolunteerActivism, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Item") },
                            onClick = { menuExpanded = false; onEdit() },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Log as Wasted") },
                            onClick = { menuExpanded = false; onLogWasted() },
                            leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, tint = ExpiredRed) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Item", color = ExpiredRed) },
                            onClick = { menuExpanded = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ExpiredRed) }
                        )
                    }
                }
            }

            // Food Name & Quantity Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${item.quantity} ${item.unit}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = when (item.storageLocation) {
                                    "FRIDGE" -> "🧊 Fridge"
                                    "PANTRY" -> "🥫 Pantry"
                                    "FREEZER" -> "❄️ Freezer"
                                    else -> item.storageLocation
                                },
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "• Category: ${item.category}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            // Depletion Progress Bar (visual indicator of remaining shelf life)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Shelf Life Depletion:",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Expires ${dateFormat.format(Date(item.expiryDate))}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCritical) ExpiredRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                LinearProgressIndicator(
                    progress = { progressFloat },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = when {
                        isCritical -> ExpiredRed
                        isTomorrow -> Color(0xFFF97316)
                        isSoon -> ExpiringWarning
                        else -> FreshGreen
                    },
                    trackColor = Color.Black.copy(alpha = 0.08f),
                    strokeCap = StrokeCap.Round
                )
            }

            if (item.notes.isNotBlank()) {
                Text(
                    text = "💡 ${item.notes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 14.sp
                )
            }

            // DIRECT RESCUE ACTIONS ROW (Cook, Donate, Consumed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // "Cook Recipe"
                Button(
                    onClick = onCookRecipe,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("btn_cook_recipe_${item.id}")
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cook", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // "Donate Surplus"
                OutlinedButton(
                    onClick = onDonate,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("btn_donate_${item.id}")
                ) {
                    Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF1D4ED8))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Donate", fontSize = 11.sp, color = Color(0xFF1D4ED8))
                }

                // "Mark Consumed"
                Button(
                    onClick = onConsumed,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(36.dp)
                        .testTag("btn_mark_consumed_${item.id}")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Consumed", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
