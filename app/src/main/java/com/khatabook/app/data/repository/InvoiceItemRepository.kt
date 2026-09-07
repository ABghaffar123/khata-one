package com.khatabook.app.data.repository

import com.khatabook.app.data.dao.InvoiceItemDao
import com.khatabook.app.data.entity.InvoiceItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceItemRepository @Inject constructor(
    private val invoiceItemDao: InvoiceItemDao
) {
    fun getItemsForTransaction(transactionId: Long): Flow<List<InvoiceItemEntity>> =
        invoiceItemDao.getItemsForTransaction(transactionId)

    suspend fun getItemsForTransactionOnce(transactionId: Long): List<InvoiceItemEntity> =
        invoiceItemDao.getItemsForTransactionOnce(transactionId)

    suspend fun insertItems(items: List<InvoiceItemEntity>) = invoiceItemDao.insertItems(items)

    suspend fun deleteItemsForTransaction(transactionId: Long) =
        invoiceItemDao.deleteItemsForTransaction(transactionId)
}
