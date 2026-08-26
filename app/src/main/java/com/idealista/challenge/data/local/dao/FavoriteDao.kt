package com.idealista.challenge.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.idealista.challenge.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE propertyCode = :propertyCode")
    fun observeOne(propertyCode: String): Flow<FavoriteEntity?>

    @Query("SELECT * FROM favorites WHERE propertyCode = :propertyCode")
    suspend fun findOne(propertyCode: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Delete
    suspend fun delete(favorite: FavoriteEntity)

    /** Read-then-write favorite toggle, wrapped in a transaction so it's atomic. */
    @Transaction
    suspend fun toggle(propertyCode: String, nowEpochMillis: Long) {
        val existing = findOne(propertyCode)
        if (existing != null) {
            delete(existing)
        } else {
            insert(FavoriteEntity(propertyCode, nowEpochMillis))
        }
    }
}
