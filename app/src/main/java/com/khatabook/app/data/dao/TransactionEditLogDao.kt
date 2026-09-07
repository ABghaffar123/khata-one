package com.khatabook.app.data.dao

import androidx.room.*
import com.khatabook.app.data.entity.TransactionEditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionEditLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TransactionEditLogEntity): Long

    @Query("SELECT * FROM transaction_edit_logs WHERE transactionId = :transactionId ORDER BY timestamp DESC")
    fun getLogsForTransaction(transactionId: Long): Flow<List<TransactionEditLogEntity>>

    @Query("SELECT * FROM transaction_edit_logs WHERE transactionId = :transactionId ORDER BY timestamp DESC")
    suspend fun getLogsForTransactionOnce(transactionId: Long): List<TransactionEditLogEntity>

    @Query("SELECT * FROM transaction_edit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<TransactionEditLogEntity>>

    @Query("DELETE FROM transaction_edit_logs WHERE transactionId = :transactionId")
    suspend fun deleteLogsForTransaction(transactionId: Long)
}
