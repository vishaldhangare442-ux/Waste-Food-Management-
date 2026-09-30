package com.example.data.repository

import com.example.data.local.DonationDao
import com.example.data.local.FoodItemDao
import com.example.data.local.WasteLogDao
import com.example.data.model.Donation
import com.example.data.model.FoodItem
import com.example.data.model.FoodStatus
import com.example.data.model.Recipe
import com.example.data.model.WasteLog
import com.example.data.remote.FirestoreService
import kotlinx.coroutines.flow.Flow

class FoodRepository(
    private val foodItemDao: FoodItemDao,
    private val wasteLogDao: WasteLogDao,
    private val donationDao: DonationDao,
    private val firestoreService: FirestoreService = FirestoreService()
) {
    fun getInStockItems(userId: Long): Flow<List<FoodItem>> = foodItemDao.getInStockItems(userId)
    fun getAllItems(userId: Long): Flow<List<FoodItem>> = foodItemDao.getAllItems(userId)

    suspend fun insertItem(item: FoodItem): Long {
        val id = foodItemDao.insertItem(item)
        firestoreService.syncInventoryItem(if (item.id == 0L) item.copy(id = id) else item)
        return id
    }

    suspend fun updateItem(item: FoodItem) {
        foodItemDao.updateItem(item)
        firestoreService.syncInventoryItem(item)
    }

    suspend fun deleteItem(item: FoodItem) = foodItemDao.deleteItem(item)

    suspend fun markAsConsumed(item: FoodItem, reason: String = "Cooked / Consumed") {
        foodItemDao.updateItem(item.copy(status = FoodStatus.CONSUMED.name))
        val co2 = calculateCo2Savings(item.quantity, item.unit)
        wasteLogDao.insertWasteLog(
            WasteLog(
                userId = item.userId,
                foodName = item.name,
                category = item.category,
                quantityWithUnit = "${item.quantity} ${item.unit}",
                actionType = "RESCUED",
                costAmount = item.priceEstimate,
                co2SavedKg = co2,
                reason = reason
            )
        )
    }

    suspend fun markAsWasted(item: FoodItem, reason: String) {
        foodItemDao.updateItem(item.copy(status = FoodStatus.WASTED.name))
        wasteLogDao.insertWasteLog(
            WasteLog(
                userId = item.userId,
                foodName = item.name,
                category = item.category,
                quantityWithUnit = "${item.quantity} ${item.unit}",
                actionType = "WASTED",
                costAmount = item.priceEstimate,
                co2SavedKg = 0.0,
                reason = reason.ifBlank { "Expired before use" }
            )
        )
    }

    suspend fun markAsDonated(item: FoodItem, destination: String, notes: String = "") {
        foodItemDao.updateItem(item.copy(status = FoodStatus.DONATED.name))
        donationDao.insertDonation(
            Donation(
                userId = item.userId,
                foodTitle = item.name,
                quantity = "${item.quantity} ${item.unit}",
                destination = destination.ifBlank { "Community Food Drop-off" },
                status = "Completed",
                notes = notes
            )
        )
        val co2 = calculateCo2Savings(item.quantity, item.unit)
        wasteLogDao.insertWasteLog(
            WasteLog(
                userId = item.userId,
                foodName = item.name,
                category = item.category,
                quantityWithUnit = "${item.quantity} ${item.unit}",
                actionType = "RESCUED",
                costAmount = item.priceEstimate,
                co2SavedKg = co2,
                reason = "Donated to $destination"
            )
        )
    }

    suspend fun cookRecipeAndDeduct(recipe: Recipe, inStockItems: List<FoodItem>): Int {
        var itemsRescuedCount = 0
        var totalSavings = 0.0
        val recipeIngredients = recipe.getIngredientList()

        for (recipeIngredient in recipeIngredients) {
            val matchedItem = inStockItems.firstOrNull { inv ->
                isIngredientMatch(inv.name, recipeIngredient)
            }
            if (matchedItem != null) {
                itemsRescuedCount++
                totalSavings += matchedItem.priceEstimate
                if (matchedItem.quantity <= 1.0) {
                    foodItemDao.updateItem(matchedItem.copy(status = FoodStatus.CONSUMED.name))
                } else {
                    foodItemDao.updateItem(matchedItem.copy(quantity = matchedItem.quantity - 1.0))
                }
            }
        }

        wasteLogDao.insertWasteLog(
            WasteLog(
                userId = inStockItems.firstOrNull()?.userId ?: 1L,
                foodName = recipe.title,
                category = "PREPARED",
                quantityWithUnit = "$itemsRescuedCount items used",
                actionType = "RESCUED",
                costAmount = totalSavings,
                co2SavedKg = itemsRescuedCount * 0.85,
                reason = "Cooked recipe: ${recipe.title}"
            )
        )

        return itemsRescuedCount
    }

    private fun isIngredientMatch(inventoryName: String, recipeIngredient: String): Boolean {
        val inv = inventoryName.trim().lowercase()
        val rec = recipeIngredient.trim().lowercase()
        return inv.contains(rec) || rec.contains(inv) ||
                inv.removeSuffix("s").contains(rec.removeSuffix("s")) ||
                rec.removeSuffix("s").contains(inv.removeSuffix("s"))
    }

    private fun calculateCo2Savings(quantity: Double, unit: String): Double {
        // Average carbon footprint factor ~2.5 kg CO2 per kg food rescued
        val weightInKg = when (unit.lowercase()) {
            "kg" -> quantity
            "g" -> quantity / 1000.0
            "l" -> quantity * 1.0
            "ml" -> quantity / 1000.0
            "slices" -> quantity * 0.04
            else -> quantity * 0.2 // default approx 200g per piece/unit
        }
        return (weightInKg * 2.5).coerceAtLeast(0.1)
    }
}
