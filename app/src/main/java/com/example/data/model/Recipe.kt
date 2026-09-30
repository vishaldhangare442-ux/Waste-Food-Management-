package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DietaryFilter(val label: String) {
    ALL("All Diets"),
    VEGETARIAN("Vegetarian"),
    VEGAN("Vegan"),
    GLUTEN_FREE("Gluten-Free"),
    DAIRY_FREE("Dairy-Free"),
    LOW_CARB("Low-Carb"),
    HIGH_PROTEIN("High-Protein")
}

enum class MealTypeFilter(val label: String) {
    ALL("All Meals"),
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    QUICK_EASY("Quick (<25 min)"),
    DESSERT("Dessert")
}

enum class AvailabilityFilter(val label: String) {
    ALL("All Recipes"),
    READY_NOW("100% In Stock"),
    MISSING_ONE("Missing <= 2 items"),
    RESCUE_PRIORITY("Expiring Items First")
}

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val mealType: String,
    val dietaryTags: String, // e.g. "Vegetarian, Dairy-Free"
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val difficulty: String, // "Easy", "Medium", "Hard"
    val ingredientsRaw: String, // semicolon-separated, e.g. "Eggs; Milk; Bread; Butter; Cinnamon"
    val instructionsRaw: String, // pipe-separated steps, e.g. "Whisk eggs and milk|Dip bread slices|Fry until golden brown"
    val isCustom: Boolean = false,
    val heroEmoji: String = "🍲"
) {
    fun getIngredientList(): List<String> {
        return ingredientsRaw.split(";").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun getInstructionSteps(): List<String> {
        return instructionsRaw.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun getDietaryTagList(): List<String> {
        return dietaryTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}

data class RecipeMatch(
    val recipe: Recipe,
    val matchPercentage: Int,
    val matchedIngredients: List<String>,
    val missingIngredients: List<String>,
    val expiringIngredientsCount: Int
) {
    val isReadyToCook: Boolean = missingIngredients.isEmpty()
}
