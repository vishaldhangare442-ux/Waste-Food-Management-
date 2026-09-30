package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.concurrent.TimeUnit

enum class StorageLocation(val label: String, val iconName: String) {
    FRIDGE("Fridge", "Kitchen"),
    PANTRY("Pantry", "Inventory"),
    FREEZER("Freezer", "AcUnit")
}

enum class FoodCategory(val label: String) {
    PRODUCE("Fruits & Veggies"),
    DAIRY("Dairy & Eggs"),
    MEAT("Meat & Protein"),
    BAKERY("Bakery & Bread"),
    PANTRY("Grains & Pantry"),
    PREPARED("Leftovers & Meals"),
    BEVERAGES("Beverages"),
    OTHER("Other")
}

enum class FoodStatus {
    IN_STOCK,
    CONSUMED,
    WASTED,
    DONATED
}

enum class ExpiryUrgency {
    EXPIRED,
    CRITICAL_TODAY,
    EXPIRING_SOON,
    FRESH
}

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val name: String,
    val category: String = FoodCategory.PRODUCE.name,
    val quantity: Double = 1.0,
    val unit: String = "pcs",
    val storageLocation: String = StorageLocation.FRIDGE.name,
    val purchaseDate: Long = System.currentTimeMillis(),
    val expiryDate: Long = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(4),
    val status: String = FoodStatus.IN_STOCK.name,
    val priceEstimate: Double = 3.50,
    val notes: String = ""
) {
    fun daysUntilExpiry(now: Long = System.currentTimeMillis()): Int {
        val diff = expiryDate - now
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun getExpiryUrgency(now: Long = System.currentTimeMillis()): ExpiryUrgency {
        val days = daysUntilExpiry(now)
        return when {
            days < 0 -> ExpiryUrgency.EXPIRED
            days == 0 -> ExpiryUrgency.CRITICAL_TODAY
            days <= 3 -> ExpiryUrgency.EXPIRING_SOON
            else -> ExpiryUrgency.FRESH
        }
    }
}
