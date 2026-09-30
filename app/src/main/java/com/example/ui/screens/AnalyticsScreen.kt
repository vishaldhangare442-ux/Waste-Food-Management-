package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Co2
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WasteLog
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.theme.ExpiredRed
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.FreshGreen
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val impactStats by viewModel.impactStats.collectAsState()
    val wasteLogs by viewModel.wasteLogs.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Waste & Impact Analytics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Measure food saved, money retained, and carbon emissions prevented",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Food Rescue Ratio Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        Text(
                            text = "Food Rescue Rate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FreshGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${impactStats.rescueRatePercentage}% Zero-Waste",
                                fontWeight = FontWeight.Bold,
                                color = FreshGreen,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (impactStats.rescueRatePercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = FreshGreen,
                        trackColor = ExpiredRed.copy(alpha = 0.2f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "✅ Rescued: ${impactStats.rescuedItemCount} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = FreshGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "❌ Wasted: ${impactStats.wastedItemCount} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = ExpiredRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Financial Saved",
                    value = "$${"%.2f".format(impactStats.totalRescuedValue)}",
                    subtitle = "vs \$${"%.2f".format(impactStats.totalWastedValue)} wasted",
                    icon = Icons.Default.AttachMoney,
                    iconTint = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "CO₂ Prevented",
                    value = "${"%.1f".format(impactStats.totalCo2SavedKg)} kg",
                    subtitle = "Equivalent to 12 tree-days",
                    icon = Icons.Default.Eco,
                    iconTint = ForestGreenPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Waste Log Timeline
        item {
            SectionHeader(
                title = "Food Logs & History",
                subtitle = "Recent meals consumed, recipes cooked, or logged items"
            )
        }

        if (wasteLogs.isEmpty()) {
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
                            text = "No logs yet. Start marking food as consumed in your pantry!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(wasteLogs) { log ->
                WasteLogItemCard(log = log)
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun WasteLogItemCard(log: WasteLog, modifier: Modifier = Modifier) {
    val isRescued = log.actionType == "RESCUED"
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isRescued) FreshGreen.copy(alpha = 0.15f) else ExpiredRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRescued) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isRescued) FreshGreen else ExpiredRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.foodName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${log.reason.ifEmpty { if (isRescued) "Rescued & Cooked" else "Wasted" }} • ${dateFormat.format(Date(log.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isRescued) "+$${"%.2f".format(log.costAmount)}" else "-$${"%.2f".format(log.costAmount)}",
                    fontWeight = FontWeight.Bold,
                    color = if (isRescued) FreshGreen else ExpiredRed,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (isRescued && log.co2SavedKg > 0) {
                    Text(
                        text = "-${"%.2f".format(log.co2SavedKg)} kg CO₂",
                        fontSize = 11.sp,
                        color = ForestGreenPrimary
                    )
                }
            }
        }
    }
}
