package com.khatabook.app.ui.screen.invoice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.app.data.entity.InvoiceItemEntity
import com.khatabook.app.data.entity.TransactionEntity
import com.khatabook.app.data.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvoicePreviewState(
    val transaction: TransactionEntity? = null,
    val items: List<InvoiceItemEntity> = emptyList(),
    val shopName: String = "Khata One",
    val invoiceText: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class InvoicePreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val transactionId: Long = savedStateHandle.get<Long>("transactionId") ?: -1L

    private val _uiState = MutableStateFlow(InvoicePreviewState())
    val uiState: StateFlow<InvoicePreviewState> = _uiState.asStateFlow()

    init {
        if (transactionId > 0) {
            loadInvoice()
        } else {
            _uiState.update { it.copy(isLoading = false, error = "Invalid transaction") }
        }
    }

    private fun loadInvoice() {
        viewModelScope.launch {
            val shopName = invoiceRepository.getShopName()
            _uiState.update { it.copy(shopName = shopName) }

            // Collect transaction
            invoiceRepository.getTransactionById(transactionId).collect { transaction ->
                if (transaction == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Transaction not found") }
                    return@collect
                }

                // Load items for this transaction
                val items = invoiceRepository.getInvoiceItemsOnce(transactionId)

                // Build invoice text
                val invoiceText = invoiceRepository.buildInvoiceText(transaction, items, shopName)

                _uiState.update {
                    it.copy(
                        transaction = transaction,
                        items = items,
                        shopName = shopName,
                        invoiceText = invoiceText,
                        isLoading = false
                    )
                }
            }
        }
    }

    /** Regenerate invoice text (e.g., after shop name change) */
    fun refreshInvoiceText() {
        val state = _uiState.value
        val transaction = state.transaction ?: return
        val text = invoiceRepository.buildInvoiceText(transaction, state.items, state.shopName)
        _uiState.update { it.copy(invoiceText = text) }
    }
}
