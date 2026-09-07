package com.khatabook.app.data.repository

import com.khatabook.app.data.dao.CashAccountDao
import com.khatabook.app.data.entity.CashAccountEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CashAccountRepository @Inject constructor(
    private val cashAccountDao: CashAccountDao
) {
    fun getAllActiveAccounts(): Flow<List<CashAccountEntity>> = cashAccountDao.getAllActiveAccounts()

    suspend fun getAllActiveAccountsOnce(): List<CashAccountEntity> = cashAccountDao.getAllActiveAccountsOnce()

    fun getAccountById(id: Long): Flow<CashAccountEntity?> = cashAccountDao.getAccountById(id)

    suspend fun getAccountByIdOnce(id: Long): CashAccountEntity? = cashAccountDao.getAccountByIdOnce(id)

    suspend fun getDefaultAccount(): CashAccountEntity? = cashAccountDao.getDefaultAccount()

    suspend fun insertAccount(account: CashAccountEntity): Long = cashAccountDao.insertAccount(account)

    suspend fun updateAccount(account: CashAccountEntity) = cashAccountDao.updateAccount(account)

    suspend fun deleteAccount(account: CashAccountEntity) = cashAccountDao.deleteAccount(account)

    suspend fun deleteAccountById(id: Long) = cashAccountDao.deleteAccountById(id)

    suspend fun updateBalance(id: Long, balance: Double) = cashAccountDao.updateBalance(id, balance)

    suspend fun setDefaultAccount(id: Long) {
        cashAccountDao.clearDefaultAccount()
        cashAccountDao.setDefaultAccount(id)
    }
}
