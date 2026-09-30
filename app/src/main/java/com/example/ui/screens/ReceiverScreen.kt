package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Donation
import com.example.data.model.FoodNeedRequest
import com.example.data.model.ReceiverProfile
import com.example.data.model.ReceiverType
import com.example.ui.components.FoodPhotoThumbnail
import com.example.ui.components.NearbyDonationMapComponent
import com.example.ui.components.SectionHeader
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ReceiverScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allDonations by viewModel.allCommunityDonations.collectAsState()
    val receiverProfile by viewModel.receiverProfile.collectAsState()
    val foodNeedRequests by viewModel.foodNeedRequests.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Available Feed, 1: My Deliveries, 2: Broadcast Needs
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showBroadcastNeedDialog by remember { mutableStateOf(false) }
    var donationToClaim by remember { mutableStateOf<Donation?>(null) }
    var needsTypeFilter by remember { mutableStateOf<ReceiverType?>(null) } // null: All, ReceiverType.NGO, ReceiverType.INDIVIDUAL

    val filteredFoodNeedRequests = remember(foodNeedRequests, needsTypeFilter) {
        if (needsTypeFilter == null) {
            foodNeedRequests
        } else {
            foodNeedRequests.filter { it.receiverType == needsTypeFilter }
        }
    }

    // Filter available surplus donations
    val availableDonations = remember(allDonations, searchQuery, selectedCategoryFilter) {
        allDonations.filter { don ->
            val isAvail = don.status == "AVAILABLE"
            val matchesSearch = searchQuery.isBlank() ||
                    don.foodTitle.contains(searchQuery, ignoreCase = true) ||
                    don.donorName.contains(searchQuery, ignoreCase = true) ||
                    don.pickupAddress.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == "ALL" ||
                    don.category.equals(selectedCategoryFilter, ignoreCase = true)
            isAvail && matchesSearch && matchesCategory
        }
    }

    // Active deliveries claimed by receivers
    val claimedDeliveries = remember(allDonations) {
        allDonations.filter { it.status != "AVAILABLE" }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("receiver_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Prominent NGO and Individual Mode Selection Buttons
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("receiver_role_selector_card"),
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Receiver Mode:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (receiverProfile.type == ReceiverType.NGO) Color(0xFFDBEAFE) else Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = if (receiverProfile.type == ReceiverType.NGO) "Active: 🏢 NGO / Shelter" else "Active: 👨‍👩‍👧 Individual / Family",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (receiverProfile.type == ReceiverType.NGO) Color(0xFF1D4ED8) else Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Side-by-side NGO and Individual Mode Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // NGO Button
                        val isNgo = receiverProfile.type == ReceiverType.NGO
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isNgo) Color(0xFF1D4ED8) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                width = if (isNgo) 2.dp else 1.dp,
                                color = if (isNgo) Color(0xFF1E40AF) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setReceiverType(ReceiverType.NGO) }
                                .testTag("btn_receiver_ngo")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Diversity3,
                                        contentDescription = null,
                                        tint = if (isNgo) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "🏢 NGO",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isNgo) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Shelters & Pantries",
                                    fontSize = 11.sp,
                                    color = if (isNgo) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Individual Button
                        val isIndividual = receiverProfile.type == ReceiverType.INDIVIDUAL
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isIndividual) Color(0xFFD97706) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                width = if (isIndividual) 2.dp else 1.dp,
                                color = if (isIndividual) Color(0xFFB45309) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.setReceiverType(ReceiverType.INDIVIDUAL) }
                                .testTag("btn_receiver_individual")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isIndividual) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "👨‍👩‍👧 Individual",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isIndividual) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Families & Persons",
                                    fontSize = 11.sp,
                                    color = if (isIndividual) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hero Header: Receiver Component
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (receiverProfile.type == ReceiverType.NGO) Color(0xFF1E3A8A) else Color(0xFF92400E)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Mode Header & Role Switcher Buttons
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
                                    text = "RECEIVER COMPONENT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Direct NGO and Individual Quick Buttons in Hero
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (receiverProfile.type == ReceiverType.NGO) Color.White else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .clickable { viewModel.setReceiverType(ReceiverType.NGO) }
                                    .testTag("hero_btn_ngo")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Diversity3,
                                        contentDescription = null,
                                        tint = if (receiverProfile.type == ReceiverType.NGO) Color(0xFF1E3A8A) else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "NGO",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (receiverProfile.type == ReceiverType.NGO) Color(0xFF1E3A8A) else Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (receiverProfile.type == ReceiverType.INDIVIDUAL) Color.White else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .clickable { viewModel.setReceiverType(ReceiverType.INDIVIDUAL) }
                                    .testTag("hero_btn_individual")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (receiverProfile.type == ReceiverType.INDIVIDUAL) Color(0xFF92400E) else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Individual",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (receiverProfile.type == ReceiverType.INDIVIDUAL) Color(0xFF92400E) else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = if (receiverProfile.type == ReceiverType.NGO)
                            "NGO Food Recovery Hub"
                        else
                            "Individual & Family Food Support",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Active Receiver Profile Box
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
                                            text = if (receiverProfile.type == ReceiverType.NGO) "NGO / FOOD BANK" else "INDIVIDUAL / FAMILY",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0F172A),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                }

                                Text(
                                    text = receiverProfile.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Text(
                                    text = "${receiverProfile.identifierOrSize} • ${receiverProfile.phone}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )

                                Text(
                                    text = "Drop Address: ${receiverProfile.address}",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }

                            IconButton(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier.testTag("btn_edit_receiver_profile")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White)
                            }
                        }
                    }

                    // Receiver Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReceiverQuickStat(
                            title = "Food Available",
                            value = "${availableDonations.size} Offers",
                            modifier = Modifier.weight(1f)
                        )
                        ReceiverQuickStat(
                            title = "In Delivery",
                            value = "${claimedDeliveries.count { it.status == "IN_TRANSIT" }} Active",
                            modifier = Modifier.weight(1f)
                        )
                        ReceiverQuickStat(
                            title = "Total Rescued",
                            value = "${claimedDeliveries.sumOf { it.servingsEstimate }} Meals",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Receiver Sub-Tabs: 0: Available Food Feed, 1: My Deliveries, 2: Food Needs Requests
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf(
                        "Available Food (${availableDonations.size})" to 0,
                        "Inbound Deliveries (${claimedDeliveries.size})" to 1,
                        "Broadcast Needs" to 2
                    )

                    tabs.forEach { (title, idx) ->
                        val isSelected = selectedTab == idx
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected)
                                (if (receiverProfile.type == ReceiverType.NGO) Color(0xFF2563EB) else Color(0xFFD97706))
                            else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = idx }
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Content based on sub-tab
        when (selectedTab) {
            0 -> {
                // Tab 0: Surplus Food Feed for Receivers
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search surplus food, catering, bakery, donors...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("receiver_search_input")
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categoryFilters = listOf(
                            "ALL" to "All Categories",
                            "COOKED_MEALS" to "🍲 Cooked Meals",
                            "BAKERY" to "🥖 Bakery & Bread",
                            "PRODUCE" to "🥗 Fresh Produce",
                            "DAIRY" to "🥛 Dairy & Milk",
                            "PACKAGED" to "🥫 Packaged Foods",
                            "BUFFET" to "🍱 Buffet Trays"
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

                item {
                    SectionHeader(
                        title = "Surplus Food Ready for Recovery",
                        subtitle = "${availableDonations.size} donations available from restaurants, markets & donors"
                    )
                }

                if (availableDonations.isEmpty()) {
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
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Fastfood, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(40.dp))
                                Text("No surplus food found", fontWeight = FontWeight.Bold)
                                Text(
                                    text = "All listed donations have been claimed, or no items match your search. Check back soon!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(availableDonations) { donation ->
                        SurplusFoodRecoveryCard(
                            donation = donation,
                            receiverType = receiverProfile.type,
                            onClaimFood = { donationToClaim = donation },
                            onCallDonor = { launchPhoneCall(context, donation.donorNumber) },
                            onMessageDonor = { launchSms(context, donation.donorNumber) }
                        )
                    }
                }
            }

            1 -> {
                // Tab 1: Inbound Deliveries
                item {
                    SectionHeader(
                        title = "Inbound Surplus Food Deliveries",
                        subtitle = "Track orders claimed by ${receiverProfile.name} in real-time"
                    )
                }

                if (claimedDeliveries.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(modifier = Modifier.padding(28.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No active recoveries yet. Go to 'Available Food' tab to claim surplus meals!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                } else {
                    items(claimedDeliveries) { donation ->
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
            }

            2 -> {
                // Tab 2: Broadcast Needs / Community Requests
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Community Food Needs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Broadcast what food your shelter or family urgently needs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Button(
                            onClick = { showBroadcastNeedDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (receiverProfile.type == ReceiverType.NGO) Color(0xFF2563EB) else Color(0xFFD97706)
                            )
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Post Need")
                        }
                    }
                }

                item {
                    NearbyDonationMapComponent(
                        viewModel = viewModel
                    )
                }

                // NGO and Individual Filter Buttons for Community Needs
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val ngoCount = foodNeedRequests.count { it.receiverType == ReceiverType.NGO }
                        val indCount = foodNeedRequests.count { it.receiverType == ReceiverType.INDIVIDUAL }

                        // All Needs Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (needsTypeFilter == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { needsTypeFilter = null }
                                .testTag("btn_filter_needs_all")
                        ) {
                            Text(
                                text = "All (${foodNeedRequests.size})",
                                fontSize = 11.sp,
                                fontWeight = if (needsTypeFilter == null) FontWeight.Bold else FontWeight.Normal,
                                color = if (needsTypeFilter == null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        // NGO Needs Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (needsTypeFilter == ReceiverType.NGO) Color(0xFF1D4ED8) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1.1f)
                                .clickable { needsTypeFilter = ReceiverType.NGO }
                                .testTag("btn_filter_needs_ngo")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Diversity3,
                                    contentDescription = null,
                                    tint = if (needsTypeFilter == ReceiverType.NGO) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "🏢 NGO ($ngoCount)",
                                    fontSize = 11.sp,
                                    fontWeight = if (needsTypeFilter == ReceiverType.NGO) FontWeight.Bold else FontWeight.Normal,
                                    color = if (needsTypeFilter == ReceiverType.NGO) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Individual Needs Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (needsTypeFilter == ReceiverType.INDIVIDUAL) Color(0xFFD97706) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1.2f)
                                .clickable { needsTypeFilter = ReceiverType.INDIVIDUAL }
                                .testTag("btn_filter_needs_individual")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (needsTypeFilter == ReceiverType.INDIVIDUAL) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "👨‍👩‍👧 Individual ($indCount)",
                                    fontSize = 11.sp,
                                    fontWeight = if (needsTypeFilter == ReceiverType.INDIVIDUAL) FontWeight.Bold else FontWeight.Normal,
                                    color = if (needsTypeFilter == ReceiverType.INDIVIDUAL) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                items(filteredFoodNeedRequests) { request ->
                    FoodNeedRequestCard(
                        request = request,
                        onCallRequester = { phone -> launchPhoneCall(context, phone) }
                    )
                }
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

    // Post Food Need Dialog
    if (showBroadcastNeedDialog) {
        BroadcastFoodNeedDialog(
            receiverProfile = receiverProfile,
            onDismiss = { showBroadcastNeedDialog = false },
            onPost = { request ->
                viewModel.postFoodNeedRequest(request)
                showBroadcastNeedDialog = false
            }
        )
    }
}

@Composable
fun ReceiverQuickStat(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, fontSize = 10.sp, color = Color.White.copy(alpha = 0.75f))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun FoodNeedRequestCard(
    request: FoodNeedRequest,
    onCallRequester: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
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
                    color = if (request.receiverType == ReceiverType.NGO) Color(0xFFDBEAFE) else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = if (request.receiverType == ReceiverType.NGO) "NGO REQUEST" else "FAMILY NEED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (request.receiverType == ReceiverType.NGO) Color(0xFF1D4ED8) else Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (request.urgency.contains("Immediate")) Color(0xFFFEE2E2) else Color(0xFFF3E8FF)
                ) {
                    Text(
                        text = "Urgency: ${request.urgency}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (request.urgency.contains("Immediate")) Color(0xFFB91C1C) else Color(0xFF7E22CE),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = request.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Beneficiaries: ~${request.peopleCount} people • Requested by: ${request.requesterName}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            if (request.notes.isNotBlank()) {
                Text(
                    text = "Details: ${request.notes}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                    Text(request.dropAddress, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                }

                TextButton(onClick = { onCallRequester(request.phone) }) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call Receiver", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun BroadcastFoodNeedDialog(
    receiverProfile: ReceiverProfile,
    onDismiss: () -> Unit,
    onPost: (FoodNeedRequest) -> Unit
) {
    var selectedType by remember { mutableStateOf(receiverProfile.type) }
    var title by remember { mutableStateOf("") }
    var peopleCount by remember { mutableStateOf(if (selectedType == ReceiverType.NGO) "45" else "4") }
    var urgency by remember { mutableStateOf("Immediate (<2h)") }
    var notes by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Campaign,
                    contentDescription = null,
                    tint = if (selectedType == ReceiverType.NGO) Color(0xFF2563EB) else Color(0xFFD97706)
                )
                Text("Broadcast Food Need", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (validationError != null) {
                    Text(validationError.orEmpty(), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                // Requester Type Selection: NGO vs Individual Buttons
                Text("Requester Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isNgo = selectedType == ReceiverType.NGO
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isNgo) Color(0xFF1D4ED8) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(if (isNgo) 1.5.dp else 1.dp, if (isNgo) Color(0xFF1E40AF) else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedType = ReceiverType.NGO
                                if (peopleCount == "4") peopleCount = "45"
                            }
                            .testTag("dialog_btn_ngo")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Diversity3,
                                contentDescription = null,
                                tint = if (isNgo) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🏢 NGO Shelter",
                                fontSize = 12.sp,
                                fontWeight = if (isNgo) FontWeight.Bold else FontWeight.Medium,
                                color = if (isNgo) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    val isInd = selectedType == ReceiverType.INDIVIDUAL
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isInd) Color(0xFFD97706) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(if (isInd) 1.5.dp else 1.dp, if (isInd) Color(0xFFB45309) else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedType = ReceiverType.INDIVIDUAL
                                if (peopleCount == "45") peopleCount = "4"
                            }
                            .testTag("dialog_btn_individual")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isInd) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "👨‍👩‍👧 Family Need",
                                fontSize = 12.sp,
                                fontWeight = if (isInd) FontWeight.Bold else FontWeight.Medium,
                                color = if (isInd) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; validationError = null },
                    label = { Text("Food Items Needed *") },
                    placeholder = { Text(if (selectedType == ReceiverType.NGO) "e.g. 45 Hot Dinner Meals, Soups" else "e.g. Fresh Groceries, Baby Food") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = peopleCount,
                    onValueChange = { peopleCount = it },
                    label = { Text("Number of People to Feed") },
                    placeholder = { Text(if (selectedType == ReceiverType.NGO) "45" else "4") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Urgency Level:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Immediate (<2h)", "Today", "This Week").forEach { u ->
                        val isSelected = urgency == u
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected)
                                (if (selectedType == ReceiverType.NGO) Color(0xFF2563EB) else Color(0xFFD97706))
                            else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { urgency = u }
                        ) {
                            Text(
                                text = u,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Dietary Requirements") },
                    placeholder = { Text("e.g. Halal or vegetarian preferred") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        validationError = "Please specify what food is needed"
                        return@Button
                    }
                    onPost(
                        FoodNeedRequest(
                            receiverType = selectedType,
                            requesterName = if (selectedType == receiverProfile.type) receiverProfile.name else (if (selectedType == ReceiverType.NGO) "Community Soup Kitchen (NGO)" else "Elena & Family (Individual)"),
                            title = title.trim(),
                            peopleCount = peopleCount.toIntOrNull() ?: (if (selectedType == ReceiverType.NGO) 30 else 4),
                            urgency = urgency,
                            phone = receiverProfile.phone,
                            dropAddress = receiverProfile.address,
                            notes = notes.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == ReceiverType.NGO) Color(0xFF2563EB) else Color(0xFFD97706)
                )
            ) {
                Text("Broadcast Need")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
