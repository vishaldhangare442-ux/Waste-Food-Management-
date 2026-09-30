package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FoodItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Query("SELECT * FROM food_items WHERE userId = :userId AND status = 'IN_STOCK' ORDER BY expiryDate ASC")
    fun getInStockItems(userId: Long): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE userId = :userId ORDER BY expiryDate ASC")
    fun getAllItems(userId: Long): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): FoodItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: FoodItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<FoodItem>)

    @Update
    suspend fun updateItem(item: FoodItem)

    @Delete
    suspend fun deleteItem(item: FoodItem)

    @Query("UPDATE food_items SET status = :status WHERE id = :id")
    suspend fun updateItemStatus(id: Long, status: String)

    @Query("SELECT COUNT(*) FROM food_items WHERE userId = :userId AND status = 'IN_STOCK'")
    fun getInStockCount(userId: Long): Flow<Int>
}
