package com.khatabook.app.data.dao

import androidx.room.*
import com.khatabook.app.data.entity.CaptureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CaptureDao {

    @Query("SELECT * FROM captures ORDER BY createdAt DESC")
    fun getAllCaptures(): Flow<List<CaptureEntity>>

    @Query("SELECT * FROM captures WHERE id = :id")
    fun getCaptureById(id: Long): Flow<CaptureEntity?>

    @Query("SELECT * FROM captures WHERE id = :id")
    suspend fun getCaptureByIdOnce(id: Long): CaptureEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCapture(capture: CaptureEntity): Long

    @Update
    suspend fun updateCapture(capture: CaptureEntity)

    @Delete
    suspend fun deleteCapture(capture: CaptureEntity)
}
