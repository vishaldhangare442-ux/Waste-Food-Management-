package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Donation
import com.example.data.model.WasteLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DonationDao {
    @Query("SELECT * FROM donations WHERE userId = :userId ORDER BY dateLogged DESC")
    fun getDonations(userId: Long): Flow<List<Donation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: Donation): Long

    @Update
    suspend fun updateDonation(donation: Donation)

    @Delete
    suspend fun deleteDonation(donation: Donation)
}

@Dao
interface WasteLogDao {
    @Query("SELECT * FROM waste_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getWasteLogs(userId: Long): Flow<List<WasteLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWasteLog(log: WasteLog): Long

    @Query("SELECT SUM(costAmount) FROM waste_logs WHERE userId = :userId AND actionType = 'RESCUED'")
    fun getTotalRescuedValue(userId: Long): Flow<Double?>

    @Query("SELECT SUM(costAmount) FROM waste_logs WHERE userId = :userId AND actionType = 'WASTED'")
    fun getTotalWastedValue(userId: Long): Flow<Double?>

    @Query("SELECT SUM(co2SavedKg) FROM waste_logs WHERE userId = :userId AND actionType = 'RESCUED'")
    fun getTotalCo2Saved(userId: Long): Flow<Double?>
}
