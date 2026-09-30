package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Donation
import com.example.data.model.FoodItem
import com.example.data.model.Recipe
import com.example.data.model.SecurityUtil
import com.example.data.model.User
import com.example.data.model.WasteLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Database(
    entities = [
        User::class,
        FoodItem::class,
        Recipe::class,
        Donation::class,
        WasteLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun foodItemDao(): FoodItemDao
    abstract fun recipeDao(): RecipeDao
    abstract fun donationDao(): DonationDao
    abstract fun wasteLogDao(): WasteLogDao

    companion object {
        @Volatile
        private var INSTANCE: FoodDatabase? = null

        fun getDatabase(context: Context): FoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FoodDatabase::class.java,
                    "food_waste_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    seedInitialData(database)
                }
            }
        }

        private suspend fun seedInitialData(db: FoodDatabase) {
            // Seed a demo user: demo@wastewise.com / Demo123!
            val demoSalt = SecurityUtil.generateSalt()
            val demoHash = SecurityUtil.hashPassword("Demo123!", demoSalt)
            val demoUser = User(
                id = 1,
                name = "Eco Chef Alex",
                email = "demo@wastewise.com",
                passwordHash = demoHash,
                salt = demoSalt,
                securityQuestion = "What was your first pet's name?",
                securityAnswerHash = SecurityUtil.hashPassword("sprout", demoSalt)
            )
            db.userDao().insertUser(demoUser)

            // Seed initial pantry inventory with mix of fresh, expiring soon items
            val now = System.currentTimeMillis()
            val day = TimeUnit.DAYS.toMillis(1)
            val sampleItems = listOf(
                FoodItem(
                    userId = 1,
                    name = "Eggs",
                    category = "DAIRY",
                    quantity = 6.0,
                    unit = "pcs",
                    storageLocation = "FRIDGE",
                    purchaseDate = now - day * 4,
                    expiryDate = now + day * 1, // Expiring tomorrow!
                    priceEstimate = 3.20,
                    notes = "Free range eggs"
                ),
                FoodItem(
                    userId = 1,
                    name = "Milk",
                    category = "DAIRY",
                    quantity = 500.0,
                    unit = "ml",
                    storageLocation = "FRIDGE",
                    purchaseDate = now - day * 5,
                    expiryDate = now + day * 2, // Expiring in 2 days!
                    priceEstimate = 2.50,
                    notes = "Whole milk"
                ),
                FoodItem(
                    userId = 1,
                    name = "Bananas",
                    category = "PRODUCE",
                    quantity = 4.0,
                    unit = "pcs",
                    storageLocation = "PANTRY",
                    purchaseDate = now - day * 5,
                    expiryDate = now + day * 1, // Ripe bananas!
                    priceEstimate = 1.80,
                    notes = "Getting ripe spotted, great for banana bread"
                ),
                FoodItem(
                    userId = 1,
                    name = "Bread",
                    category = "BAKERY",
                    quantity = 8.0,
                    unit = "slices",
                    storageLocation = "PANTRY",
                    purchaseDate = now - day * 3,
                    expiryDate = now + day * 2,
                    priceEstimate = 2.90,
                    notes = "Sourdough loaf"
                ),
                FoodItem(
                    userId = 1,
                    name = "Spinach",
                    category = "PRODUCE",
                    quantity = 150.0,
                    unit = "g",
                    storageLocation = "FRIDGE",
                    purchaseDate = now - day * 2,
                    expiryDate = now + day * 3,
                    priceEstimate = 2.40,
                    notes = "Organic baby spinach"
                ),
                FoodItem(
                    userId = 1,
                    name = "Tomatoes",
                    category = "PRODUCE",
                    quantity = 4.0,
                    unit = "pcs",
                    storageLocation = "FRIDGE",
                    purchaseDate = now - day * 1,
                    expiryDate = now + day * 5,
                    priceEstimate = 3.00,
                    notes = "Ripe vine tomatoes"
                ),
                FoodItem(
                    userId = 1,
                    name = "Rice",
                    category = "PANTRY",
                    quantity = 1.5,
                    unit = "kg",
                    storageLocation = "PANTRY",
                    purchaseDate = now - day * 10,
                    expiryDate = now + day * 180,
                    priceEstimate = 4.50,
                    notes = "Jasmine rice"
                ),
                FoodItem(
                    userId = 1,
                    name = "Cheese",
                    category = "DAIRY",
                    quantity = 200.0,
                    unit = "g",
                    storageLocation = "FRIDGE",
                    purchaseDate = now - day * 3,
                    expiryDate = now + day * 7,
                    priceEstimate = 4.00,
                    notes = "Cheddar block"
                ),
                FoodItem(
                    userId = 1,
                    name = "Onion",
                    category = "PRODUCE",
                    quantity = 3.0,
                    unit = "pcs",
                    storageLocation = "PANTRY",
                    purchaseDate = now - day * 4,
                    expiryDate = now + day * 14,
                    priceEstimate = 1.50,
                    notes = "Yellow onions"
                ),
                FoodItem(
                    userId = 1,
                    name = "Flour",
                    category = "PANTRY",
                    quantity = 1.0,
                    unit = "kg",
                    storageLocation = "PANTRY",
                    purchaseDate = now - day * 20,
                    expiryDate = now + day * 120,
                    priceEstimate = 2.20,
                    notes = "All purpose flour"
                ),
                FoodItem(
                    userId = 1,
                    name = "Butter",
                    category = "DAIRY",
                    quantity = 250.0,
                    unit = "g",
                    storageLocation = "FRIDGE",
                    purchaseDate = now - day * 6,
                    expiryDate = now + day * 20,
                    priceEstimate = 3.50,
                    notes = "Unsalted butter"
                ),
                FoodItem(
                    userId = 1,
                    name = "Pasta",
                    category = "PANTRY",
                    quantity = 500.0,
                    unit = "g",
                    storageLocation = "PANTRY",
                    purchaseDate = now - day * 8,
                    expiryDate = now + day * 200,
                    priceEstimate = 1.90,
                    notes = "Penne rigate"
                )
            )
            db.foodItemDao().insertItems(sampleItems)

            // Seed rich recipe catalog for recommendation engine
            val sampleRecipes = listOf(
                Recipe(
                    title = "Zero-Waste Veggie Frittata",
                    description = "A fluffy Italian egg frittata that uses up any odds-and-ends vegetables and cheese in your fridge.",
                    mealType = "Breakfast",
                    dietaryTags = "Vegetarian, Gluten-Free, High-Protein",
                    prepTimeMinutes = 10,
                    cookTimeMinutes = 15,
                    servings = 4,
                    difficulty = "Easy",
                    ingredientsRaw = "Eggs; Milk; Cheese; Spinach; Tomatoes; Onion",
                    instructionsRaw = "Preheat oven to 375°F (190°C) or prepare a stovetop skillet.|In a bowl, whisk eggs, milk, salt, and pepper.|Sauté chopped onions, tomatoes, and wilted spinach for 3 minutes.|Pour egg mixture over veggies, sprinkle cheese on top, and cook until golden brown and set.",
                    heroEmoji = "🍳"
                ),
                Recipe(
                    title = "Rescue Banana Bread",
                    description = "The ultimate zero-waste treat to rescue overripe or spotted bananas from the bin.",
                    mealType = "Dessert",
                    dietaryTags = "Vegetarian, Quick (<25 min)",
                    prepTimeMinutes = 12,
                    cookTimeMinutes = 45,
                    servings = 8,
                    difficulty = "Easy",
                    ingredientsRaw = "Bananas; Flour; Butter; Eggs; Milk",
                    instructionsRaw = "Preheat oven to 350°F (175°C) and grease a loaf pan.|Mash the ripe bananas with a fork until smooth.|Melt butter and stir in eggs and splash of milk.|Fold in flour gently until just combined.|Bake for 45-50 minutes until a toothpick inserted in center comes out clean.",
                    heroEmoji = "🍌"
                ),
                Recipe(
                    title = "Pantry Fried Rice",
                    description = "Transforms day-old rice and leftover produce into a savory, fragrant dinner.",
                    mealType = "Dinner",
                    dietaryTags = "Vegetarian, Dairy-Free, Quick (<25 min)",
                    prepTimeMinutes = 8,
                    cookTimeMinutes = 10,
                    servings = 3,
                    difficulty = "Easy",
                    ingredientsRaw = "Rice; Eggs; Onion; Butter; Spinach",
                    instructionsRaw = "Heat a large pan or wok with butter or oil on high heat.|Sauté diced onion and greens until fragrant.|Push veggies to the side and scramble eggs in the open pan space.|Toss in chilled cooked rice and stir constantly for 4 minutes until hot and toasted.",
                    heroEmoji = "🍚"
                ),
                Recipe(
                    title = "Creamy Tomato Basil Pasta",
                    description = "Quick comforting pasta using fresh or ripe pantry tomatoes and melted cheese.",
                    mealType = "Dinner",
                    dietaryTags = "Vegetarian, Quick (<25 min)",
                    prepTimeMinutes = 5,
                    cookTimeMinutes = 15,
                    servings = 2,
                    difficulty = "Easy",
                    ingredientsRaw = "Pasta; Tomatoes; Cheese; Onion; Butter",
                    instructionsRaw = "Boil pasta in salted water until al dente.|In a skillet, soften onions with butter, then add chopped tomatoes and simmer to create a rich sauce.|Fold in cooked pasta with a splash of pasta water.|Top with generous grated cheese and freshly cracked black pepper.",
                    heroEmoji = "🍝"
                ),
                Recipe(
                    title = "Golden French Toast",
                    description = "Classic breakfast favorite made specifically for using up dry or stale sliced bread.",
                    mealType = "Breakfast",
                    dietaryTags = "Vegetarian, Quick (<25 min)",
                    prepTimeMinutes = 5,
                    cookTimeMinutes = 8,
                    servings = 2,
                    difficulty = "Easy",
                    ingredientsRaw = "Bread; Eggs; Milk; Butter; Bananas",
                    instructionsRaw = "In a shallow dish, whisk together eggs and milk.|Heat butter in skillet over medium heat.|Dip each bread slice for 10 seconds per side until soaked.|Cook 3-4 minutes each side until golden and caramelised. Serve with sliced bananas.",
                    heroEmoji = "🍞"
                ),
                Recipe(
                    title = "Green Energy Smoothie",
                    description = "Hydrating, vitamin-packed smoothie that rescues fresh spinach leaves and bananas.",
                    mealType = "Breakfast",
                    dietaryTags = "Vegetarian, Gluten-Free, Quick (<25 min)",
                    prepTimeMinutes = 5,
                    cookTimeMinutes = 0,
                    servings = 2,
                    difficulty = "Easy",
                    ingredientsRaw = "Spinach; Bananas; Milk",
                    instructionsRaw = "Add fresh spinach leaves, sliced banana, and chilled milk to blender.|Blend on high speed for 60 seconds until silky smooth.|Pour into glasses and enjoy immediately.",
                    heroEmoji = "🥤"
                ),
                Recipe(
                    title = "Crispy Cheese Toastie",
                    description = "Satisfying hot toasted sandwich to utilize remaining cheese blocks and bread heels.",
                    mealType = "Lunch",
                    dietaryTags = "Vegetarian, Quick (<25 min)",
                    prepTimeMinutes = 3,
                    cookTimeMinutes = 6,
                    servings = 1,
                    difficulty = "Easy",
                    ingredientsRaw = "Bread; Cheese; Butter; Tomatoes",
                    instructionsRaw = "Butter the outer sides of bread slices.|Layer sliced cheese and tomato slices inside.|Grill in a non-stick pan over medium-low heat until bread is golden crusty and cheese is fully melted.",
                    heroEmoji = "🥪"
                ),
                Recipe(
                    title = "Roasted Garlic Herb Rice Bowl",
                    description = "Savory warm grain bowl topped with tender onions and melted cheese.",
                    mealType = "Lunch",
                    dietaryTags = "Vegetarian, Gluten-Free",
                    prepTimeMinutes = 10,
                    cookTimeMinutes = 20,
                    servings = 2,
                    difficulty = "Easy",
                    ingredientsRaw = "Rice; Onion; Tomatoes; Cheese; Butter",
                    instructionsRaw = "Cook rice with a pat of butter.|Caramelize sliced onions in a pan until sweet and golden.|Toss warm rice with roasted tomatoes and onions.|Melt cheese over the top and season with herbs.",
                    heroEmoji = "🥗"
                )
            )
            db.recipeDao().insertRecipes(sampleRecipes)

            // Seed initial donation drop-off locations
            db.donationDao().insertDonation(
                Donation(
                    userId = 1,
                    foodTitle = "Unopened Pasta & Canned Goods",
                    quantity = "4 packs",
                    destination = "Downtown Community Food Hub",
                    status = "Completed",
                    notes = "Delivered to north pantry shelf"
                )
            )

            // Seed initial impact logs
            db.wasteLogDao().insertWasteLog(
                WasteLog(
                    userId = 1,
                    foodName = "Leftover Rice",
                    category = "PANTRY",
                    quantityWithUnit = "300 g",
                    actionType = "RESCUED",
                    costAmount = 2.50,
                    co2SavedKg = 0.75,
                    reason = "Turned into Pantry Fried Rice"
                )
            )
            db.wasteLogDao().insertWasteLog(
                WasteLog(
                    userId = 1,
                    foodName = "Stale Baguette",
                    category = "BAKERY",
                    quantityWithUnit = "1 loaf",
                    actionType = "RESCUED",
                    costAmount = 3.00,
                    co2SavedKg = 0.85,
                    reason = "Made golden French Toast"
                )
            )
        }
    }
}
