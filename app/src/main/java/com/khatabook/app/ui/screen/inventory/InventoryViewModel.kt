package com.khatabook.app.ui.screen.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.app.data.entity.ProductEntity
import com.khatabook.app.data.entity.ProductUnit
import com.khatabook.app.data.repository.ProductRepository
import com.khatabook.app.notification.LowStockTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryUiState(
    val products: List<ProductEntity> = emptyList(),
    val searchQuery: String = "",
    val totalStockValue: Double = 0.0,
    val totalPotentialRevenue: Double = 0.0,
    val lowStockCount: Int = 0,
    val isLoading: Boolean = true
)

data class AddProductUiState(
    val name: String = "",
    val barcode: String = "",
    val unit: ProductUnit = ProductUnit.PIECE,
    val salePrice: String = "",
    val purchasePrice: String = "",
    val stockQuantity: String = "",
    val lowStockThreshold: String = "5",
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val error: String? = null,
    // Duplicate detection
    val existingProduct: ProductEntity? = null,
    val isDuplicateCheck: Boolean = false,
    val showDuplicateDialog: Boolean = false
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val lowStockTracker: LowStockTracker
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<ProductEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                productRepository.getAllActiveProducts()
            } else {
                productRepository.searchProducts(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                products,
                productRepository.getTotalStockValue(),
                productRepository.getTotalPotentialRevenue(),
                productRepository.getLowStockProducts()
            ) { productList, stockValue, revenue, lowStock ->
                InventoryUiState(
                    products = productList,
                    totalStockValue = stockValue ?: 0.0,
                    totalPotentialRevenue = revenue ?: 0.0,
                    lowStockCount = lowStock.size,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
        }
    }

    /**
     * Check and trigger low stock notification for a specific product.
     * Called after stock deduction or addition.
     */
    fun checkLowStock(product: ProductEntity) {
        lowStockTracker.checkProductStock(product)
    }

    /**
     * Observe low stock count for badge display.
     */
    val lowStockCount: StateFlow<Int> = lowStockTracker.lowStockCount
}

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddProductUiState())
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()

    init {
        // Receive barcode from barcode scanner via SavedStateHandle
        savedStateHandle.get<String>("scanned_barcode")?.let { barcode ->
            if (barcode.isNotBlank()) {
                onBarcodeScanned(barcode)
            }
        }
    }

    fun onNameChange(name: String) { _uiState.update { it.copy(name = name) } }
    fun onBarcodeChange(barcode: String) { _uiState.update { it.copy(barcode = barcode) } }
    fun onUnitChange(unit: ProductUnit) { _uiState.update { it.copy(unit = unit) } }
    fun onSalePriceChange(price: String) { _uiState.update { it.copy(salePrice = price) } }
    fun onPurchasePriceChange(price: String) { _uiState.update { it.copy(purchasePrice = price) } }
    fun onStockQuantityChange(qty: String) { _uiState.update { it.copy(stockQuantity = qty) } }
    fun onLowStockThresholdChange(threshold: String) { _uiState.update { it.copy(lowStockThreshold = threshold) } }

    /**
     * Called when barcode is scanned from camera.
     * Checks if product already exists with this barcode.
     */
    fun onBarcodeScanned(barcode: String) {
        _uiState.update { it.copy(barcode = barcode, isDuplicateCheck = true) }

        viewModelScope.launch {
            val existing = productRepository.getProductByBarcode(barcode)
            if (existing != null) {
                // Product exists — show duplicate dialog with quick quantity update
                _uiState.update {
                    it.copy(
                        existingProduct = existing,
                        showDuplicateDialog = true,
                        isDuplicateCheck = false,
                        // Pre-fill name from existing product
                        name = existing.name
                    )
                }
            } else {
                // New barcode — clear duplicate state
                _uiState.update {
                    it.copy(
                        existingProduct = null,
                        showDuplicateDialog = false,
                        isDuplicateCheck = false
                    )
                }
            }
        }
    }

    /**
     * User chooses to update stock of existing product instead of creating new.
     */
    fun onDuplicateConfirmUpdate(additionalQuantity: Double) {
        val existing = _uiState.value.existingProduct ?: return
        _uiState.update { it.copy(isSaving = true, showDuplicateDialog = false) }

        viewModelScope.launch {
            try {
                productRepository.addStock(existing.id, additionalQuantity)
                _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    /**
     * User chooses to create new product despite barcode match.
     */
    fun onDuplicateDismiss() {
        _uiState.update {
            it.copy(
                existingProduct = null,
                showDuplicateDialog = false
            )
        }
    }

    fun saveProduct() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Product name is required") }
            return
        }
        val salePrice = state.salePrice.toDoubleOrNull()
        if (salePrice == null || salePrice <= 0) {
            _uiState.update { it.copy(error = "Valid sale price is required") }
            return
        }
        val purchasePrice = state.purchasePrice.toDoubleOrNull() ?: 0.0
        val stockQty = state.stockQuantity.toDoubleOrNull() ?: 0.0
        val threshold = state.lowStockThreshold.toDoubleOrNull() ?: 5.0

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val product = ProductEntity(
                    name = state.name.trim(),
                    barcode = state.barcode.ifBlank { null },
                    unit = state.unit,
                    salePrice = salePrice,
                    purchasePrice = purchasePrice,
                    stockQuantity = stockQty,
                    lowStockThreshold = threshold
                )
                productRepository.insertProduct(product)
                _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
