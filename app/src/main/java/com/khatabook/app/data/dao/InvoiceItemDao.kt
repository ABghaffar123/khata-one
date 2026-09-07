package com.khatabook.app.data.dao

import androidx.room.*
import com.khatabook.app.data.entity.InvoiceItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceItemDao {

    @Query("SELECT * FROM invoice_items WHERE transactionId = :transactionId")
    fun getItemsForTransaction(transactionId: Long): Flow<List<InvoiceItemEntity>>

    @Query("SELECT * FROM invoice_items WHERE transactionId = :transactionId")
    suspend fun getItemsForTransactionOnce(transactionId: Long): List<InvoiceItemEntity>

    @Query("SELECT SUM(totalPrice) FROM invoice_items WHERE transactionId = :transactionId")
    fun getTransactionTotal(transactionId: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InvoiceItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InvoiceItemEntity>)

    @Delete
    suspend fun deleteItem(item: InvoiceItemEntity)

    @Query("DELETE FROM invoice_items WHERE transactionId = :transactionId")
    suspend fun deleteItemsForTransaction(transactionId: Long)
}
