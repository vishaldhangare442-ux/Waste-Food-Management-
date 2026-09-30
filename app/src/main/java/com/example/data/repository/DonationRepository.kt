package com.example.data.repository

import com.example.data.local.DonationDao
import com.example.data.local.WasteLogDao
import com.example.data.model.CommunityDropOffCenter
import com.example.data.model.Donation
import com.example.data.model.WasteLog
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
    private val wasteLogDao: WasteLogDao
) {
    fun getDonations(userId: Long): Flow<List<Donation>> = donationDao.getDonations(userId)
    fun getWasteLogs(userId: Long): Flow<List<WasteLog>> = wasteLogDao.getWasteLogs(userId)

    suspend fun insertDonation(donation: Donation): Long = donationDao.insertDonation(donation)
    suspend fun updateDonation(donation: Donation) = donationDao.updateDonation(donation)
    suspend fun deleteDonation(donation: Donation) = donationDao.deleteDonation(donation)

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
