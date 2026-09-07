package com.khatabook.app.data.dao

import androidx.room.*
import com.khatabook.app.data.entity.CashAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashAccountDao {

    @Query("SELECT * FROM cash_accounts WHERE isActive = 1 ORDER BY isDefault DESC, name ASC")
    fun getAllActiveAccounts(): Flow<List<CashAccountEntity>>

    @Query("SELECT * FROM cash_accounts WHERE isActive = 1 ORDER BY isDefault DESC, name ASC")
    suspend fun getAllActiveAccountsOnce(): List<CashAccountEntity>

    @Query("SELECT * FROM cash_accounts WHERE id = :id")
    fun getAccountById(id: Long): Flow<CashAccountEntity?>

    @Query("SELECT * FROM cash_accounts WHERE id = :id")
    suspend fun getAccountByIdOnce(id: Long): CashAccountEntity?

    @Query("SELECT * FROM cash_accounts WHERE isDefault = 1 AND isActive = 1 LIMIT 1")
    suspend fun getDefaultAccount(): CashAccountEntity?

    @Query("SELECT COUNT(*) FROM cash_accounts WHERE isActive = 1")
    suspend fun getActiveAccountCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: CashAccountEntity): Long

    @Update
    suspend fun updateAccount(account: CashAccountEntity)

    @Delete
    suspend fun deleteAccount(account: CashAccountEntity)

    @Query("DELETE FROM cash_accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)

    @Query("UPDATE cash_accounts SET balance = :balance, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateBalance(id: Long, balance: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE cash_accounts SET isDefault = 0 WHERE isDefault = 1")
    suspend fun clearDefaultAccount()

    @Query("UPDATE cash_accounts SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultAccount(id: Long)
}
