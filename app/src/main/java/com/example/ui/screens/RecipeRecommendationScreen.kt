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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AvailabilityFilter
import com.example.data.model.DietaryFilter
import com.example.data.model.MealTypeFilter
import com.example.data.model.Recipe
import com.example.data.model.RecipeMatch
import com.example.ui.components.MatchScoreBadge
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.FreshGreen
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeRecommendationScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val recommendedRecipes by viewModel.recommendedRecipes.collectAsState()
    val searchQuery by viewModel.recipeSearchQuery.collectAsState()
    val selectedDietaryFilter by viewModel.selectedDietaryFilter.collectAsState()
    val selectedMealTypeFilter by viewModel.selectedMealTypeFilter.collectAsState()
    val selectedAvailabilityFilter by viewModel.selectedAvailabilityFilter.collectAsState()
    val selectedRecipeMatch by viewModel.selectedRecipeMatch.collectAsState()
    val inStockItems by viewModel.inStockItems.collectAsState()

    var showAddRecipeDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Engine Info & Search
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
                            text = "Recipe Recommendations",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Matched against your ${inStockItems.size} pantry ingredients",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${recommendedRecipes.size} recipes",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setRecipeSearchQuery(it) },
                    placeholder = { Text("Search by dish name or ingredient...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setRecipeSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recipe_search_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                // 1. Availability Filter (Ready to Cook / Expiring Items First)
                Text(
                    text = "Pantry Availability:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AvailabilityFilter.values()) { filter ->
                        FilterChip(
                            selected = selectedAvailabilityFilter == filter,
                            onClick = { viewModel.setAvailabilityFilter(filter) },
                            label = { Text(filter.label) },
                            leadingIcon = if (filter == AvailabilityFilter.READY_NOW) {
                                { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FreshGreen, modifier = Modifier.size(16.dp)) }
                            } else if (filter == AvailabilityFilter.RESCUE_PRIORITY) {
                                { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = if (filter == AvailabilityFilter.READY_NOW) {
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFDCFCE7),
                                    selectedLabelColor = Color(0xFF15803D)
                                )
                            } else FilterChipDefaults.filterChipColors()
                        )
                    }
                }

                // 2. Dietary Restrictions Filter
                Text(
                    text = "Dietary Restrictions:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DietaryFilter.values()) { filter ->
                        FilterChip(
                            selected = selectedDietaryFilter == filter,
                            onClick = { viewModel.setDietaryFilter(filter) },
                            label = { Text(filter.label) }
                        )
                    }
                }

                // 3. Meal Type Filter
                Text(
                    text = "Meal Type:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MealTypeFilter.values()) { filter ->
                        FilterChip(
                            selected = selectedMealTypeFilter == filter,
                            onClick = { viewModel.setMealTypeFilter(filter) },
                            label = { Text(filter.label) }
                        )
                    }
                }
            }

            // Recipe List
            if (recommendedRecipes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🔍", fontSize = 48.sp)
                        Text(
                            text = "No matching recipes found",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Try loosening your dietary filters or adding more items to your inventory.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = {
                                viewModel.setDietaryFilter(DietaryFilter.ALL)
                                viewModel.setMealTypeFilter(MealTypeFilter.ALL)
                                viewModel.setAvailabilityFilter(AvailabilityFilter.ALL)
                                viewModel.setRecipeSearchQuery("")
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Reset All Filters")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        com.example.ui.components.GeminiRecipeSection(viewModel = viewModel)
                    }

                    items(recommendedRecipes, key = { it.recipe.id }) { recipeMatch ->
                        RecipeMatchCard(
                            recipeMatch = recipeMatch,
                            onClick = { viewModel.selectRecipeForDetails(recipeMatch) },
                            onCookNow = { viewModel.cookRecipe(recipeMatch.recipe) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Add Custom Recipe FAB
        FloatingActionButton(
            onClick = { showAddRecipeDialog = true },
            containerColor = ForestGreenPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_custom_recipe_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Custom Recipe", modifier = Modifier.size(28.dp))
        }

        // Recipe Details Modal BottomSheet
        if (selectedRecipeMatch != null) {
            RecipeDetailsBottomSheet(
                recipeMatch = selectedRecipeMatch!!,
                onDismiss = { viewModel.selectRecipeForDetails(null) },
                onCookAndDeduct = {
                    viewModel.cookRecipe(selectedRecipeMatch!!.recipe)
                }
            )
        }

        // Add Custom Recipe Dialog
        if (showAddRecipeDialog) {
            AddCustomRecipeDialog(
                onDismiss = { showAddRecipeDialog = false },
                onSave = { newRecipe ->
                    viewModel.addCustomRecipe(newRecipe)
                    showAddRecipeDialog = false
                }
            )
        }
    }
}

@Composable
fun RecipeMatchCard(
    recipeMatch: RecipeMatch,
    onClick: () -> Unit,
    onCookNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe = recipeMatch.recipe

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("recipe_card_${recipe.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Emoji + Title + Match Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = recipe.heroEmoji, fontSize = 24.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = recipe.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${recipe.mealType} • ${recipe.difficulty} • ${recipe.servings} servings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                MatchScoreBadge(
                    matchPercentage = recipeMatch.matchPercentage,
                    expiringRescuedCount = recipeMatch.expiringIngredientsCount
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // In-stock vs Missing ingredients preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Have: ${recipeMatch.matchedIngredients.joinToString(", ").ifEmpty { "None yet" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = FreshGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (recipeMatch.missingIngredients.isNotEmpty()) {
                        Text(
                            text = "Need: ${recipeMatch.missingIngredients.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

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
                        text = "${recipe.prepTimeMinutes + recipe.cookTimeMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("View Steps", fontSize = 12.sp)
                }

                Button(
                    onClick = onCookNow,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (recipeMatch.isReadyToCook) FreshGreen else ForestGreenPrimary
                    )
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (recipeMatch.isReadyToCook) "Cook & Deduct" else "Cook Recipe",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailsBottomSheet(
    recipeMatch: RecipeMatch,
    onDismiss: () -> Unit,
    onCookAndDeduct: () -> Unit
) {
    val recipe = recipeMatch.recipe
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title & Hero
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = recipe.heroEmoji, fontSize = 36.sp)
                    Column {
                        Text(
                            text = recipe.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${recipe.mealType} • ${recipe.dietaryTags}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                MatchScoreBadge(
                    matchPercentage = recipeMatch.matchPercentage,
                    expiringRescuedCount = recipeMatch.expiringIngredientsCount
                )
            }

            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            // Info Pills Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailPill("Prep", "${recipe.prepTimeMinutes} min", Modifier.weight(1f))
                DetailPill("Cook", "${recipe.cookTimeMinutes} min", Modifier.weight(1f))
                DetailPill("Servings", "${recipe.servings} ppl", Modifier.weight(1f))
                DetailPill("Level", recipe.difficulty, Modifier.weight(1f))
            }

            Divider()

            // Ingredients Breakdown
            Text(
                text = "Ingredients & Inventory Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recipe.getIngredientList().forEach { ing ->
                    val isAvailable = recipeMatch.matchedIngredients.any { it.equals(ing, ignoreCase = true) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isAvailable) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ing,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isAvailable) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isAvailable) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = FreshGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "In Pantry",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            } else {
                                Text(
                                    text = "Missing",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            Divider()

            // Step-by-Step Instructions
            Text(
                text = "Preparation Instructions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                recipe.getInstructionSteps().forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Cook & Deduct Button
            Button(
                onClick = onCookAndDeduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("cook_and_deduct_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "I Cooked This! (Deduct from Pantry)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun DetailPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AddCustomRecipeDialog(
    onDismiss: () -> Unit,
    onSave: (Recipe) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("Dinner") }
    var dietaryTags by remember { mutableStateOf("Vegetarian") }
    var ingredientsRaw by remember { mutableStateOf("") }
    var instructionsRaw by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("10") }
    var cookTime by remember { mutableStateOf("20") }
    var emoji by remember { mutableStateOf("🍲") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val emojis = listOf("🍲", "🥗", "🥪", "🍳", "🍝", "🍚", "🍞", "🍌", "🥤", "🍕")
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Dessert", "Snack")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Recipe", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (validationError != null) {
                    Text(
                        text = validationError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Emoji picker
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(emojis) { em ->
                        Surface(
                            shape = CircleShape,
                            color = if (emoji == em) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { emoji = em }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = em, fontSize = 20.sp)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; validationError = null },
                    label = { Text("Recipe Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Meal type
                Text("Meal Type:", style = MaterialTheme.typography.labelSmall)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(mealTypes) { mt ->
                        FilterChip(
                            selected = mealType == mt,
                            onClick = { mealType = mt },
                            label = { Text(mt) }
                        )
                    }
                }

                OutlinedTextField(
                    value = dietaryTags,
                    onValueChange = { dietaryTags = it },
                    label = { Text("Dietary Tags (e.g. Vegetarian, Gluten-Free)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = ingredientsRaw,
                    onValueChange = { ingredientsRaw = it; validationError = null },
                    label = { Text("Ingredients (semicolon separated) *") },
                    placeholder = { Text("e.g. Eggs; Spinach; Cheese; Milk") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = instructionsRaw,
                    onValueChange = { instructionsRaw = it; validationError = null },
                    label = { Text("Instructions (pipe separated steps) *") },
                    placeholder = { Text("e.g. Whisk eggs and milk|Sauté spinach|Bake until set") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = prepTime,
                        onValueChange = { prepTime = it },
                        label = { Text("Prep (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = cookTime,
                        onValueChange = { cookTime = it },
                        label = { Text("Cook (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        validationError = "Please enter a recipe title"
                        return@Button
                    }
                    if (ingredientsRaw.isBlank()) {
                        validationError = "Please add at least one ingredient"
                        return@Button
                    }
                    if (instructionsRaw.isBlank()) {
                        validationError = "Please add preparation instructions"
                        return@Button
                    }
                    val recipe = Recipe(
                        title = title.trim(),
                        description = description.trim().ifEmpty { "Delicious zero-waste homemade dish." },
                        mealType = mealType,
                        dietaryTags = dietaryTags.trim(),
                        prepTimeMinutes = prepTime.toIntOrNull() ?: 10,
                        cookTimeMinutes = cookTime.toIntOrNull() ?: 15,
                        servings = 2,
                        difficulty = "Easy",
                        ingredientsRaw = ingredientsRaw.trim(),
                        instructionsRaw = instructionsRaw.trim(),
                        heroEmoji = emoji,
                        isCustom = true
                    )
                    onSave(recipe)
                }
            ) {
                Text("Save Recipe")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
