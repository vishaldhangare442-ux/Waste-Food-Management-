package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Donation
import com.example.ui.components.FoodPhotoThumbnail
import com.example.ui.components.SectionHeader
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.viewmodel.MainViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DeliveryTrackerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allDonations by viewModel.allCommunityDonations.collectAsState()
    val activeDeliveries by viewModel.activeDeliveries.collectAsState()
    val selectedDelivery by viewModel.selectedDeliveryForTracking.collectAsState()

    // Pick currently tracked delivery or default to first active one
    val currentDelivery = selectedDelivery
        ?: activeDeliveries.firstOrNull()
        ?: allDonations.firstOrNull { it.status != "AVAILABLE" }
        ?: allDonations.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("delivery_tracker_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Live Delivery Location Tracker",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time surplus food transport from Donor to Receiver",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFEDE9FE)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7C3AED))
                        )
                        Text(
                            text = "GPS LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED)
                        )
                    }
                }
            }
        }

        // Active Deliveries Horizontal Selector
        if (allDonations.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Select Surplus Food Delivery:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allDonations) { don ->
                            val isSelected = don.id == currentDelivery?.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF7C3AED) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    viewModel.selectDeliveryForTracking(don)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeliveryDining,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = don.foodTitle.take(24) + if (don.foodTitle.length > 24) "..." else "",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Status: ${don.status}",
                                            fontSize = 10.sp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (currentDelivery == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No deliveries active. Post a donation or claim surplus food to begin tracking.")
                    }
                }
            }
        } else {
            // Live Interactive Route Map Canvas
            item {
                InteractiveDeliveryMapCanvas(
                    donation = currentDelivery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                )
            }

            // Quick Telemetry & Status Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TelemetryItem(
                            icon = Icons.Default.Timer,
                            title = "EST. ARRIVAL",
                            value = if (currentDelivery.status == "DELIVERED") "Delivered" else "${currentDelivery.etaMinutes} mins",
                            color = Color(0xFF7C3AED)
                        )
                        Box(modifier = Modifier.width(1.dp).height(36.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        TelemetryItem(
                            icon = Icons.Default.Speed,
                            title = "AVG SPEED",
                            value = if (currentDelivery.status == "DELIVERED") "0 km/h" else "28 km/h",
                            color = Color(0xFF0284C7)
                        )
                        Box(modifier = Modifier.width(1.dp).height(36.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        TelemetryItem(
                            icon = Icons.Default.Place,
                            title = "DISTANCE",
                            value = if (currentDelivery.status == "DELIVERED") "Arrived" else "1.8 km",
                            color = Color(0xFF16A34A)
                        )
                    }
                }
            }

            // Timeline Steps
            item {
                DeliveryTimelineCard(donation = currentDelivery)
            }

            // Interactive Controls: Simulate Transit & Contact Parties
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Delivery Simulation & Driver Actions",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Button to Advance status & simulate courier driving
                        if (currentDelivery.status != "DELIVERED") {
                            Button(
                                onClick = { viewModel.advanceDelivery(currentDelivery) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_advance_delivery_status"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.FastForward, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (currentDelivery.status) {
                                        "CLAIMED" -> "Simulate: Driver Picked Up Food at Donor"
                                        "OUT_FOR_PICKUP" -> "Simulate: Driver In Transit on Route"
                                        "IN_TRANSIT" -> "Simulate: Complete Delivery to Receiver"
                                        else -> "Advance Delivery Step"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFDCFCE7),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D))
                                    Text(
                                        text = "Food Successfully Delivered! Rescued ~${currentDelivery.weightKg} kg (${currentDelivery.servingsEstimate} meals)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                        }

                        // Contact Buttons: Donor Number & Receiver Number
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { launchPhoneCall(context, currentDelivery.donorNumber) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call Donor", fontSize = 12.sp)
                            }

                            if (currentDelivery.receiverPhone.isNotBlank()) {
                                Button(
                                    onClick = { launchPhoneCall(context, currentDelivery.receiverPhone) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call Receiver", fontSize = 12.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = { launchMaps(context, currentDelivery.pickupAddress) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = "Open Maps", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Food Manifest Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Food Cargo Manifest",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FoodPhotoThumbnail(
                                photoUri = currentDelivery.foodPhotoUri,
                                presetName = currentDelivery.foodPresetName,
                                modifier = Modifier.size(70.dp)
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = currentDelivery.foodTitle,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Quantity: ${currentDelivery.quantity} (~${currentDelivery.weightKg} kg)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Servings: ${currentDelivery.servingsEstimate} people fed",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForestGreenPrimary
                                )
                            }
                        }

                        // Donor info with explicit donor number
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Pickup: ${currentDelivery.donorName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Phone: ${currentDelivery.donorNumber}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2563EB)
                                    )
                                }
                                Text(
                                    text = currentDelivery.pickupAddress,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Receiver info with explicit NGO / Individual badge
                        if (currentDelivery.receiverName.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (currentDelivery.receiverType == "NGO") Color(0xFFEFF6FF) else Color(0xFFFFFBEB)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Drop-off: ${currentDelivery.receiverName}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentDelivery.receiverType == "NGO") Color(0xFF1E40AF) else Color(0xFF92400E)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (currentDelivery.receiverType == "NGO") Color(0xFF2563EB) else Color(0xFFD97706)
                                        ) {
                                            Text(
                                                text = currentDelivery.receiverType,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = currentDelivery.dropAddress.ifBlank { "Shelter Distribution Center" },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
fun InteractiveDeliveryMapCanvas(
    donation: Donation,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_anim")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_float"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 1. Draw subtle street grid lines
                val gridColor = Color(0xFF1E293B)
                for (x in 0..(w.toInt()) step 60) {
                    drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), h), strokeWidth = 1f)
                }
                for (y in 0..(h.toInt()) step 60) {
                    drawLine(gridColor, Offset(0f, y.toFloat()), Offset(w, y.toFloat()), strokeWidth = 1f)
                }

                // 2. Define route keypoints from Donor (top-left) to Receiver (bottom-right)
                val start = Offset(w * 0.18f, h * 0.35f)
                val p1 = Offset(w * 0.40f, h * 0.25f)
                val p2 = Offset(w * 0.60f, h * 0.70f)
                val end = Offset(w * 0.85f, h * 0.65f)

                val routePath = Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(p1.x, p1.y, p2.x, p2.y, end.x, end.y)
                }

                // Draw background road path
                drawPath(
                    path = routePath,
                    color = Color(0xFF334155),
                    style = Stroke(width = 10f, cap = StrokeCap.Round)
                )

                // Draw dashed active route
                drawPath(
                    path = routePath,
                    color = Color(0xFF818CF8),
                    style = Stroke(
                        width = 4f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                    )
                )

                // 3. Compute Courier / Vehicle Position along the route
                val progress = when (donation.status) {
                    "AVAILABLE" -> 0.05f
                    "CLAIMED" -> 0.18f
                    "OUT_FOR_PICKUP" -> 0.38f
                    "IN_TRANSIT" -> 0.68f
                    "DELIVERED" -> 1.0f
                    else -> 0.5f
                }

                // Interpolate along the cubic bezier
                val t = progress
                val u = 1f - t
                val curX = u * u * u * start.x + 3 * u * u * t * p1.x + 3 * u * t * t * p2.x + t * t * t * end.x
                val curY = u * u * u * start.y + 3 * u * u * t * p1.y + 3 * u * t * t * p2.y + t * t * t * end.y
                val courierPos = Offset(curX, curY)

                // 4. Draw Animated Radar Pulse at Courier Position
                if (donation.status != "DELIVERED") {
                    drawCircle(
                        color = Color(0xFFA855F7).copy(alpha = 0.4f * (1f - pulseProgress)),
                        radius = 20f + (pulseProgress * 35f),
                        center = courierPos
                    )
                }

                // 5. Draw Donor Pin (Green)
                drawCircle(Color(0xFF16A34A), radius = 14f, center = start)
                drawCircle(Color.White, radius = 5f, center = start)

                // 6. Draw Receiver Pin (Blue for NGO, Amber for Individual)
                val receiverColor = if (donation.receiverType == "NGO") Color(0xFF2563EB) else Color(0xFFD97706)
                drawCircle(receiverColor, radius = 14f, center = end)
                drawCircle(Color.White, radius = 5f, center = end)

                // 7. Draw Courier Vehicle Marker (Purple)
                drawCircle(Color(0xFF7C3AED), radius = 16f, center = courierPos)
                drawCircle(Color.White, radius = 7f, center = courierPos)
            }

            // Overlay badges and labels
            // Donor Pin Label
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF16A34A)),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 14.dp, top = 14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF16A34A)))
                    Text("Donor: ${donation.donorName.take(16)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Receiver Pin Label
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (donation.receiverType == "NGO") Color(0xFF2563EB) else Color(0xFFD97706)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (donation.receiverType == "NGO") Color(0xFF2563EB) else Color(0xFFD97706)))
                    Text(
                        text = "Receiver: ${donation.receiverName.ifBlank { "Community Hub" }.take(18)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Courier Live Info in middle top
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF7C3AED).copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(
                        text = if (donation.status == "DELIVERED") "Delivery Complete" else "Courier En Route: ~${donation.etaMinutes} min",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun TelemetryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun DeliveryTimelineCard(
    donation: Donation,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        "1. Surplus Food Claimed" to (donation.status != "AVAILABLE"),
        "2. Ready for Pickup at Donor" to (donation.status in listOf("OUT_FOR_PICKUP", "IN_TRANSIT", "DELIVERED")),
        "3. In Transit (Live GPS Tracking)" to (donation.status in listOf("IN_TRANSIT", "DELIVERED")),
        "4. Arrived at Destination" to (donation.status == "DELIVERED"),
        "5. Handed Over & Rescued" to (donation.status == "DELIVERED")
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Live Delivery Milestones",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            steps.forEachIndexed { index, (stepTitle, isDone) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isDone) Color(0xFF16A34A) else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        } else {
                            Text("${index + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }

                    Text(
                        text = stepTitle,
                        fontSize = 13.sp,
                        fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                        color = if (isDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

fun launchMaps(context: Context, address: String) {
    if (address.isNotBlank()) {
        try {
            val gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address))
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            context.startActivity(mapIntent)
        } catch (_: Exception) {}
    }
}
