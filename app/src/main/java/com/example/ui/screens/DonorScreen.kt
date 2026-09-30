package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Donation
import com.example.ui.components.FOOD_PRESETS
import com.example.ui.components.FoodPhotoThumbnail
import com.example.ui.components.FoodPresetSelector
import com.example.ui.components.NearbyDonationMapComponent
import com.example.ui.components.SectionHeader
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

enum class DonorViewMode {
    MAP,
    LISTINGS
}

@Composable
fun DonorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allDonations by viewModel.allCommunityDonations.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val foodNeedRequests by viewModel.foodNeedRequests.collectAsState()
    var showPostDialog by remember { mutableStateOf(false) }
    var donorViewMode by remember { mutableStateOf(DonorViewMode.MAP) }
    var prefilledTitle by remember { mutableStateOf("") }

    // Filter to donations posted by this user (or demo donations for interactive review)
    val userDonations = remember(allDonations, currentUser) {
        val uid = currentUser?.id ?: 1L
        allDonations.filter { it.userId == uid }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("donor_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header: Donor Component
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ForestGreenPrimary
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
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
                                Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text("DONOR COMPONENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEF08A)
                        ) {
                            Text(
                                text = "Zero Food Waste",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF854D0E),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Turn Surplus Food into Hope",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "Restaurants, catering services, grocery shops and individuals: Post extra food with photos, contact number, and track delivery to NGOs and local families.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showPostDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = ForestGreenPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_post_surplus_food")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Post Surplus Food", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.setTab(AppTab.RECOVER) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.6f))),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Receiver Mode")
                        }
                    }
                }
            }
        }

        // Quick Stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DonorStatChip(
                    label = "Total Posted",
                    value = "${userDonations.size} Listings",
                    color = Color(0xFFE0E7FF),
                    textColor = Color(0xFF3730A3),
                    modifier = Modifier.weight(1f)
                )
                DonorStatChip(
                    label = "Rescued / Active",
                    value = "${userDonations.count { it.status != "AVAILABLE" }} Active",
                    color = Color(0xFFDCFCE7),
                    textColor = Color(0xFF15803D),
                    modifier = Modifier.weight(1f)
                )
                DonorStatChip(
                    label = "Meals Saved",
                    value = "${userDonations.sumOf { it.servingsEstimate }} Servings",
                    color = Color(0xFFFEF3C7),
                    textColor = Color(0xFFB45309),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // View Switcher Tabs: Nearby Requests Map vs My Surplus Posts
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (donorViewMode == DonorViewMode.MAP) ForestGreenPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { donorViewMode = DonorViewMode.MAP }
                            .testTag("tab_donor_map")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (donorViewMode == DonorViewMode.MAP) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Nearby Needs Map (${foodNeedRequests.size})",
                                fontSize = 12.sp,
                                fontWeight = if (donorViewMode == DonorViewMode.MAP) FontWeight.Bold else FontWeight.Medium,
                                color = if (donorViewMode == DonorViewMode.MAP) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (donorViewMode == DonorViewMode.LISTINGS) ForestGreenPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { donorViewMode = DonorViewMode.LISTINGS }
                            .testTag("tab_donor_listings")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.VolunteerActivism,
                                contentDescription = null,
                                tint = if (donorViewMode == DonorViewMode.LISTINGS) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "My Listings (${userDonations.size})",
                                fontSize = 12.sp,
                                fontWeight = if (donorViewMode == DonorViewMode.LISTINGS) FontWeight.Bold else FontWeight.Medium,
                                color = if (donorViewMode == DonorViewMode.LISTINGS) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        if (donorViewMode == DonorViewMode.MAP) {
            // GOOGLE MAP COMPONENT FOR NEARBY DONATION REQUESTS
            item {
                NearbyDonationMapComponent(
                    viewModel = viewModel,
                    onFulfillRequest = { req ->
                        prefilledTitle = "Donation for ${req.requesterName}: ${req.title}"
                        showPostDialog = true
                    }
                )
            }
        } else {
            // Section: Donor's Posted Surplus Food
            item {
                SectionHeader(
                    title = "Your Surplus Food Posts",
                    subtitle = "Manage active donations and live delivery statuses"
                )
            }

            if (userDonations.isEmpty()) {
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
                            Icon(Icons.Default.Fastfood, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(40.dp))
                            Text("No food posted by your account yet", fontWeight = FontWeight.Bold)
                            Text(
                                text = "Tap 'Post Surplus Food' to list extra meals, fresh produce, or bakery items.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Button(
                                onClick = { showPostDialog = true },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Post First Donation")
                            }
                        }
                    }
                }
            } else {
                items(userDonations) { donation ->
                    DonorFoodCard(
                        donation = donation,
                        onTrackDelivery = {
                            viewModel.selectDeliveryForTracking(donation)
                            viewModel.setTab(AppTab.DELIVERY)
                        },
                        onCallReceiver = { phone ->
                            launchPhoneCall(context, phone)
                        }
                    )
                }
            }

            // Community Feed preview
            item {
                SectionHeader(
                    title = "All Community Food Rescue Listings",
                    subtitle = "Active surplus food listed across the city"
                )
            }

            val otherDonations = allDonations.filter { it.userId != (currentUser?.id ?: 1L) }
            items(otherDonations) { donation ->
                DonorFoodCard(
                    donation = donation,
                    onTrackDelivery = {
                        viewModel.selectDeliveryForTracking(donation)
                        viewModel.setTab(AppTab.DELIVERY)
                    },
                    onCallReceiver = { phone ->
                        launchPhoneCall(context, phone)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }

    if (showPostDialog) {
        PostSurplusFoodDialog(
            initialTitle = prefilledTitle,
            onDismiss = {
                showPostDialog = false
                prefilledTitle = ""
            },
            onPost = { newDonation ->
                viewModel.addDonation(newDonation)
                showPostDialog = false
                prefilledTitle = ""
            }
        )
    }
}

@Composable
fun DonorStatChip(
    label: String,
    value: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, fontSize = 11.sp, color = textColor.copy(alpha = 0.8f))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun DonorFoodCard(
    donation: Donation,
    onTrackDelivery: () -> Unit,
    onCallReceiver: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                // Food photo thumbnail
                FoodPhotoThumbnail(
                    photoUri = donation.foodPhotoUri,
                    presetName = donation.foodPresetName,
                    modifier = Modifier.size(76.dp)
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
                            color = when (donation.status) {
                                "AVAILABLE" -> Color(0xFFDCFCE7)
                                "CLAIMED" -> Color(0xFFDBEAFE)
                                "OUT_FOR_PICKUP" -> Color(0xFFFEF3C7)
                                "IN_TRANSIT" -> Color(0xFFF3E8FF)
                                else -> Color(0xFFE2E8F0)
                            }
                        ) {
                            Text(
                                text = when (donation.status) {
                                    "AVAILABLE" -> "🟢 Available"
                                    "CLAIMED" -> "🔵 Claimed"
                                    "OUT_FOR_PICKUP" -> "🟡 Ready for Pickup"
                                    "IN_TRANSIT" -> "🟣 In Transit"
                                    "DELIVERED" -> "✅ Delivered"
                                    else -> donation.status
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (donation.status) {
                                    "AVAILABLE" -> Color(0xFF15803D)
                                    "CLAIMED" -> Color(0xFF1D4ED8)
                                    "OUT_FOR_PICKUP" -> Color(0xFFB45309)
                                    "IN_TRANSIT" -> Color(0xFF7E22CE)
                                    else -> Color(0xFF334155)
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = "${donation.servingsEstimate} servings",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreenPrimary
                        )
                    }

                    Text(
                        text = donation.foodTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )

                    Text(
                        text = "Quantity: ${donation.quantity} (~${donation.weightKg} kg)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Donor Contact & Pickup location details
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Donor: ${donation.donorName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Donor Number: ${donation.donorNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8)
                            )
                        }

                        TextButton(
                            onClick = { launchPhoneCall(context, donation.donorNumber) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", fontSize = 12.sp)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            // Receiver details if claimed
            if (donation.receiverName.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (donation.receiverType == "NGO") Color(0xFFEFF6FF) else Color(0xFFFFFBEB)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (donation.receiverType == "NGO") Icons.Default.Diversity3 else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (donation.receiverType == "NGO") Color(0xFF2563EB) else Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Receiver: ${donation.receiverName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (donation.receiverType == "NGO") Color(0xFF1E40AF) else Color(0xFF92400E)
                                )
                            }
                            if (donation.receiverDetails.isNotBlank()) {
                                Text(
                                    text = donation.receiverDetails,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }

                        if (donation.receiverPhone.isNotBlank()) {
                            IconButton(
                                onClick = { onCallReceiver(donation.receiverPhone) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call Receiver", tint = Color(0xFF2563EB))
                            }
                        }
                    }
                }
            }

            // Action Buttons: Track delivery if not available
            if (donation.status != "AVAILABLE") {
                Button(
                    onClick = onTrackDelivery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_track_delivery_${donation.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C3AED)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DeliveryDining, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (donation.status == "DELIVERED") "View Delivery Summary" else "Track Delivery Location Live",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostSurplusFoodDialog(
    initialTitle: String = "",
    onDismiss: () -> Unit,
    onPost: (Donation) -> Unit
) {
    var foodTitle by remember(initialTitle) { mutableStateOf(initialTitle) }
    var category by remember { mutableStateOf("COOKED_MEALS") }
    var quantity by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("20") }
    var weightKg by remember { mutableStateOf("8.0") }
    var donorName by remember { mutableStateOf("Sunrise Restaurant") }
    var donorNumber by remember { mutableStateOf("+1 (555) 234-8890") }
    var pickupAddress by remember { mutableStateOf("450 Broadway St, Downtown") }
    var expiryHours by remember { mutableIntStateOf(6) }
    var specialInstructions by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf("COOKED_MEAL") }
    var customPhotoUri by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customPhotoUri = uri.toString()
        }
    }

    val categories = listOf(
        "COOKED_MEALS" to "Hot Cooked Meals",
        "BAKERY" to "Bakery & Breads",
        "PRODUCE" to "Fresh Produce & Greens",
        "DAIRY" to "Dairy, Milk & Eggs",
        "PACKAGED" to "Pantry & Packaged Goods",
        "BUFFET" to "Buffet & Catering Trays"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = ForestGreenPrimary)
                Text("Post Surplus Food (Donor)", fontWeight = FontWeight.Bold)
            }
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

                // Food Photo Section (User requirement: "food photo")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Food Photo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FoodPhotoThumbnail(
                                photoUri = customPhotoUri,
                                presetName = selectedPreset,
                                modifier = Modifier.size(64.dp)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Upload Photo", fontSize = 12.sp)
                                }
                                if (customPhotoUri.isNotBlank()) {
                                    TextButton(onClick = { customPhotoUri = "" }) {
                                        Text("Clear custom photo", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        // Presets
                        FoodPresetSelector(
                            selectedPresetId = selectedPreset,
                            onSelectPreset = {
                                selectedPreset = it
                                // Map preset to category
                                when (it) {
                                    "BUFFET" -> category = "BUFFET"
                                    "BAKERY" -> category = "BAKERY"
                                    "PRODUCE" -> category = "PRODUCE"
                                    "DAIRY" -> category = "DAIRY"
                                    "COOKED_MEAL" -> category = "COOKED_MEALS"
                                    "PANTRY" -> category = "PACKAGED"
                                }
                            }
                        )
                    }
                }

                // Food Title
                OutlinedTextField(
                    value = foodTitle,
                    onValueChange = { foodTitle = it; validationError = null },
                    label = { Text("Food Title / Description *") },
                    placeholder = { Text("e.g. 40 Packed Rice & Curry Meals") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_food_title")
                )

                // Donor Number (Explicit user requirement: "dinner numbar" -> donor number)
                OutlinedTextField(
                    value = donorNumber,
                    onValueChange = { donorNumber = it; validationError = null },
                    label = { Text("Donor Contact Number (Phone) *") },
                    placeholder = { Text("e.g. +1 (555) 234-8890") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF2563EB)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_donor_number")
                )

                // Donor Name
                OutlinedTextField(
                    value = donorName,
                    onValueChange = { donorName = it },
                    label = { Text("Donor Name / Business Name *") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Quantity & Servings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        placeholder = { Text("e.g. 20 boxes") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = servings,
                        onValueChange = { servings = it },
                        label = { Text("Servings") },
                        placeholder = { Text("20") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Pickup Location Address
                OutlinedTextField(
                    value = pickupAddress,
                    onValueChange = { pickupAddress = it; validationError = null },
                    label = { Text("Pickup Location / Address *") },
                    placeholder = { Text("e.g. 742 Evergreen Terrace, Downtown") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Expiry countdown hours
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                        Text("Best before: $expiryHours hours", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(4, 8, 24, 48).forEach { hrs ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (expiryHours == hrs) ForestGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { expiryHours = hrs }
                            ) {
                                Text(
                                    text = "${hrs}h",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (expiryHours == hrs) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Special pickup instructions
                OutlinedTextField(
                    value = specialInstructions,
                    onValueChange = { specialInstructions = it },
                    label = { Text("Pickup Instructions (optional)") },
                    placeholder = { Text("e.g. Come to rear kitchen entrance, ask for Marco") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (foodTitle.isBlank()) {
                        validationError = "Please describe the food being donated"
                        return@Button
                    }
                    if (donorNumber.isBlank()) {
                        validationError = "Please enter donor contact phone number"
                        return@Button
                    }
                    if (pickupAddress.isBlank()) {
                        validationError = "Please provide pickup address"
                        return@Button
                    }

                    val sCount = servings.toIntOrNull() ?: 15
                    val wCount = weightKg.toDoubleOrNull() ?: (sCount * 0.4)

                    onPost(
                        Donation(
                            foodTitle = foodTitle.trim(),
                            category = category,
                            quantity = quantity.trim().ifBlank { "$sCount servings" },
                            servingsEstimate = sCount,
                            weightKg = wCount,
                            foodPhotoUri = customPhotoUri,
                            foodPresetName = selectedPreset,
                            donorName = donorName.trim().ifBlank { "Community Food Donor" },
                            donorNumber = donorNumber.trim(),
                            pickupAddress = pickupAddress.trim(),
                            expiryHours = expiryHours,
                            specialInstructions = specialInstructions.trim(),
                            status = "AVAILABLE"
                        )
                    )
                },
                modifier = Modifier.testTag("btn_confirm_post_donation")
            ) {
                Text("Post Donation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun launchPhoneCall(context: Context, phone: String) {
    if (phone.isNotBlank()) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
