package com.khatabook.app.data.repository

import com.khatabook.app.data.dao.SupplierTransactionDao
import com.khatabook.app.data.entity.SupplierTransactionEntity
import com.khatabook.app.util.PkDateTime
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupplierTransactionRepository @Inject constructor(
    private val supplierTransactionDao: SupplierTransactionDao
) {
    fun getAllTransactions(): Flow<List<SupplierTransactionEntity>> =
        supplierTransactionDao.getAllTransactions()

    fun getTransactionsForSupplier(supplierId: Long): Flow<List<SupplierTransactionEntity>> =
        supplierTransactionDao.getTransactionsForSupplier(supplierId)

    suspend fun getTransactionsForSupplierOnce(supplierId: Long): List<SupplierTransactionEntity> =
        supplierTransactionDao.getTransactionsForSupplierOnce(supplierId)

    /**
     * Get today's purchases in PKT timezone.
     */
    fun getTodayPurchases(): Flow<Double?> {
        val (start, end) = PkDateTime.pktTodayRange()
        return supplierTransactionDao.getTodayPurchases(start, end)
    }

    /**
     * Get today's payments in PKT timezone.
     */
    fun getTodayPayments(): Flow<Double?> {
        val (start, end) = PkDateTime.pktTodayRange()
        return supplierTransactionDao.getTodayPayments(start, end)
    }

    suspend fun insertTransaction(transaction: SupplierTransactionEntity): Long =
        supplierTransactionDao.insertTransaction(transaction)

    suspend fun deleteTransaction(transaction: SupplierTransactionEntity) =
        supplierTransactionDao.deleteTransaction(transaction)
}
