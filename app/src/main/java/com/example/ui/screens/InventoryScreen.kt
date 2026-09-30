package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpiryUrgency
import com.example.data.model.FoodCategory
import com.example.data.model.FoodItem
import com.example.data.model.StorageLocation
import com.example.ui.components.ExpiryUrgencyBadge
import com.example.ui.theme.ExpiredRed
import com.example.ui.theme.ExpiringWarning
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.FreshGreen
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: MainViewModel,
    onNavigateToRecipes: () -> Unit,
    showAddDialogInitially: Boolean = false,
    onDismissInitialAddDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val inStockItems by viewModel.inStockItems.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf("ALL") }
    var selectedUrgencyFilter by remember { mutableStateOf("ALL") }

    // Dialog state
    var showAddEditDialog by remember { mutableStateOf(showAddDialogInitially) }
    var editingItem by remember { mutableStateOf<FoodItem?>(null) }
    var wastedItemToLog by remember { mutableStateOf<FoodItem?>(null) }
    var donationItemToLog by remember { mutableStateOf<FoodItem?>(null) }

    // Filter items
    val filteredItems = inStockItems.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true) ||
                item.notes.contains(searchQuery, ignoreCase = true)

        val matchesLocation = selectedLocation == "ALL" || item.storageLocation.equals(selectedLocation, ignoreCase = true)

        val urgency = item.getExpiryUrgency()
        val matchesUrgency = when (selectedUrgencyFilter) {
            "ALL" -> true
            "EXPIRING" -> urgency == ExpiryUrgency.EXPIRING_SOON || urgency == ExpiryUrgency.CRITICAL_TODAY
            "FRESH" -> urgency == ExpiryUrgency.FRESH
            "EXPIRED" -> urgency == ExpiryUrgency.EXPIRED
            else -> true
        }

        matchesSearch && matchesLocation && matchesUrgency
    }

    val expiringCount = inStockItems.count {
        val u = it.getExpiryUrgency()
        u == ExpiryUrgency.EXPIRING_SOON || u == ExpiryUrgency.CRITICAL_TODAY || u == ExpiryUrgency.EXPIRED
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header & Search
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Food Inventory",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${inStockItems.size} items in pantry & fridge",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    if (expiringCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ExpiringWarning.copy(alpha = 0.18f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = ExpiringWarning,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "$expiringCount Expiring Soon",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by item name or category...") },
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
                        .testTag("inventory_search_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                // Storage location filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedLocation == "ALL",
                            onClick = { selectedLocation = "ALL" },
                            label = { Text("All Places (${inStockItems.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedLocation == "FRIDGE",
                            onClick = { selectedLocation = "FRIDGE" },
                            label = { Text("Fridge (${inStockItems.count { it.storageLocation == "FRIDGE" }})") },
                            leadingIcon = { Icon(Icons.Default.Kitchen, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedLocation == "PANTRY",
                            onClick = { selectedLocation = "PANTRY" },
                            label = { Text("Pantry (${inStockItems.count { it.storageLocation == "PANTRY" }})") },
                            leadingIcon = { Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedLocation == "FREEZER",
                            onClick = { selectedLocation = "FREEZER" },
                            label = { Text("Freezer (${inStockItems.count { it.storageLocation == "FREEZER" }})") }
                        )
                    }
                }

                // Status Urgency filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedUrgencyFilter == "ALL",
                            onClick = { selectedUrgencyFilter = "ALL" },
                            label = { Text("All Status") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedUrgencyFilter == "EXPIRING",
                            onClick = { selectedUrgencyFilter = "EXPIRING" },
                            label = { Text("⚠️ Urgent / Expiring Soon") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ExpiringWarning.copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFFB45309)
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedUrgencyFilter == "FRESH",
                            onClick = { selectedUrgencyFilter = "FRESH" },
                            label = { Text("🟢 Fresh") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedUrgencyFilter == "EXPIRED",
                            onClick = { selectedUrgencyFilter = "EXPIRED" },
                            label = { Text("🔴 Past Expiry") }
                        )
                    }
                }
            }

            // Items List
            if (filteredItems.isEmpty()) {
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
                        Text(text = "📦", fontSize = 48.sp)
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching food items" else "Your inventory is empty",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "Try searching for a different term" else "Tap '+' below to add fresh groceries and start tracking expiration dates.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = {
                                editingItem = null
                                showAddEditDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Food Item")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        FoodItemCard(
                            item = item,
                            onConsumed = { viewModel.markItemConsumed(item) },
                            onLogWasted = { wastedItemToLog = item },
                            onDonate = { donationItemToLog = item },
                            onEdit = {
                                editingItem = item
                                showAddEditDialog = true
                            },
                            onDelete = { viewModel.deleteFoodItem(item) },
                            onFindRecipes = onNavigateToRecipes
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Floating Action Button to Add Food
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
                            label = { Text("Reason (e.g. Moldy, Left too long, Overbought)") },
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

        // Donate Dialog
        if (donationItemToLog != null) {
            val item = donationItemToLog!!
            var destination by remember { mutableStateOf("City Care Community Fridge") }
            AlertDialog(
                onDismissRequest = { donationItemToLog = null },
                title = { Text("Donate Surplus Food") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Share this item with a local charity or community pantry.",
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
                        }
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

@Composable
fun FoodItemCard(
    item: FoodItem,
    onConsumed: () -> Unit,
    onLogWasted: () -> Unit,
    onDonate: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFindRecipes: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val daysLeft = item.daysUntilExpiry()
    val urgency = item.getExpiryUrgency()
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("food_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = item.storageLocation,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "${item.quantity} ${item.unit} • ${item.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Text(
                        text = "Expires: ${dateFormat.format(Date(item.expiryDate))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ExpiryUrgencyBadge(
                        daysLeft = daysLeft,
                        urgency = urgency
                    )

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Find Recipes") },
                                onClick = { menuExpanded = false; onFindRecipes() },
                                leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Donate") },
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
            }

            if (item.notes.isNotBlank()) {
                Text(
                    text = "Note: ${item.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "Ate / Cooked This" (Primary Rescue Action)
                Button(
                    onClick = onConsumed,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cooked / Ate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onFindRecipes,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Recipes", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AddEditFoodDialog(
    initialItem: FoodItem?,
    onDismiss: () -> Unit,
    onSave: (FoodItem) -> Unit
) {
    var name by remember { mutableStateOf(initialItem?.name.orEmpty()) }
    var category by remember { mutableStateOf(initialItem?.category ?: FoodCategory.PRODUCE.name) }
    var quantityText by remember { mutableStateOf(initialItem?.quantity?.toString() ?: "1.0") }
    var unit by remember { mutableStateOf(initialItem?.unit ?: "pcs") }
    var storageLocation by remember { mutableStateOf(initialItem?.storageLocation ?: StorageLocation.FRIDGE.name) }
    var expiryDate by remember {
        mutableLongStateOf(
            initialItem?.expiryDate ?: (System.currentTimeMillis() + TimeUnit.DAYS.toMillis(4))
        )
    }
    var priceText by remember { mutableStateOf(initialItem?.priceEstimate?.toString() ?: "3.00") }
    var notes by remember { mutableStateOf(initialItem?.notes.orEmpty()) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val quickItems = listOf("Milk", "Eggs", "Bread", "Spinach", "Bananas", "Tomatoes", "Rice", "Cheese", "Apples", "Chicken", "Pasta")
    val units = listOf("pcs", "g", "kg", "ml", "L", "cups", "slices", "packs")
    val categories = FoodCategory.values().map { it.name }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem == null) "Add Food Item" else "Edit Food Item",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (validationError != null) {
                    Text(
                        text = validationError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Quick item chips
                if (initialItem == null) {
                    Text("Quick suggestions:", style = MaterialTheme.typography.labelSmall)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickItems) { suggestion ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    name = suggestion
                                    // smart auto-categorize
                                    when (suggestion) {
                                        "Milk", "Eggs", "Cheese" -> {
                                            category = FoodCategory.DAIRY.name
                                            storageLocation = "FRIDGE"
                                            unit = if (suggestion == "Milk") "ml" else "pcs"
                                        }
                                        "Bread" -> {
                                            category = FoodCategory.BAKERY.name
                                            storageLocation = "PANTRY"
                                            unit = "slices"
                                        }
                                        "Rice", "Pasta" -> {
                                            category = FoodCategory.PANTRY.name
                                            storageLocation = "PANTRY"
                                            unit = "g"
                                        }
                                        "Chicken" -> {
                                            category = FoodCategory.MEAT.name
                                            storageLocation = "FRIDGE"
                                            unit = "g"
                                        }
                                        else -> {
                                            category = FoodCategory.PRODUCE.name
                                            unit = "pcs"
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Food Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; validationError = null },
                    label = { Text("Item Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("food_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Category chips
                Text("Category:", style = MaterialTheme.typography.labelSmall)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.lowercase().capitalize(), fontSize = 11.sp) }
                        )
                    }
                }

                // Quantity and Unit Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Unit picker
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Unit:", style = MaterialTheme.typography.labelSmall)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(units) { u ->
                                FilterChip(
                                    selected = unit == u,
                                    onClick = { unit = u },
                                    label = { Text(u, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                // Storage Location
                Text("Storage Location:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("FRIDGE", "PANTRY", "FREEZER").forEach { loc ->
                        FilterChip(
                            selected = storageLocation == loc,
                            onClick = { storageLocation = loc },
                            label = { Text(loc.lowercase().capitalize()) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Expiration Date & Presets
                Text("Expiration Date:", style = MaterialTheme.typography.labelSmall)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateFormat.format(Date(expiryDate)),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        val days = ((expiryDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
                        Text(
                            text = if (days < 0) "Expired" else if (days == 0) "Today" else "In $days days",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Quick Expiry Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val now = System.currentTimeMillis()
                    val dayMs = TimeUnit.DAYS.toMillis(1)
                    listOf(
                        "+1 Day" to 1L,
                        "+3 Days" to 3L,
                        "+1 Wk" to 7L,
                        "+2 Wks" to 14L,
                        "+1 Mo" to 30L
                    ).forEach { (label, days) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    expiryDate = now + dayMs * days
                                }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Estimated Price
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Estimated Cost ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        validationError = "Please enter an item name"
                        return@Button
                    }
                    val qty = quantityText.toDoubleOrNull() ?: 1.0
                    val price = priceText.toDoubleOrNull() ?: 2.50
                    val item = initialItem?.copy(
                        name = name.trim(),
                        category = category,
                        quantity = qty,
                        unit = unit,
                        storageLocation = storageLocation,
                        expiryDate = expiryDate,
                        priceEstimate = price,
                        notes = notes.trim()
                    ) ?: FoodItem(
                        name = name.trim(),
                        category = category,
                        quantity = qty,
                        unit = unit,
                        storageLocation = storageLocation,
                        expiryDate = expiryDate,
                        priceEstimate = price,
                        notes = notes.trim()
                    )
                    onSave(item)
                },
                modifier = Modifier.testTag("save_food_button")
            ) {
                Text("Save Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
