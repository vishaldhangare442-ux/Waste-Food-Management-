package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodNeedRequest
import com.example.data.model.ReceiverType
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.viewmodel.MainViewModel
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

// Center point: Donor location (e.g. San Francisco downtown)
const val DONOR_LAT = 37.7749
const val DONOR_LNG = -122.4194

@Composable
fun NearbyDonationMapComponent(
    viewModel: MainViewModel,
    onFulfillRequest: (FoodNeedRequest) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allRequests by viewModel.foodNeedRequests.collectAsState()
    val selectedRequest by viewModel.selectedMapRequest.collectAsState()

    // Filter states
    var maxDistanceMiles by remember { mutableFloatStateOf(10f) }
    var selectedReceiverType by remember { mutableStateOf("ALL") } // "ALL", "NGO", "INDIVIDUAL"
    var selectedUrgency by remember { mutableStateOf("ALL") } // "ALL", "Immediate (<2h)", "Today", "This Week"
    var mapZoomLevel by remember { mutableFloatStateOf(1.0f) } // 0.8f (wide) to 2.0f (close)
    var isSatelliteMode by remember { mutableStateOf(false) }

    // Filtered requests based on distance, type, urgency
    val filteredRequests = remember(allRequests, maxDistanceMiles, selectedReceiverType, selectedUrgency) {
        allRequests.filter { req ->
            val matchesDistance = req.distanceMiles <= maxDistanceMiles
            val matchesType = when (selectedReceiverType) {
                "NGO" -> req.receiverType == ReceiverType.NGO
                "INDIVIDUAL" -> req.receiverType == ReceiverType.INDIVIDUAL
                else -> true
            }
            val matchesUrgency = when (selectedUrgency) {
                "ALL" -> true
                else -> req.urgency.equals(selectedUrgency, ignoreCase = true)
            }
            matchesDistance && matchesType && matchesUrgency
        }.sortedBy { it.distanceMiles }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("nearby_donation_map_component"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Title & Google Maps Badge
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
                        color = Color(0xFF4285F4) // Google Maps Blue
                    ) {
                        Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Nearby Donation Requests Map",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE8F0FE)
                            ) {
                                Text(
                                    text = "Google Maps",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1967D2),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Visualizing ${filteredRequests.size} requests within ${maxDistanceMiles.toInt()} mi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                }

                // Satellite / Default Mode Toggle
                IconButton(
                    onClick = { isSatelliteMode = !isSatelliteMode },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Toggle Map Style",
                        tint = if (isSatelliteMode) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Quick Distance Filter Chips Row
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter by Distance Radius:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                    Text(
                        text = "Max ${maxDistanceMiles.toInt()} miles",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenPrimary
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val distancePresets = listOf(1f to "< 1 mi", 3f to "< 3 mi", 5f to "< 5 mi", 10f to "< 10 mi", 25f to "All (<25 mi)")
                    items(distancePresets) { (dist, label) ->
                        val isSelected = maxDistanceMiles == dist
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) ForestGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { maxDistanceMiles = dist }
                                .testTag("distance_chip_${dist.toInt()}")
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Interactive Distance Slider
                Slider(
                    value = maxDistanceMiles,
                    onValueChange = { maxDistanceMiles = it },
                    valueRange = 1f..25f,
                    steps = 23,
                    colors = SliderDefaults.colors(
                        thumbColor = ForestGreenPrimary,
                        activeTrackColor = ForestGreenPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .testTag("distance_slider")
                )
            }

            // Recipient Type & Urgency Filters Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Type filters
                val typeOptions = listOf("ALL" to "All", "NGO" to "🏢 NGOs", "INDIVIDUAL" to "👨‍👩‍👧 Families")
                typeOptions.forEach { (typeKey, typeLabel) ->
                    val isSelected = selectedReceiverType == typeKey
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected)
                            (if (typeKey == "NGO") Color(0xFF1D4ED8) else if (typeKey == "INDIVIDUAL") Color(0xFFD97706) else Color(0xFF374151))
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedReceiverType = typeKey }
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // INTERACTIVE GOOGLE MAP CANVAS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .testTag("interactive_google_map_canvas")
            ) {
                // Infinite transition for pulsing radar and donor beacon
                val infiniteTransition = rememberInfiniteTransition(label = "map_beacon_pulse")
                val pulseRadius by infiniteTransition.animateFloat(
                    initialValue = 12f,
                    targetValue = 28f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1400),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "pulse"
                )
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 0.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1400),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "pulseAlpha"
                )

                // Track clicked coordinates for marker selection
                var mapCenterOffset by remember { mutableStateOf(Offset.Zero) }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(filteredRequests, mapZoomLevel) {
                            detectTapGestures { tapOffset ->
                                // Determine if user clicked near any pin
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                val donorX = w * 0.48f + mapCenterOffset.x
                                val donorY = h * 0.52f + mapCenterOffset.y

                                var closestRequest: FoodNeedRequest? = null
                                var minTapDist = 48f // 48px hit target

                                filteredRequests.forEach { req ->
                                    val coords = getRelativeCoords(req, donorX, donorY, w, h, maxDistanceMiles, mapZoomLevel)
                                    val dx = tapOffset.x - coords.x
                                    val dy = tapOffset.y - coords.y
                                    val tapDist = sqrt(dx * dx + dy * dy)
                                    if (tapDist < minTapDist) {
                                        minTapDist = tapDist
                                        closestRequest = req
                                    }
                                }

                                if (closestRequest != null) {
                                    viewModel.selectMapRequest(closestRequest)
                                } else {
                                    // Clicked empty space
                                    viewModel.selectMapRequest(null)
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val donorX = w * 0.48f + mapCenterOffset.x
                    val donorY = h * 0.52f + mapCenterOffset.y

                    // 1. Draw Google Maps Base Map Styling
                    drawGoogleMapBackground(w, h, isSatelliteMode)

                    // 2. Draw Distance Radius Rings (1 mi, 3 mi, 5 mi, 10 mi)
                    drawDistanceRadiusRings(donorX, donorY, w, maxDistanceMiles, mapZoomLevel, isSatelliteMode)

                    // 3. Draw Route to Selected Request
                    selectedRequest?.let { selected ->
                        val targetCoords = getRelativeCoords(selected, donorX, donorY, w, h, maxDistanceMiles, mapZoomLevel)
                        drawRouteLine(donorX, donorY, targetCoords.x, targetCoords.y, selected.distanceMiles)
                    }

                    // 4. Draw Request Markers (NGOs & Individuals)
                    filteredRequests.forEach { req ->
                        val isSelected = req.id == selectedRequest?.id
                        val coords = getRelativeCoords(req, donorX, donorY, w, h, maxDistanceMiles, mapZoomLevel)
                        drawGoogleMapsPin(
                            coords = coords,
                            request = req,
                            isSelected = isSelected
                        )
                    }

                    // 5. Draw Donor's Current Location Beacon (Google Maps pulsing blue dot)
                    drawDonorLocationBeacon(donorX, donorY, pulseRadius, pulseAlpha)
                }

                // Map Overlay Controls (Zoom In, Zoom Out, Center, Open Google Maps)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { mapZoomLevel = min(2.0f, mapZoomLevel + 0.25f) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color(0xFF1E293B), modifier = Modifier.size(18.dp))
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { mapZoomLevel = max(0.6f, mapZoomLevel - 0.25f) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color(0xFF1E293B), modifier = Modifier.size(18.dp))
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .size(34.dp)
                            .clickable {
                                mapCenterOffset = Offset.Zero
                                mapZoomLevel = 1.0f
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Center on Donor", tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Bottom Left Map Legend
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2563EB)))
                            Text("You", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF1D4ED8)))
                            Text("NGO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFD97706)))
                            Text("Family", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                    }
                }
            }

            // SELECTED REQUEST DETAIL CARD (Slide in when pin or card clicked)
            AnimatedVisibility(
                visible = selectedRequest != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedRequest?.let { req ->
                    SelectedRequestDetailCard(
                        request = req,
                        onClose = { viewModel.selectMapRequest(null) },
                        onNavigateGoogleMaps = { launchGoogleMapsNavigation(context, req.latitude, req.longitude, req.dropAddress) },
                        onCall = { launchPhoneCall(context, req.phone) },
                        onFulfill = { onFulfillRequest(req) }
                    )
                }
            }

            // HORIZONTAL CAROUSEL OF FILTERED REQUESTS
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Nearby Requests (${filteredRequests.size} within ${maxDistanceMiles.toInt()} miles):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                if (filteredRequests.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No requests found within ${maxDistanceMiles.toInt()} miles. Try increasing the distance slider above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredRequests) { req ->
                            val isSelected = req.id == selectedRequest?.id
                            NearbyRequestMiniCard(
                                request = req,
                                isSelected = isSelected,
                                onClick = { viewModel.selectMapRequest(req) },
                                onOpenGoogleMaps = { launchGoogleMapsNavigation(context, req.latitude, req.longitude, req.dropAddress) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedRequestDetailCard(
    request: FoodNeedRequest,
    onClose: () -> Unit,
    onNavigateGoogleMaps: () -> Unit,
    onCall: () -> Unit,
    onFulfill: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (request.receiverType == ReceiverType.NGO) Color(0xFFEFF6FF) else Color(0xFFFFFBEB)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (request.receiverType == ReceiverType.NGO) Color(0xFF3B82F6) else Color(0xFFF59E0B)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with Requester Type and Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (request.receiverType == ReceiverType.NGO) Color(0xFF1D4ED8) else Color(0xFFD97706)
                    ) {
                        Text(
                            text = if (request.receiverType == ReceiverType.NGO) "🏢 NGO SHELTER" else "👨‍👩‍👧 FAMILY IN NEED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (request.urgency.contains("Immediate")) Color(0xFFFEE2E2) else Color(0xFFE0E7FF)
                    ) {
                        Text(
                            text = "⚡ ${request.urgency}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (request.urgency.contains("Immediate")) Color(0xFFDC2626) else Color(0xFF3730A3),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = "📍 ${request.distanceMiles} mi away",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Dismiss", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }

            Text(
                text = request.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Text(
                text = "Requester: ${request.requesterName} • Serving ~${request.peopleCount} people",
                fontSize = 12.sp,
                color = Color(0xFF334155)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(14.dp))
                Text(request.dropAddress, fontSize = 11.sp, color = Color(0xFF475569))
            }

            if (request.notes.isNotBlank()) {
                Text(
                    text = "Notes: ${request.notes}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Open in Google Maps
                Button(
                    onClick = onNavigateGoogleMaps,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Google Maps", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Call
                OutlinedButton(
                    onClick = onCall,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(0.8f)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 12.sp)
                }

                // Fulfill
                Button(
                    onClick = onFulfill,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Donate Food", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun NearbyRequestMiniCard(
    request: FoodNeedRequest,
    isSelected: Boolean,
    onClick: () -> Unit,
    onOpenGoogleMaps: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                (if (request.receiverType == ReceiverType.NGO) Color(0xFFDBEAFE) else Color(0xFFFEF3C7))
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected)
                (if (request.receiverType == ReceiverType.NGO) Color(0xFF2563EB) else Color(0xFFD97706))
            else MaterialTheme.colorScheme.outlineVariant
        )
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
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (request.receiverType == ReceiverType.NGO) Color(0xFF1D4ED8) else Color(0xFFD97706)
                ) {
                    Text(
                        text = if (request.receiverType == ReceiverType.NGO) "NGO" else "FAMILY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "${request.distanceMiles} mi",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
            }

            Text(
                text = request.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = "${request.requesterName} (~${request.peopleCount} people)",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                maxLines = 1
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ ${request.urgency}",
                    fontSize = 10.sp,
                    color = if (request.urgency.contains("Immediate")) Color(0xFFDC2626) else Color(0xFF7C3AED),
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(onClick = onOpenGoogleMaps, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.OpenInNew, contentDescription = "Open in Maps", tint = Color(0xFF4285F4), modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// Canvas Drawing Helpers for Google Maps Style Rendering
private fun DrawScope.drawGoogleMapBackground(width: Float, height: Float, isSatellite: Boolean) {
    if (isSatellite) {
        // Dark / Satellite Palette
        drawRect(color = Color(0xFF0F172A), size = Size(width, height))
        // Subtle water body
        val waterPath = Path().apply {
            moveTo(width * 0.75f, 0f)
            cubicTo(width * 0.70f, height * 0.4f, width * 0.85f, height * 0.7f, width, height * 0.8f)
            lineTo(width, 0f)
            close()
        }
        drawPath(waterPath, color = Color(0xFF1E3A5F))
    } else {
        // Classic Google Maps Light Palette
        // Land: #E8ECE9
        drawRect(color = Color(0xFFE8ECE9), size = Size(width, height))

        // Parks / Greenspaces (#C8E6C9)
        drawRoundRect(
            color = Color(0xFFD4ECD5),
            topLeft = Offset(width * 0.05f, height * 0.15f),
            size = Size(width * 0.22f, height * 0.25f),
            cornerRadius = CornerRadius(16f, 16f)
        )
        drawRoundRect(
            color = Color(0xFFD4ECD5),
            topLeft = Offset(width * 0.55f, height * 0.65f),
            size = Size(width * 0.25f, height * 0.28f),
            cornerRadius = CornerRadius(16f, 16f)
        )

        // Water body / Bay (#AADAFF - Google Maps classic water blue)
        val bayPath = Path().apply {
            moveTo(width * 0.78f, 0f)
            cubicTo(width * 0.72f, height * 0.35f, width * 0.82f, height * 0.75f, width, height * 0.9f)
            lineTo(width, 0f)
            close()
        }
        drawPath(bayPath, color = Color(0xFFAADAFF))

        // Primary Grid & Road Network
        // Major Arterial Road (White with soft borders)
        drawLine(
            color = Color(0xFFD1D5DB),
            start = Offset(0f, height * 0.38f),
            end = Offset(width, height * 0.38f),
            strokeWidth = 9f
        )
        drawLine(
            color = Color.White,
            start = Offset(0f, height * 0.38f),
            end = Offset(width, height * 0.38f),
            strokeWidth = 6f
        )

        drawLine(
            color = Color(0xFFD1D5DB),
            start = Offset(width * 0.48f, 0f),
            end = Offset(width * 0.48f, height),
            strokeWidth = 9f
        )
        drawLine(
            color = Color.White,
            start = Offset(width * 0.48f, 0f),
            end = Offset(width * 0.48f, height),
            strokeWidth = 6f
        )

        // Diagonal Highway (Orange/Yellow Google Maps highway style)
        val hwyPath = Path().apply {
            moveTo(0f, height * 0.75f)
            cubicTo(width * 0.3f, height * 0.6f, width * 0.6f, height * 0.4f, width * 0.85f, 0f)
        }
        drawPath(hwyPath, color = Color(0xFFFDE68A), style = Stroke(width = 5f, cap = StrokeCap.Round))

        // Secondary city street grid
        for (i in 1..4) {
            val y = height * (i * 0.18f)
            drawLine(color = Color(0xFFF3F4F6), start = Offset(0f, y), end = Offset(width * 0.78f, y), strokeWidth = 3f)
        }
        for (i in 1..4) {
            val x = width * (i * 0.16f)
            drawLine(color = Color(0xFFF3F4F6), start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 3f)
        }
    }
}

private fun DrawScope.drawDistanceRadiusRings(
    centerX: Float,
    centerY: Float,
    width: Float,
    maxMiles: Float,
    zoom: Float,
    isSatellite: Boolean
) {
    val pxPerMile = (width * 0.40f / maxMiles) * zoom
    val ringMiles = listOf(1f, 3f, 5f, 10f)

    ringMiles.forEach { miles ->
        if (miles <= maxMiles) {
            val radius = miles * pxPerMile
            drawCircle(
                color = if (isSatellite) Color(0xFF64748B).copy(alpha = 0.35f) else Color(0xFF94A3B8).copy(alpha = 0.4f),
                center = Offset(centerX, centerY),
                radius = radius,
                style = Stroke(
                    width = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )
        }
    }
}

private fun DrawScope.drawDonorLocationBeacon(centerX: Float, centerY: Float, pulseRadius: Float, pulseAlpha: Float) {
    // Pulsing accuracy halo
    drawCircle(
        color = Color(0xFF4285F4).copy(alpha = pulseAlpha),
        center = Offset(centerX, centerY),
        radius = pulseRadius * 1.5f
    )

    // White outer ring
    drawCircle(
        color = Color.White,
        center = Offset(centerX, centerY),
        radius = 9f
    )

    // Solid Google Maps Blue center dot
    drawCircle(
        color = Color(0xFF1A73E8),
        center = Offset(centerX, centerY),
        radius = 6.5f
    )
}

private fun DrawScope.drawRouteLine(startX: Float, startY: Float, endX: Float, endY: Float, distanceMiles: Double) {
    // Route halo
    drawLine(
        color = Color(0xFF3B82F6).copy(alpha = 0.3f),
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = 8f,
        cap = StrokeCap.Round
    )

    // Main Google Maps dashed navigation route line
    drawLine(
        color = Color(0xFF2563EB),
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = 3.5f,
        cap = StrokeCap.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
    )

    // Midpoint ETA badge indicator
    val midX = (startX + endX) / 2
    val midY = (startY + endY) / 2
    drawCircle(color = Color(0xFF1E40AF), center = Offset(midX, midY), radius = 6f)
    drawCircle(color = Color.White, center = Offset(midX, midY), radius = 3f)
}

private fun DrawScope.drawGoogleMapsPin(coords: Offset, request: FoodNeedRequest, isSelected: Boolean) {
    val pinColor = if (request.receiverType == ReceiverType.NGO) Color(0xFF1D4ED8) else Color(0xFFD97706)
    val pinRadius = if (isSelected) 15f else 11f

    // Shadow underneath pin
    drawOval(
        color = Color.Black.copy(alpha = 0.25f),
        topLeft = Offset(coords.x - pinRadius * 0.7f, coords.y + pinRadius * 0.5f),
        size = Size(pinRadius * 1.4f, pinRadius * 0.6f)
    )

    // Outer glow if selected
    if (isSelected) {
        drawCircle(
            color = pinColor.copy(alpha = 0.35f),
            center = coords,
            radius = pinRadius + 8f
        )
    }

    // Google Maps teardrop marker shape
    val pinPath = Path().apply {
        moveTo(coords.x, coords.y + pinRadius * 0.9f)
        lineTo(coords.x - pinRadius * 0.8f, coords.y - pinRadius * 0.3f)
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(
                coords.x - pinRadius,
                coords.y - pinRadius * 1.6f,
                coords.x + pinRadius,
                coords.y + pinRadius * 0.4f
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        lineTo(coords.x, coords.y + pinRadius * 0.9f)
        close()
    }
    drawPath(pinPath, color = pinColor)

    // White center circle inside pin
    drawCircle(
        color = Color.White,
        center = Offset(coords.x, coords.y - pinRadius * 0.6f),
        radius = pinRadius * 0.45f
    )

    // Urgent badge if immediate
    if (request.urgency.contains("Immediate")) {
        drawCircle(
            color = Color(0xFFDC2626),
            center = Offset(coords.x + pinRadius * 0.7f, coords.y - pinRadius * 1.3f),
            radius = 4f
        )
    }
}

// Convert geographic offset relative to Donor location to Canvas pixel coordinates
private fun getRelativeCoords(
    request: FoodNeedRequest,
    donorX: Float,
    donorY: Float,
    width: Float,
    height: Float,
    maxDistanceMiles: Float,
    zoom: Float
): Offset {
    val dLat = request.latitude - DONOR_LAT
    val dLng = request.longitude - DONOR_LNG

    // 1 deg latitude ≈ 69 miles
    // 1 deg longitude at 38° ≈ 54 miles
    val xOffsetMiles = dLng * 54.6
    val yOffsetMiles = -dLat * 69.0 // Invert because screen Y is down

    val pxPerMile = (width * 0.40f / maxDistanceMiles) * zoom
    val px = donorX + (xOffsetMiles * pxPerMile).toFloat()
    val py = donorY + (yOffsetMiles * pxPerMile).toFloat()

    // Clamp inside canvas boundary with margin
    val clampedX = px.coerceIn(24f, width - 24f)
    val clampedY = py.coerceIn(24f, height - 24f)

    return Offset(clampedX, clampedY)
}

// External Intents for Google Maps Navigation & Phone
fun launchGoogleMapsNavigation(context: Context, lat: Double, lng: Double, address: String) {
    try {
        // First try Google Navigation Intent
        val navUri = Uri.parse("google.navigation:q=$lat,$lng&mode=d")
        val mapIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
            return
        }
    } catch (_: Exception) {}

    // Fallback to universal geo intent or web Google Maps URL
    try {
        val geoUri = Uri.parse("geo:$lat,$lng?q=" + Uri.encode(address))
        val fallbackIntent = Intent(Intent.ACTION_VIEW, geoUri)
        context.startActivity(fallbackIntent)
    } catch (_: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}

private fun launchPhoneCall(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    } catch (_: Exception) {}
}
