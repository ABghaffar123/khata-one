package com.khatabook.app.ui.screen.inventory

import androidx.lifecycle.ViewModel
import com.khatabook.app.data.entity.ProductEntity
import com.khatabook.app.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class LowStockViewModel @Inject constructor(
    productRepository: ProductRepository
) : ViewModel() {

    val lowStockProducts: Flow<List<ProductEntity>> = productRepository.getLowStockProducts()
}
