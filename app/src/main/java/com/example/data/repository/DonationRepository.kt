package com.example.data.repository

import com.example.data.local.DonationDao
import com.example.data.local.WasteLogDao
import com.example.data.model.CommunityDropOffCenter
import com.example.data.model.Donation
import com.example.data.model.WasteLog
import com.example.data.remote.FirestoreService
import kotlinx.coroutines.flow.Flow

data class WasteImpactStats(
    val totalRescuedValue: Double,
    val totalWastedValue: Double,
    val totalCo2SavedKg: Double,
    val rescuedItemCount: Int,
    val wastedItemCount: Int
) {
    val rescueRatePercentage: Int
        get() {
            val total = rescuedItemCount + wastedItemCount
            return if (total > 0) (rescuedItemCount * 100) / total else 100
        }
}

class DonationRepository(
    private val donationDao: DonationDao,
    private val wasteLogDao: WasteLogDao,
    private val firestoreService: FirestoreService = FirestoreService()
) {
    fun getDonations(userId: Long): Flow<List<Donation>> = donationDao.getDonations(userId)
    fun getAllDonations(): Flow<List<Donation>> = donationDao.getAllDonations()
    fun getActiveDeliveries(): Flow<List<Donation>> = donationDao.getActiveDeliveries()
    fun getDonationById(id: Long): Flow<Donation?> = donationDao.getDonationById(id)
    fun getWasteLogs(userId: Long): Flow<List<WasteLog>> = wasteLogDao.getWasteLogs(userId)

    suspend fun insertDonation(donation: Donation): Long {
        val id = donationDao.insertDonation(donation)
        val saved = if (donation.id == 0L) donation.copy(id = id) else donation
        firestoreService.syncDonation(saved)
        return id
    }

    suspend fun updateDonation(donation: Donation) {
        donationDao.updateDonation(donation)
        firestoreService.syncDonation(donation)
    }

    suspend fun deleteDonation(donation: Donation) = donationDao.deleteDonation(donation)

    suspend fun claimFoodDonation(
        donation: Donation,
        receiverType: String,
        receiverName: String,
        receiverPhone: String,
        receiverDetails: String,
        dropAddress: String,
        deliveryMethod: String
    ) {
        val updated = donation.copy(
            status = "CLAIMED",
            receiverType = receiverType,
            receiverName = receiverName,
            receiverPhone = receiverPhone,
            receiverDetails = receiverDetails,
            dropAddress = dropAddress.ifBlank { donation.dropAddress },
            deliveryMethod = deliveryMethod,
            dateClaimed = System.currentTimeMillis(),
            deliveryProgress = 0.15f,
            etaMinutes = if (deliveryMethod == "SELF_PICKUP") 30 else 25
        )
        donationDao.updateDonation(updated)
        firestoreService.syncDonation(updated)
    }

    suspend fun advanceDelivery(donation: Donation) {
        val nextStatus: String
        val nextProgress: Float
        val nextEta: Int
        when (donation.status) {
            "CLAIMED" -> {
                nextStatus = "OUT_FOR_PICKUP"
                nextProgress = 0.40f
                nextEta = 18
            }
            "OUT_FOR_PICKUP" -> {
                nextStatus = "IN_TRANSIT"
                nextProgress = 0.75f
                nextEta = 8
            }
            "IN_TRANSIT" -> {
                nextStatus = "DELIVERED"
                nextProgress = 1.0f
                nextEta = 0
            }
            else -> return
        }

        val updated = donation.copy(
            status = nextStatus,
            deliveryProgress = nextProgress,
            etaMinutes = nextEta,
            dateDelivered = if (nextStatus == "DELIVERED") System.currentTimeMillis() else donation.dateDelivered
        )
        donationDao.updateDonation(updated)
        firestoreService.syncDonation(updated)

        if (nextStatus == "DELIVERED") {
            // Log into impact table
            val co2 = (donation.weightKg * 2.5).coerceAtLeast(1.0)
            wasteLogDao.insertWasteLog(
                WasteLog(
                    userId = donation.userId,
                    foodName = donation.foodTitle,
                    category = donation.category,
                    quantityWithUnit = donation.quantity,
                    actionType = "RESCUED",
                    costAmount = donation.servingsEstimate * 4.50,
                    co2SavedKg = co2,
                    reason = "Rescued by ${donation.receiverName} (${donation.receiverType})"
                )
            )
        }
    }

    fun getCommunityCenters(): List<CommunityDropOffCenter> {
        return listOf(
            CommunityDropOffCenter(
                name = "City Care Community Fridge",
                address = "420 Market Street, Downtown",
                hours = "Open 24/7 (Outdoor Fridge)",
                acceptedItems = "Fresh fruits, vegetables, bakery items, sealed shelf-stable goods",
                phone = "(555) 234-5678"
            ),
            CommunityDropOffCenter(
                name = "Second Harvest Food Bank",
                address = "1850 Riverbank Way",
                hours = "Mon-Sat: 8:00 AM - 6:00 PM",
                acceptedItems = "Unopened dry pantry goods, canned meals, unopened dairy",
                phone = "(555) 890-1234"
            ),
            CommunityDropOffCenter(
                name = "Hope Shelter Kitchen",
                address = "712 Elm Avenue",
                hours = "Daily: 9:00 AM - 5:00 PM",
                acceptedItems = "Bulk produce, surplus bread, catering packages with labels",
                phone = "(555) 456-7890"
            ),
            CommunityDropOffCenter(
                name = "Green Neighbors Share Hub",
                address = "305 Oak Road, Community Center",
                hours = "Tue, Thu, Sat: 10:00 AM - 4:00 PM",
                acceptedItems = "Home garden surplus, extra baking supplies, clean pantry items",
                phone = "(555) 678-9012"
            )
        )
    }
}
