package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "donations")
data class Donation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val foodTitle: String,
    val quantity: String,
    val destination: String, // e.g. "Downtown Community Fridge", "Second Harvest Food Bank"
    val dateLogged: Long = System.currentTimeMillis(),
    val status: String = "Scheduled", // "Scheduled", "Completed", "Cancelled"
    val contactPhone: String = "",
    val notes: String = ""
)

data class CommunityDropOffCenter(
    val name: String,
    val address: String,
    val hours: String,
    val acceptedItems: String,
    val phone: String
)
