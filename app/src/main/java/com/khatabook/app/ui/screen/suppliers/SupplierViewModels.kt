package com.khatabook.app.ui.screen.suppliers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.app.data.entity.SupplierEntity
import com.khatabook.app.data.entity.SupplierTransactionEntity
import com.khatabook.app.data.entity.SupplierTransactionType
import com.khatabook.app.data.repository.SupplierRepository
import com.khatabook.app.data.repository.SupplierTransactionRepository
import com.khatabook.app.util.PkDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══ Supplier List ═══
@HiltViewModel
class SupplierListViewModel @Inject constructor(
    private val supplierRepository: SupplierRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val suppliers: StateFlow<List<SupplierEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) supplierRepository.getAllSuppliers()
            else supplierRepository.searchSuppliers(query)
        }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val totalPayables: StateFlow<Double?> = supplierRepository.getTotalPayables()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }

    fun deleteSupplier(supplier: SupplierEntity) {
        viewModelScope.launch { supplierRepository.deleteSupplier(supplier) }
    }
}

// ═══ Supplier Detail ═══
data class SupplierDetailUiState(
    val supplier: SupplierEntity? = null,
    val transactions: List<SupplierTransactionEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SupplierDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val supplierRepository: SupplierRepository,
    private val supplierTransactionRepository: SupplierTransactionRepository
) : ViewModel() {

    private val supplierId: Long = savedStateHandle.get<Long>("supplierId") ?: -1L
    private val _uiState = MutableStateFlow(SupplierDetailUiState())
    val uiState: StateFlow<SupplierDetailUiState> = _uiState.asStateFlow()

    init {
        if (supplierId > 0) {
            viewModelScope.launch {
                supplierRepository.getSupplierById(supplierId).collect { supplier ->
                    _uiState.update { it.copy(supplier = supplier) }
                }
            }
            viewModelScope.launch {
                supplierTransactionRepository.getTransactionsForSupplier(supplierId).collect { txns ->
                    _uiState.update { it.copy(transactions = txns, isLoading = false) }
                }
            }
        }
    }

    fun addTransaction(type: SupplierTransactionType, amount: Double, description: String?) {
        val supplier = _uiState.value.supplier ?: return
        viewModelScope.launch {
            val txn = SupplierTransactionEntity(
                supplierId = supplier.id,
                supplierName = supplier.name,
                type = type,
                amount = amount,
                description = description
            )
            supplierTransactionRepository.insertTransaction(txn)

            val nowUtc = PkDateTime.nowUtc()
            val updated = when (type) {
                SupplierTransactionType.PURCHASE -> supplier.copy(
                    totalCreditTaken = supplier.totalCreditTaken + amount,
                    updatedAt = nowUtc
                )
                SupplierTransactionType.PAYMENT -> supplier.copy(
                    totalPaymentGiven = supplier.totalPaymentGiven + amount,
                    updatedAt = nowUtc
                )
            }
            supplierRepository.updateSupplier(updated)
        }
    }
}

// ═══ Add Supplier ═══
data class AddSupplierUiState(
    val name: String = "",
    val phone: String = "",
    val shopName: String = "",
    val address: String = "",
    val notes: String = "",
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddSupplierViewModel @Inject constructor(
    private val supplierRepository: SupplierRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddSupplierUiState())
    val uiState: StateFlow<AddSupplierUiState> = _uiState.asStateFlow()

    fun onNameChange(v: String) { _uiState.update { it.copy(name = v) } }
    fun onPhoneChange(v: String) { _uiState.update { it.copy(phone = v) } }
    fun onShopNameChange(v: String) { _uiState.update { it.copy(shopName = v) } }
    fun onAddressChange(v: String) { _uiState.update { it.copy(address = v) } }
    fun onNotesChange(v: String) { _uiState.update { it.copy(notes = v) } }

    fun saveSupplier() {
        val state = _uiState.value
        if (state.name.isBlank()) { _uiState.update { it.copy(error = "Name is required") }; return }
        if (state.phone.isBlank()) { _uiState.update { it.copy(error = "Phone is required") }; return }

        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                supplierRepository.insertSupplier(
                    SupplierEntity(
                        name = state.name.trim(),
                        phone = state.phone.trim(),
                        shopName = state.shopName.trim().ifBlank { null },
                        address = state.address.trim().ifBlank { null },
                        notes = state.notes.trim().ifBlank { null }
                    )
                )
                _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
