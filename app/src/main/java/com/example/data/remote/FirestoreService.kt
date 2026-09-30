package com.example.data.remote

import android.util.Log
import com.example.data.model.Donation
import com.example.data.model.FoodItem
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestoreService {
    private val tag = "FirestoreService"

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(tag, "Firestore not initialized (likely missing google-services.json): ${e.message}")
            null
        }
    }

    val isAvailable: Boolean
        get() = firestore != null

    // Real-time listener for donations
    fun observeDonations(): Flow<List<Donation>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            registration = db.collection("donations")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(tag, "Error observing donations: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val donations = snapshot.documents.mapNotNull { doc ->
                            try {
                                Donation(
                                    id = doc.getLong("id") ?: 0L,
                                    userId = doc.getLong("userId") ?: 1L,
                                    donorName = doc.getString("donorName") ?: "Donor",
                                    donorNumber = doc.getString("donorNumber") ?: "",
                                    foodTitle = doc.getString("foodTitle") ?: "Surplus Food",
                                    category = doc.getString("category") ?: "COOKED_MEALS",
                                    quantity = doc.getString("quantity") ?: "1",
                                    servingsEstimate = doc.getLong("servingsEstimate")?.toInt() ?: 10,
                                    weightKg = doc.getDouble("weightKg") ?: 5.0,
                                    foodPhotoUri = doc.getString("foodPhotoUri") ?: "",
                                    foodPresetName = doc.getString("foodPresetName") ?: "COOKED_MEAL",
                                    pickupAddress = doc.getString("pickupAddress") ?: "",
                                    dropAddress = doc.getString("dropAddress") ?: "",
                                    status = doc.getString("status") ?: "AVAILABLE",
                                    receiverType = doc.getString("receiverType") ?: "",
                                    receiverName = doc.getString("receiverName") ?: "",
                                    receiverPhone = doc.getString("receiverPhone") ?: "",
                                    receiverDetails = doc.getString("receiverDetails") ?: "",
                                    deliveryMethod = doc.getString("deliveryMethod") ?: "VOLUNTEER_DELIVERY",
                                    courierName = doc.getString("courierName") ?: "Eco Courier",
                                    courierPhone = doc.getString("courierPhone") ?: "",
                                    etaMinutes = doc.getLong("etaMinutes")?.toInt() ?: 15,
                                    deliveryProgress = doc.getDouble("deliveryProgress")?.toFloat() ?: 0.0f,
                                    specialInstructions = doc.getString("specialInstructions") ?: "",
                                    dateLogged = doc.getLong("dateLogged") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(donations)
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "Failed to register donation snapshot listener: ${e.message}")
        }

        awaitClose {
            registration?.remove()
        }
    }

    // Push or update donation in real-time
    fun syncDonation(donation: Donation) {
        val db = firestore ?: return
        try {
            val docId = if (donation.id > 0) donation.id.toString() else System.currentTimeMillis().toString()
            val data = hashMapOf(
                "id" to donation.id,
                "userId" to donation.userId,
                "donorName" to donation.donorName,
                "donorNumber" to donation.donorNumber,
                "foodTitle" to donation.foodTitle,
                "category" to donation.category,
                "quantity" to donation.quantity,
                "servingsEstimate" to donation.servingsEstimate,
                "weightKg" to donation.weightKg,
                "foodPhotoUri" to donation.foodPhotoUri,
                "foodPresetName" to donation.foodPresetName,
                "pickupAddress" to donation.pickupAddress,
                "dropAddress" to donation.dropAddress,
                "status" to donation.status,
                "receiverType" to donation.receiverType,
                "receiverName" to donation.receiverName,
                "receiverPhone" to donation.receiverPhone,
                "receiverDetails" to donation.receiverDetails,
                "deliveryMethod" to donation.deliveryMethod,
                "courierName" to donation.courierName,
                "courierPhone" to donation.courierPhone,
                "etaMinutes" to donation.etaMinutes,
                "deliveryProgress" to donation.deliveryProgress,
                "specialInstructions" to donation.specialInstructions,
                "dateLogged" to donation.dateLogged,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("donations").document(docId).set(data, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(tag, "Donation synchronized to Firestore: ${donation.foodTitle}")
                }
                .addOnFailureListener { e ->
                    Log.w(tag, "Failed to sync donation to Firestore: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w(tag, "Exception during Firestore donation sync: ${e.message}")
        }
    }

    // Sync inventory items
    fun syncInventoryItem(item: FoodItem) {
        val db = firestore ?: return
        try {
            val docId = if (item.id > 0) item.id.toString() else System.currentTimeMillis().toString()
            val data = hashMapOf(
                "id" to item.id,
                "userId" to item.userId,
                "name" to item.name,
                "category" to item.category,
                "quantity" to item.quantity,
                "unit" to item.unit,
                "storageLocation" to item.storageLocation,
                "expiryDate" to item.expiryDate,
                "status" to item.status,
                "priceEstimate" to item.priceEstimate,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("inventory").document(docId).set(data, SetOptions.merge())
        } catch (e: Exception) {
            Log.w(tag, "Exception during Firestore inventory sync: ${e.message}")
        }
    }
}
