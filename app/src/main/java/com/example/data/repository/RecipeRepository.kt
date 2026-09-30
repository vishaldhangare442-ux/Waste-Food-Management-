package com.example.data.repository

import com.example.data.local.RecipeDao
import com.example.data.model.AvailabilityFilter
import com.example.data.model.DietaryFilter
import com.example.data.model.FoodItem
import com.example.data.model.MealTypeFilter
import com.example.data.model.Recipe
import com.example.data.model.RecipeMatch
import kotlinx.coroutines.flow.Flow

class RecipeRepository(
    private val recipeDao: RecipeDao
) {
    fun getAllRecipes(): Flow<List<Recipe>> = recipeDao.getAllRecipes()

    suspend fun insertRecipe(recipe: Recipe): Long = recipeDao.insertRecipe(recipe)

    suspend fun deleteRecipe(recipe: Recipe) = recipeDao.deleteRecipe(recipe)

    /**
     * Core Recipe Recommendation Engine:
     * Evaluates all recipes against the user's current active pantry inventory.
     * Computes matched ingredients, missing ingredients, match percentage,
     * and counts how many ingredients expiring soon are rescued by each recipe.
     */
    fun recommendRecipes(
        recipes: List<Recipe>,
        inventoryItems: List<FoodItem>,
        dietaryFilter: DietaryFilter = DietaryFilter.ALL,
        mealTypeFilter: MealTypeFilter = MealTypeFilter.ALL,
        availabilityFilter: AvailabilityFilter = AvailabilityFilter.ALL,
        searchQuery: String = ""
    ): List<RecipeMatch> {
        val matches = recipes.map { recipe ->
            computeRecipeMatch(recipe, inventoryItems)
        }

        // Apply filters
        return matches.filter { match ->
            val recipe = match.recipe

            // Search query filter
            val matchesSearch = searchQuery.isBlank() ||
                    recipe.title.contains(searchQuery, ignoreCase = true) ||
                    recipe.description.contains(searchQuery, ignoreCase = true) ||
                    recipe.ingredientsRaw.contains(searchQuery, ignoreCase = true)

            // Dietary filter
            val matchesDiet = when (dietaryFilter) {
                DietaryFilter.ALL -> true
                else -> recipe.dietaryTags.contains(dietaryFilter.label, ignoreCase = true)
            }

            // Meal type filter
            val matchesMeal = when (mealTypeFilter) {
                MealTypeFilter.ALL -> true
                MealTypeFilter.QUICK_EASY -> (recipe.prepTimeMinutes + recipe.cookTimeMinutes) <= 25 ||
                        recipe.dietaryTags.contains("Quick", ignoreCase = true)
                else -> recipe.mealType.equals(mealTypeFilter.label, ignoreCase = true)
            }

            // Availability filter
            val matchesAvailability = when (availabilityFilter) {
                AvailabilityFilter.ALL -> true
                AvailabilityFilter.READY_NOW -> match.isReadyToCook
                AvailabilityFilter.MISSING_ONE -> match.missingIngredients.size <= 2
                AvailabilityFilter.RESCUE_PRIORITY -> match.expiringIngredientsCount > 0
            }

            matchesSearch && matchesDiet && matchesMeal && matchesAvailability
        }.sortedWith(
            compareByDescending<RecipeMatch> { it.expiringIngredientsCount > 0 }
                .thenByDescending { it.matchPercentage }
                .thenByDescending { it.expiringIngredientsCount }
                .thenBy { it.missingIngredients.size }
        )
    }

    private fun computeRecipeMatch(
        recipe: Recipe,
        inventory: List<FoodItem>
    ): RecipeMatch {
        val ingredients = recipe.getIngredientList()
        val matched = mutableListOf<String>()
        val missing = mutableListOf<String>()
        var expiringCount = 0

        for (ingredient in ingredients) {
            val matchedItem = inventory.firstOrNull { inv ->
                isIngredientMatch(inv.name, ingredient)
            }

            if (matchedItem != null) {
                matched.add(ingredient)
                if (matchedItem.daysUntilExpiry() <= 3) {
                    expiringCount++
                }
            } else {
                missing.add(ingredient)
            }
        }

        val percentage = if (ingredients.isNotEmpty()) {
            (matched.size * 100) / ingredients.size
        } else {
            0
        }

        return RecipeMatch(
            recipe = recipe,
            matchPercentage = percentage,
            matchedIngredients = matched,
            missingIngredients = missing,
            expiringIngredientsCount = expiringCount
        )
    }

    private fun isIngredientMatch(inventoryName: String, recipeIngredient: String): Boolean {
        val inv = inventoryName.trim().lowercase()
        val rec = recipeIngredient.trim().lowercase()

        if (inv == rec) return true
        if (inv.contains(rec) || rec.contains(inv)) return true

        // Clean common plurals (e.g. eggs -> egg, bananas -> banana, tomatoes -> tomato)
        val cleanInv = inv.removeSuffix("es").removeSuffix("s")
        val cleanRec = rec.removeSuffix("es").removeSuffix("s")
        return cleanInv.contains(cleanRec) || cleanRec.contains(cleanInv)
    }
}
