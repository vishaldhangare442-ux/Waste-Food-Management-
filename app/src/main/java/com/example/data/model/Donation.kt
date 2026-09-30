package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ReceiverType(val label: String, val badgeColorHex: Long) {
    NGO("NGO / Food Bank", 0xFF2563EB),
    INDIVIDUAL("Individual / Family", 0xFFD97706)
}

enum class DeliveryStatus(val label: String, val colorHex: Long) {
    AVAILABLE("Available for Rescue", 0xFF16A34A),
    CLAIMED("Claimed", 0xFF2563EB),
    OUT_FOR_PICKUP("Out for Pickup", 0xFFD97706),
    IN_TRANSIT("In Transit", 0xFF9333EA),
    DELIVERED("Delivered & Saved", 0xFF059669)
}

@Entity(tableName = "donations")
data class Donation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val donorName: String = "Green Kitchen Deli",
    val donorNumber: String = "+1 (555) 234-8890",
    val foodTitle: String,
    val category: String = "COOKED_MEALS", // "COOKED_MEALS", "BAKERY", "PRODUCE", "DAIRY", "PACKAGED"
    val quantity: String = "15 servings",
    val servingsEstimate: Int = 15,
    val weightKg: Double = 6.0,
    val foodPhotoUri: String = "",
    val foodPresetName: String = "COOKED_MEAL", // "COOKED_MEAL", "BAKERY", "PRODUCE", "DAIRY", "PANTRY", "BUFFET"
    val preparedTimeMillis: Long = System.currentTimeMillis() - 3600000L,
    val expiryHours: Int = 6,
    val pickupAddress: String = "742 Evergreen Terrace, Downtown",
    val pickupLatitude: Double = 37.7749,
    val pickupLongitude: Double = -122.4194,
    val dropAddress: String = "1850 Riverbank Way, Food Shelter",
    val dropLatitude: Double = 37.7833,
    val dropLongitude: Double = -122.4167,
    val currentDeliveryLat: Double = 37.7785,
    val currentDeliveryLng: Double = -122.4180,
    val deliveryProgress: Float = 0.45f, // 0.0 to 1.0 along the route
    val status: String = "AVAILABLE", // "AVAILABLE", "CLAIMED", "OUT_FOR_PICKUP", "IN_TRANSIT", "DELIVERED"
    val receiverType: String = "", // "NGO" or "INDIVIDUAL"
    val receiverName: String = "", // e.g. "St. Jude Food Shelter (NGO)" or "Maria Watson (Individual)"
    val receiverPhone: String = "",
    val receiverDetails: String = "", // e.g. "Reg #NGO-9842" or "Household of 4"
    val deliveryMethod: String = "VOLUNTEER_DELIVERY", // "VOLUNTEER_DELIVERY", "SELF_PICKUP"
    val courierName: String = "Carlos Rivera (Eco Courier #12)",
    val courierPhone: String = "+1 (555) 432-1098",
    val etaMinutes: Int = 14,
    val specialInstructions: String = "",
    val destination: String = "", // for backwards compatibility
    val notes: String = "",
    val dateLogged: Long = System.currentTimeMillis(),
    val dateClaimed: Long = 0L,
    val dateDelivered: Long = 0L
)

data class ReceiverProfile(
    val type: ReceiverType = ReceiverType.NGO,
    val name: String = "Helping Hands Food Bank",
    val phone: String = "+1 (555) 987-6543",
    val address: String = "1200 Hope Way, Suite 4",
    val identifierOrSize: String = "NGO Reg #4421-B", // e.g. registration ID or household size
    val dailyCapacityMeals: Int = 150
)

data class FoodNeedRequest(
    val id: Long = System.currentTimeMillis(),
    val receiverType: ReceiverType = ReceiverType.NGO,
    val requesterName: String = "Helping Hands Food Bank",
    val title: String,
    val peopleCount: Int = 40,
    val urgency: String = "Today", // "Immediate (<2h)", "Today", "This Week"
    val phone: String = "+1 (555) 987-6543",
    val dropAddress: String = "1200 Hope Way, Suite 4",
    val notes: String = "",
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val distanceMiles: Double = 1.2,
    val category: String = "Cooked Meals",
    val timestamp: Long = System.currentTimeMillis()
)

data class CommunityDropOffCenter(
    val name: String,
    val address: String,
    val hours: String,
    val acceptedItems: String,
    val phone: String
)
