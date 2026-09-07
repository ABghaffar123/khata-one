package com.khatabook.app.data.repository

import com.khatabook.app.data.dao.CustomerDao
import com.khatabook.app.data.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao
) {
    fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    fun getCustomerById(id: Long): Flow<CustomerEntity?> = customerDao.getCustomerById(id)

    suspend fun getCustomerByIdOnce(id: Long): CustomerEntity? = customerDao.getCustomerByIdOnce(id)

    fun searchCustomers(query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(query)

    fun getTopDebtors(): Flow<List<CustomerEntity>> = customerDao.getTopDebtors()

    suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)

    suspend fun deleteCustomerById(id: Long) = customerDao.deleteCustomerById(id)

    fun getTotalDues(): Flow<Double?> = customerDao.getTotalDues()

    fun getCustomerCount(): Flow<Int> = customerDao.getCustomerCount()
}
