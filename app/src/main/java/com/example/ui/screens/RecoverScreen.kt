package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Donation
import com.example.data.model.ReceiverProfile
import com.example.data.model.ReceiverType
import com.example.ui.components.FoodPhotoThumbnail
import com.example.ui.components.SectionHeader
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

@Composable
fun RecoverScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allDonations by viewModel.allCommunityDonations.collectAsState()
    val receiverProfile by viewModel.receiverProfile.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var donationToClaim by remember { mutableStateOf<Donation?>(null) }

    // Filter available surplus food
    val availableDonations = remember(allDonations, searchQuery, selectedCategoryFilter) {
        allDonations.filter { don ->
            val matchesSearch = searchQuery.isBlank() ||
                    don.foodTitle.contains(searchQuery, ignoreCase = true) ||
                    don.donorName.contains(searchQuery, ignoreCase = true) ||
                    don.pickupAddress.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == "ALL" ||
                    don.category.equals(selectedCategoryFilter, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }

    val myRecoveries = remember(allDonations, receiverProfile) {
        allDonations.filter { it.receiverName.isNotBlank() }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("recover_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header: Food Recover Component
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (receiverProfile.type == ReceiverType.NGO) Color(0xFF1E3A8A) else Color(0xFF78350F)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (receiverProfile.type == ReceiverType.NGO) Icons.Default.Diversity3 else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "RECOVER COMPONENT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Receiver Switcher: NGO vs Individual (Explicit user requirement!)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.clickable { viewModel.toggleReceiverType() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (receiverProfile.type == ReceiverType.NGO) "Switch to Individual" else "Switch to NGO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Text(
                        text = "Rescue Surplus Food for Communities",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Current Active Receiver Profile Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (receiverProfile.type == ReceiverType.NGO) Color(0xFF60A5FA) else Color(0xFFFBBF24)
                                    ) {
                                        Text(
                                            text = if (receiverProfile.type == ReceiverType.NGO) "NGO RECEIVER" else "INDIVIDUAL RECEIVER",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0F172A),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = receiverProfile.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "${receiverProfile.identifierOrSize} • ${receiverProfile.phone}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "Delivery address: ${receiverProfile.address}",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            IconButton(
                                onClick = { showEditProfileDialog = true }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Search and Filters
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search surplus food, meals, bakery, donors...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_food_recovery")
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categoryFilters = listOf(
                    "ALL" to "All Food",
                    "COOKED_MEALS" to "🍲 Cooked Meals",
                    "BAKERY" to "🥖 Bakery",
                    "PRODUCE" to "🥗 Fresh Produce",
                    "DAIRY" to "🥛 Dairy",
                    "PACKAGED" to "🥫 Packaged"
                )
                items(categoryFilters) { (catId, catLabel) ->
                    val isSelected = selectedCategoryFilter == catId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = catId },
                        label = { Text(catLabel, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        // Active Deliveries / Recoveries section if any
        if (myRecoveries.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Active Food Deliveries",
                    subtitle = "${myRecoveries.size} surplus food orders claimed & in delivery"
                )
            }

            items(myRecoveries) { donation ->
                ActiveRecoveryTrackerCard(
                    donation = donation,
                    onTrackLocation = {
                        viewModel.selectDeliveryForTracking(donation)
                        viewModel.setTab(AppTab.DELIVERY)
                    },
                    onCallDonor = {
                        launchPhoneCall(context, donation.donorNumber)
                    }
                )
            }
        }

        // Available Surplus Food Feed
        item {
            SectionHeader(
                title = "Available for Recovery",
                subtitle = "${availableDonations.count { it.status == "AVAILABLE" }} listings ready for NGO & Individual claim"
            )
        }

        val availableOnly = availableDonations.filter { it.status == "AVAILABLE" }
        if (availableOnly.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No surplus food found for this filter. Check back soon or clear search.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(availableOnly) { donation ->
                SurplusFoodRecoveryCard(
                    donation = donation,
                    receiverType = receiverProfile.type,
                    onClaimFood = { donationToClaim = donation },
                    onCallDonor = { launchPhoneCall(context, donation.donorNumber) },
                    onMessageDonor = { launchSms(context, donation.donorNumber) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }

    // Claim Food Dialog
    donationToClaim?.let { donation ->
        ClaimFoodDialog(
            donation = donation,
            receiverProfile = receiverProfile,
            onDismiss = { donationToClaim = null },
            onConfirmClaim = { deliveryMethod, notes ->
                viewModel.claimDonation(donation, deliveryMethod, notes)
                donationToClaim = null
                viewModel.setTab(AppTab.DELIVERY)
            }
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        EditReceiverProfileDialog(
            profile = receiverProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updated ->
                viewModel.updateReceiverProfile(updated)
                showEditProfileDialog = false
            }
        )
    }
}

@Composable
fun SurplusFoodRecoveryCard(
    donation: Donation,
    receiverType: ReceiverType,
    onClaimFood: () -> Unit,
    onCallDonor: () -> Unit,
    onMessageDonor: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Food Photo
                FoodPhotoThumbnail(
                    photoUri = donation.foodPhotoUri,
                    presetName = donation.foodPresetName,
                    modifier = Modifier.size(80.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "AVAILABLE FOR RESCUE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(12.dp))
                                Text(
                                    text = "Best before ${donation.expiryHours}h",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }

                    Text(
                        text = donation.foodTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )

                    Text(
                        text = "Quantity: ${donation.quantity} • Feeds ~${donation.servingsEstimate} people",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
            }

            // Donor Info & Donor Number (User explicitly requested "dinner numbar")
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            Icon(Icons.Default.Business, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Donor: ${donation.donorName}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Pickup Location",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Donor Number: ${donation.donorNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = onCallDonor,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call Donor", tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = onMessageDonor,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Message, contentDescription = "SMS Donor", tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                        Text(
                            text = donation.pickupAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (donation.specialInstructions.isNotBlank()) {
                Text(
                    text = "Note: ${donation.specialInstructions}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }

            // Claim Food Button
            Button(
                onClick = onClaimFood,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_claim_food_${donation.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (receiverType == ReceiverType.NGO) Color(0xFF2563EB) else Color(0xFFD97706)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Claim Food as ${if (receiverType == ReceiverType.NGO) "NGO" else "Individual"}",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ActiveRecoveryTrackerCard(
    donation: Donation,
    onTrackLocation: () -> Unit,
    onCallDonor: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (donation.status) {
                        "CLAIMED" -> Color(0xFFDBEAFE)
                        "OUT_FOR_PICKUP" -> Color(0xFFFEF3C7)
                        "IN_TRANSIT" -> Color(0xFFF3E8FF)
                        else -> Color(0xFFDCFCE7)
                    }
                ) {
                    Text(
                        text = when (donation.status) {
                            "CLAIMED" -> "🔵 Claimed • Courier Assigned"
                            "OUT_FOR_PICKUP" -> "🟡 Driver Heading to Donor"
                            "IN_TRANSIT" -> "🟣 In Transit to Delivery Location"
                            else -> "✅ Food Rescued & Delivered"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (donation.status) {
                            "CLAIMED" -> Color(0xFF1D4ED8)
                            "OUT_FOR_PICKUP" -> Color(0xFFB45309)
                            "IN_TRANSIT" -> Color(0xFF7E22CE)
                            else -> Color(0xFF15803D)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "ETA ~${donation.etaMinutes} min",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF7C3AED)
                )
            }

            Text(
                text = donation.foodTitle,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Donor: ${donation.donorName} (${donation.donorNumber}) • Claimed by: ${donation.receiverName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTrackLocation,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.DeliveryDining, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Track Live Delivery Location", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCallDonor,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call Donor", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun ClaimFoodDialog(
    donation: Donation,
    receiverProfile: ReceiverProfile,
    onDismiss: () -> Unit,
    onConfirmClaim: (deliveryMethod: String, notes: String) -> Unit
) {
    var deliveryMethod by remember { mutableStateOf("VOLUNTEER_DELIVERY") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Confirm Food Recovery", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Claiming: ${donation.foodTitle}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Quantity: ${donation.quantity} (~${donation.servingsEstimate} meals)",
                    style = MaterialTheme.typography.bodySmall
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (receiverProfile.type == ReceiverType.NGO) Color(0xFFDBEAFE) else Color(0xFFFEF3C7)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (receiverProfile.type == ReceiverType.NGO) Icons.Default.Diversity3 else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (receiverProfile.type == ReceiverType.NGO) Color(0xFF1D4ED8) else Color(0xFFB45309),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Receiver: ${receiverProfile.name} (${receiverProfile.type.label})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = receiverProfile.identifierOrSize,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Text("Delivery Method:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { deliveryMethod = "VOLUNTEER_DELIVERY" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = deliveryMethod == "VOLUNTEER_DELIVERY",
                        onClick = { deliveryMethod = "VOLUNTEER_DELIVERY" }
                    )
                    Text("Volunteer Courier (Live GPS Route Tracking)", fontSize = 13.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { deliveryMethod = "SELF_PICKUP" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = deliveryMethod == "SELF_PICKUP",
                        onClick = { deliveryMethod = "SELF_PICKUP" }
                    )
                    Text("Self Pickup at Donor Address", fontSize = 13.sp)
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Note for Donor / Delivery Courier") },
                    placeholder = { Text("e.g. Please ring front bell") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmClaim(deliveryMethod, notes.trim()) }
            ) {
                Text("Confirm & Start Delivery")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditReceiverProfileDialog(
    profile: ReceiverProfile,
    onDismiss: () -> Unit,
    onSave: (ReceiverProfile) -> Unit
) {
    var type by remember { mutableStateOf(profile.type) }
    var name by remember { mutableStateOf(profile.name) }
    var phone by remember { mutableStateOf(profile.phone) }
    var address by remember { mutableStateOf(profile.address) }
    var identifier by remember { mutableStateOf(profile.identifierOrSize) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Receiver Profile (NGO / Individual)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Receiver Type:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (type == ReceiverType.NGO) Color(0xFF2563EB) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                type = ReceiverType.NGO
                                if (name.contains("Individual")) name = "Helping Hands Food Bank (NGO)"
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Diversity3, contentDescription = null, tint = if (type == ReceiverType.NGO) Color.White else MaterialTheme.colorScheme.onSurface)
                            Text("NGO", color = if (type == ReceiverType.NGO) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (type == ReceiverType.INDIVIDUAL) Color(0xFFD97706) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                type = ReceiverType.INDIVIDUAL
                                if (name.contains("NGO")) name = "Alex Rivera (Individual)"
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = if (type == ReceiverType.INDIVIDUAL) Color.White else MaterialTheme.colorScheme.onSurface)
                            Text("Individual", color = if (type == ReceiverType.INDIVIDUAL) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (type == ReceiverType.NGO) "NGO / Food Bank Name" else "Individual / Family Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text(if (type == ReceiverType.NGO) "NGO Registration ID" else "Household Members Size") },
                    placeholder = { Text(if (type == ReceiverType.NGO) "e.g. NGO Reg #501C-4421" else "e.g. Family of 4") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Delivery Drop-off Address") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        profile.copy(
                            type = type,
                            name = name.trim().ifBlank { if (type == ReceiverType.NGO) "Community NGO" else "Individual Recipient" },
                            phone = phone.trim(),
                            address = address.trim(),
                            identifierOrSize = identifier.trim()
                        )
                    )
                }
            ) {
                Text("Save Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun launchSms(context: Context, phone: String) {
    if (phone.isNotBlank()) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phone"))
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
