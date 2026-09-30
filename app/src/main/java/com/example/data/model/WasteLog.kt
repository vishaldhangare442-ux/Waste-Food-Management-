package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waste_logs")
data class WasteLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val foodName: String,
    val category: String,
    val quantityWithUnit: String,
    val actionType: String, // "RESCUED" (e.g. Cooked/Consumed) or "WASTED"
    val costAmount: Double,
    val co2SavedKg: Double, // Approx 2.5 kg CO2 per kg food saved
    val reason: String = "", // e.g. "Cooked with recipe", "Expired before use", "Overbought"
    val timestamp: Long = System.currentTimeMillis()
)
