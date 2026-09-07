package com.khatabook.app.data.repository

import com.khatabook.app.data.dao.TransactionDao
import com.khatabook.app.data.dao.TransactionEditLogDao
import com.khatabook.app.data.entity.EditAction
import com.khatabook.app.data.entity.TransactionEditLogEntity
import com.khatabook.app.data.entity.TransactionEntity
import com.khatabook.app.util.PkDateTime
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val editLogDao: TransactionEditLogDao
) {
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsForCustomer(customerId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForCustomer(customerId)

    suspend fun getTransactionsForCustomerOnce(customerId: Long): List<TransactionEntity> =
        transactionDao.getTransactionsForCustomerOnce(customerId)

    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsBetweenDates(startDate, endDate)

    fun getTransactionsForCustomerBetweenDates(
        customerId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionEntity>> = transactionDao.getTransactionsForCustomerBetweenDates(customerId, startDate, endDate)

    suspend fun getTransactionsForCustomerBetweenDatesOnce(
        customerId: Long,
        startDate: Long,
        endDate: Long
    ): List<TransactionEntity> = transactionDao.getTransactionsForCustomerBetweenDatesOnce(customerId, startDate, endDate)

    fun getTodayCollected(): Flow<Double?> {
        val (startOfDay, endOfDay) = PkDateTime.pktTodayRange()
        return transactionDao.getTodayCollected(startOfDay, endOfDay)
    }

    fun getThisMonthTotal(): Flow<Double?> {
        val (startOfMonth, endOfMonth) = PkDateTime.pktMonthRange()
        return transactionDao.getThisMonthTotal(startOfMonth, endOfMonth)
    }

    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>> = transactionDao.getRecentTransactions(limit)

    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>> = transactionDao.getTransactionsByAccount(accountId)

    fun getTransactionsByAccountBetweenDates(accountId: Long, startDate: Long, endDate: Long): Flow<List<TransactionEntity>> = transactionDao.getTransactionsByAccountBetweenDates(accountId, startDate, endDate)

    fun getOverdueTransactions(): Flow<List<TransactionEntity>> = transactionDao.getOverdueTransactions(PkDateTime.nowUtc())

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: Long) =
        transactionDao.deleteTransactionById(id)

    // ═══ Edit Log Methods ═══

    fun getEditLogsForTransaction(transactionId: Long): Flow<List<TransactionEditLogEntity>> =
        editLogDao.getLogsForTransaction(transactionId)

    suspend fun getEditLogsForTransactionOnce(transactionId: Long): List<TransactionEditLogEntity> =
        editLogDao.getLogsForTransactionOnce(transactionId)

    suspend fun insertEditLog(log: TransactionEditLogEntity): Long =
        editLogDao.insertLog(log)

    /**
     * Update a transaction and log all field changes.
     * Compares old vs new values and creates individual log entries for each changed field.
     */
    suspend fun updateTransactionWithLog(
        oldTransaction: TransactionEntity,
        newTransaction: TransactionEntity,
        changedBy: String = "User",
        notes: String? = null
    ) {
        val nowUtc = PkDateTime.nowUtc()

        // Log each changed field
        if (oldTransaction.amount != newTransaction.amount) {
            editLogDao.insertLog(
                TransactionEditLogEntity(
                    transactionId = oldTransaction.id,
                    action = EditAction.EDITED,
                    fieldName = "amount",
                    oldValue = oldTransaction.amount.toString(),
                    newValue = newTransaction.amount.toString(),
                    changedBy = changedBy,
                    timestamp = nowUtc,
                    notes = notes
                )
            )
        }

        if (oldTransaction.type != newTransaction.type) {
            editLogDao.insertLog(
                TransactionEditLogEntity(
                    transactionId = oldTransaction.id,
                    action = EditAction.EDITED,
                    fieldName = "type",
                    oldValue = oldTransaction.type.name,
                    newValue = newTransaction.type.name,
                    changedBy = changedBy,
                    timestamp = nowUtc,
                    notes = notes
                )
            )
        }

        if (oldTransaction.description != newTransaction.description) {
            editLogDao.insertLog(
                TransactionEditLogEntity(
                    transactionId = oldTransaction.id,
                    action = EditAction.EDITED,
                    fieldName = "description",
                    oldValue = oldTransaction.description,
                    newValue = newTransaction.description,
                    changedBy = changedBy,
                    timestamp = nowUtc,
                    notes = notes
                )
            )
        }

        if (oldTransaction.date != newTransaction.date) {
            editLogDao.insertLog(
                TransactionEditLogEntity(
                    transactionId = oldTransaction.id,
                    action = EditAction.EDITED,
                    fieldName = "date",
                    oldValue = oldTransaction.date.toString(),
                    newValue = newTransaction.date.toString(),
                    changedBy = changedBy,
                    timestamp = nowUtc,
                    notes = notes
                )
            )
        }

        if (oldTransaction.dueDate != newTransaction.dueDate) {
            editLogDao.insertLog(
                TransactionEditLogEntity(
                    transactionId = oldTransaction.id,
                    action = EditAction.EDITED,
                    fieldName = "dueDate",
                    oldValue = oldTransaction.dueDate?.toString(),
                    newValue = newTransaction.dueDate?.toString(),
                    changedBy = changedBy,
                    timestamp = nowUtc,
                    notes = notes
                )
            )
        }

        transactionDao.updateTransaction(newTransaction)
    }

    /**
     * Delete a transaction and log the deletion.
     */
    suspend fun deleteTransactionWithLog(
        transaction: TransactionEntity,
        changedBy: String = "User"
    ) {
        val nowUtc = PkDateTime.nowUtc()
        editLogDao.insertLog(
            TransactionEditLogEntity(
                transactionId = transaction.id,
                action = EditAction.DELETED,
                fieldName = null,
                oldValue = "Amount: ${transaction.amount}, Type: ${transaction.type.name}",
                newValue = null,
                changedBy = changedBy,
                timestamp = nowUtc
            )
        )
        transactionDao.deleteTransaction(transaction)
    }
}
