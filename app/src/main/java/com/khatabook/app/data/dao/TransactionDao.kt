package com.khatabook.app.data.dao

import androidx.room.*
import com.khatabook.app.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY date DESC")
    fun getTransactionsForCustomer(customerId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY date DESC")
    suspend fun getTransactionsForCustomerOnce(customerId: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionById(id: Long): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsForCustomerBetweenDates(
        customerId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE customerId = :customerId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getTransactionsForCustomerBetweenDatesOnce(
        customerId: Long,
        startDate: Long,
        endDate: Long
    ): List<TransactionEntity>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE type IN (1, 2) 
        AND date BETWEEN :startOfDay AND :endOfDay
    """)
    fun getTodayCollected(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE date BETWEEN :startOfMonth AND :endOfMonth
    """)
    fun getThisMonthTotal(startOfMonth: Long, endOfMonth: Long): Flow<Double?>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByAccountBetweenDates(accountId: Long, startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE dueDate IS NOT NULL AND dueDate < :currentTime AND type = 0
        ORDER BY dueDate ASC
    """)
    fun getOverdueTransactions(currentTime: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}
