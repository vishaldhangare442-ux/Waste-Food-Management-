package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpiryUrgency
import com.example.data.model.FoodItem
import com.example.data.model.RecipeMatch
import com.example.ui.components.ExpiryUrgencyBadge
import com.example.ui.components.MatchScoreBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.theme.ExpiredRed
import com.example.ui.theme.ExpiringWarning
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.FreshGreen
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToInventory: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onOpenAddFoodDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val inStockItems by viewModel.inStockItems.collectAsState()
    val recommendedRecipes by viewModel.recommendedRecipes.collectAsState()
    val impactStats by viewModel.impactStats.collectAsState()

    // Identify urgent items
    val expiringItems = inStockItems.filter {
        val urgency = it.getExpiryUrgency()
        urgency == ExpiryUrgency.CRITICAL_TODAY || urgency == ExpiryUrgency.EXPIRING_SOON || urgency == ExpiryUrgency.EXPIRED
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Welcome & Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    ForestGreenPrimary,
                                    Color(0xFF2E7D32),
                                    Color(0xFF1B5E20)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Hello, ${currentUser?.name ?: "Chef"}! 👋",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Let's prevent food waste today",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Eco,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick mini stats row inside hero
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HeroStatPill(
                                label = "Pantry Items",
                                value = "${inStockItems.size}",
                                modifier = Modifier.weight(1f)
                            )
                            HeroStatPill(
                                label = "Money Saved",
                                value = "$${"%.1f".format(impactStats.totalRescuedValue)}",
                                modifier = Modifier.weight(1f)
                            )
                            HeroStatPill(
                                label = "CO₂ Cut",
                                value = "${"%.1f".format(impactStats.totalCo2SavedKg)} kg",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 2. Urgent Expiry Alerts Banner (if items are expiring soon)
        if (expiringItems.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expiry_alert_banner"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(ExpiredRed, ExpiringWarning)))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ExpiredRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = ExpiredRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${expiringItems.size} ${if (expiringItems.size == 1) "item needs" else "items need"} attention!",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ExpiredRed
                                )
                                Text(
                                    text = "Use them today or cook with recommended recipes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF7F1D1D)
                                )
                            }
                        }

                        // List of first 3 urgent items
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            expiringItems.take(3).forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = item.name,
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "(${item.quantity} ${item.unit})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    ExpiryUrgencyBadge(
                                        daysLeft = item.daysUntilExpiry(),
                                        urgency = item.getExpiryUrgency()
                                    )
                                }
                            }
                        }

                        // Call to action button: "Rescue with Recipes"
                        Button(
                            onClick = onNavigateToRecipes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("rescue_recipes_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Find Recipes for Expiring Items", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // 3. Quick Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "Add Food",
                    subtitle = "Log fresh item",
                    icon = Icons.Default.Add,
                    color = ForestGreenPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenAddFoodDialog
                )
                QuickActionCard(
                    title = "View Pantry",
                    subtitle = "${inStockItems.size} items active",
                    icon = Icons.Default.Kitchen,
                    color = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToInventory
                )
                QuickActionCard(
                    title = "Recipes",
                    subtitle = "Smart match",
                    icon = Icons.Default.Restaurant,
                    color = Color(0xFFD97706),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToRecipes
                )
            }
        }

        // 4. Recipe Recommendation Engine Spotlight
        item {
            SectionHeader(
                title = "Smart Recipe Recommendations",
                subtitle = "Suggested based on what's in your kitchen",
                actionLabel = "See All",
                onActionClick = onNavigateToRecipes
            )
        }

        if (recommendedRecipes.isEmpty()) {
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
                        Text(text = "🥗", fontSize = 36.sp)
                        Text(
                            text = "Add ingredients to unlock recipes",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Our engine calculates recipe matches dynamically as you add pantry items.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recommendedRecipes.take(5)) { recipeMatch ->
                        RecipeSpotlightCard(
                            recipeMatch = recipeMatch,
                            onClick = {
                                viewModel.selectRecipeForDetails(recipeMatch)
                                onNavigateToRecipes()
                            }
                        )
                    }
                }
            }
        }

        // 5. Impact Snapshot
        item {
            SectionHeader(
                title = "Sustainability Impact",
                subtitle = "Your zero-waste footprint"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Saved from Waste",
                    value = "${impactStats.rescuedItemCount} meals",
                    subtitle = "${impactStats.rescueRatePercentage}% rescue rate",
                    icon = Icons.Default.Eco,
                    iconTint = FreshGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Estimated Value",
                    value = "$${"%.2f".format(impactStats.totalRescuedValue)}",
                    subtitle = "Kept in your pocket",
                    icon = Icons.Default.VolunteerActivism,
                    iconTint = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun HeroStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RecipeSpotlightCard(
    recipeMatch: RecipeMatch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe = recipeMatch.recipe
    Card(
        modifier = modifier
            .width(220.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = recipe.heroEmoji, fontSize = 28.sp)
                MatchScoreBadge(
                    matchPercentage = recipeMatch.matchPercentage,
                    expiringRescuedCount = recipeMatch.expiringIngredientsCount
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = recipe.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${recipe.prepTimeMinutes + recipe.cookTimeMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Text(
                    text = if (recipeMatch.isReadyToCook) "Ready to cook! →" else "Missing ${recipeMatch.missingIngredients.size} items",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (recipeMatch.isReadyToCook) FreshGreen else Color(0xFFD97706)
                )
            }
        }
    }
}
