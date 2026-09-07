package com.khatabook.app.data.repository

import com.khatabook.app.data.dao.SupplierDao
import com.khatabook.app.data.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupplierRepository @Inject constructor(
    private val supplierDao: SupplierDao
) {
    fun getAllSuppliers(): Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()

    fun getSupplierById(id: Long): Flow<SupplierEntity?> = supplierDao.getSupplierById(id)

    suspend fun getSupplierByIdOnce(id: Long): SupplierEntity? = supplierDao.getSupplierByIdOnce(id)

    fun searchSuppliers(query: String): Flow<List<SupplierEntity>> = supplierDao.searchSuppliers(query)

    fun getSuppliersWithPayables(): Flow<List<SupplierEntity>> = supplierDao.getSuppliersWithPayables()

    suspend fun insertSupplier(supplier: SupplierEntity): Long = supplierDao.insertSupplier(supplier)

    suspend fun updateSupplier(supplier: SupplierEntity) = supplierDao.updateSupplier(supplier)

    suspend fun deleteSupplier(supplier: SupplierEntity) = supplierDao.deleteSupplier(supplier)

    suspend fun deleteSupplierById(id: Long) = supplierDao.deleteSupplierById(id)

    fun getTotalPayables(): Flow<Double?> = supplierDao.getTotalPayables()

    fun getSupplierCount(): Flow<Int> = supplierDao.getSupplierCount()
}
