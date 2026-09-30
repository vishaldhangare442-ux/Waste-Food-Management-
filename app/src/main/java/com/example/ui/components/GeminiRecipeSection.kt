package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodItem
import com.example.data.model.Recipe
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.FreshGreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun GeminiRecipeSection(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val inStockItems by viewModel.inStockItems.collectAsState()
    val geminiRecipes by viewModel.geminiRecipes.collectAsState()
    val isGenerating by viewModel.isGeneratingGeminiRecipes.collectAsState()
    val geminiError by viewModel.geminiError.collectAsState()

    var selectedDiet by remember { mutableStateOf("All Diets") }
    var selectedMeal by remember { mutableStateOf("All Meals") }
    var expandedRecipeId by remember { mutableStateOf<Long?>(null) }

    val expiringItems = remember(inStockItems) {
        inStockItems.filter { it.daysUntilExpiry() <= 3 }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("gemini_recipe_section"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1B4B) // Deep Indigo / AI Violet
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF8B5CF6) // Violet sparkle badge
                    ) {
                        Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Gemini AI Recipe Assistant",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF4C1D95)
                            ) {
                                Text(
                                    text = "gemini-3.5-flash",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC4B5FD),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Smart zero-waste recommendations from your pantry",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                if (geminiRecipes.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearGeminiRecommendations() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.7f))
                    }
                }
            }

            // Pantry Inventory Context Pill
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Kitchen,
                        contentDescription = null,
                        tint = Color(0xFFA78BFA),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (expiringItems.isNotEmpty()) {
                            "Pantry: ${inStockItems.size} items (⚠️ ${expiringItems.size} expiring: ${expiringItems.take(2).joinToString { it.name }})"
                        } else {
                            "Pantry: ${inStockItems.size} ingredients ready for recipe matching"
                        },
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            // Dietary & Meal Filter Selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Preferences:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val dietFilters = listOf("All Diets", "Vegetarian", "Vegan", "High-Protein", "Gluten-Free", "Quick (<25m)")
                    items(dietFilters) { diet ->
                        val isSelected = selectedDiet == diet
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.clickable { selectedDiet = diet }
                        ) {
                            Text(
                                text = diet,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Generate Button
            Button(
                onClick = { viewModel.generateGeminiRecipes(selectedDiet, selectedMeal) },
                enabled = !isGenerating && inStockItems.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5CF6),
                    disabledContainerColor = Color(0xFF4C1D95).copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("btn_generate_gemini_recipes")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini is crafting recipes...", color = Color.White, fontSize = 13.sp)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (geminiRecipes.isEmpty()) "✨ Get AI Recipe Recommendations" else "✨ Regenerate AI Recipes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Error display
            if (geminiError != null) {
                Text(
                    text = geminiError.orEmpty(),
                    fontSize = 11.sp,
                    color = Color(0xFFFCA5A5),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Gemini Generated Recipe Cards
            AnimatedVisibility(
                visible = geminiRecipes.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "✨ Recommended by Gemini for your pantry:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA78BFA)
                    )

                    geminiRecipes.forEach { recipe ->
                        val isExpanded = expandedRecipeId == recipe.id
                        GeminiRecipeCard(
                            recipe = recipe,
                            inStockItems = inStockItems,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedRecipeId = if (isExpanded) null else recipe.id
                            },
                            onCookRecipe = {
                                viewModel.cookRecipe(recipe)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeminiRecipeCard(
    recipe: Recipe,
    inStockItems: List<FoodItem>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCookRecipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recipeIngredients = remember(recipe.ingredientsRaw) { recipe.getIngredientList() }
    val inStockNames = remember(inStockItems) { inStockItems.map { it.name.lowercase() } }

    val matchedIngredients = recipeIngredients.filter { ing ->
        inStockNames.any { stock -> ing.lowercase().contains(stock) || stock.contains(ing.lowercase()) }
    }
    val missingIngredients = recipeIngredients.filter { ing ->
        !inStockNames.any { stock -> ing.lowercase().contains(stock) || stock.contains(ing.lowercase()) }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1065)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(recipe.heroEmoji, fontSize = 24.sp)
                    Column {
                        Text(
                            text = recipe.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⏱️ ${recipe.prepTimeMinutes + recipe.cookTimeMinutes} mins",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            Text(
                                text = "• 🍽️ ${recipe.servings} servings",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                            Text(
                                text = "• ${recipe.difficulty}",
                                fontSize = 11.sp,
                                color = Color(0xFFA78BFA)
                            )
                        }
                    }
                }

                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = Color.White
                    )
                }
            }

            Text(
                text = recipe.description,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 16.sp
            )

            // Ingredient match summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF065F46)
                ) {
                    Text(
                        text = "✓ ${matchedIngredients.size} from your pantry",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA7F3D0),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (missingIngredients.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF7C2D12)
                    ) {
                        Text(
                            text = "+ ${missingIngredients.size} optional/staple",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFED7AA),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Expanded details
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Divider(color = Color.White.copy(alpha = 0.15f))

                    Text("Ingredients:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA78BFA))
                    recipeIngredients.forEach { ing ->
                        val isMatched = inStockNames.any { stock -> ing.lowercase().contains(stock) || stock.contains(ing.lowercase()) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isMatched) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isMatched) Color(0xFF34D399) else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = ing,
                                fontSize = 12.sp,
                                color = if (isMatched) Color.White else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Text("Instructions:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA78BFA))
                    recipe.getInstructionSteps().forEachIndexed { index, step ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "${index + 1}.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA78BFA)
                            )
                            Text(
                                text = step,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onCookRecipe,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("👨‍🍳 Cook & Deduct from Pantry Inventory", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
