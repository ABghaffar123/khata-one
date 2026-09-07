package com.khatabook.app.data.dao

import androidx.room.*
import com.khatabook.app.data.entity.SupplierTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierTransactionDao {

    @Query("SELECT * FROM supplier_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<SupplierTransactionEntity>>

    @Query("SELECT * FROM supplier_transactions WHERE supplierId = :supplierId ORDER BY date DESC")
    fun getTransactionsForSupplier(supplierId: Long): Flow<List<SupplierTransactionEntity>>

    @Query("SELECT * FROM supplier_transactions WHERE supplierId = :supplierId ORDER BY date DESC")
    suspend fun getTransactionsForSupplierOnce(supplierId: Long): List<SupplierTransactionEntity>

    @Query("SELECT * FROM supplier_transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<SupplierTransactionEntity>>

    @Query("SELECT SUM(amount) FROM supplier_transactions WHERE type = 0 AND date BETWEEN :startOfDay AND :endOfDay")
    fun getTodayPurchases(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM supplier_transactions WHERE type = 1 AND date BETWEEN :startOfDay AND :endOfDay")
    fun getTodayPayments(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: SupplierTransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: SupplierTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: SupplierTransactionEntity)
}
