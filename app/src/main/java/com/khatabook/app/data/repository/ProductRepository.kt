package com.khatabook.app.data.repository

import com.khatabook.app.data.dao.ProductDao
import com.khatabook.app.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {
    fun getAllActiveProducts(): Flow<List<ProductEntity>> = productDao.getAllActiveProducts()

    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    fun getProductById(id: Long): Flow<ProductEntity?> = productDao.getProductById(id)

    suspend fun getProductByIdOnce(id: Long): ProductEntity? = productDao.getProductByIdOnce(id)

    suspend fun getProductByBarcode(barcode: String): ProductEntity? = productDao.getProductByBarcode(barcode)

    fun getProductByBarcodeFlow(barcode: String): Flow<ProductEntity?> = productDao.getProductByBarcodeFlow(barcode)

    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

    fun getLowStockProducts(): Flow<List<ProductEntity>> = productDao.getLowStockProducts()

    fun getTotalStockValue(): Flow<Double?> = productDao.getTotalStockValue()

    fun getTotalPotentialRevenue(): Flow<Double?> = productDao.getTotalPotentialRevenue()

    suspend fun insertProduct(product: ProductEntity): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)

    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)

    suspend fun deleteProductById(id: Long) = productDao.deleteProductById(id)

    suspend fun deductStock(productId: Long, quantity: Double): Boolean =
        productDao.deductStock(productId, quantity) > 0

    suspend fun addStock(productId: Long, quantity: Double) = productDao.addStock(productId, quantity)
}
