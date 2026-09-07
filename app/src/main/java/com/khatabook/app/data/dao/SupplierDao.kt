package com.khatabook.app.data.dao

import androidx.room.*
import com.khatabook.app.data.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    fun getSupplierById(id: Long): Flow<SupplierEntity?>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierByIdOnce(id: Long): SupplierEntity?

    @Query("SELECT * FROM suppliers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchSuppliers(query: String): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE (totalCreditTaken - totalPaymentGiven) > 0 ORDER BY (totalCreditTaken - totalPaymentGiven) DESC")
    fun getSuppliersWithPayables(): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)

    @Query("DELETE FROM suppliers WHERE id = :id")
    suspend fun deleteSupplierById(id: Long)

    @Query("SELECT SUM(totalCreditTaken - totalPaymentGiven) FROM suppliers WHERE (totalCreditTaken - totalPaymentGiven) > 0")
    fun getTotalPayables(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM suppliers")
    fun getSupplierCount(): Flow<Int>
}
