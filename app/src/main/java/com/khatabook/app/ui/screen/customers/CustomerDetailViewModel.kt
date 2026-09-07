package com.khatabook.app.ui.screen.customers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.app.data.entity.CustomerEntity
import com.khatabook.app.data.entity.TransactionEntity
import com.khatabook.app.data.repository.CustomerRepository
import com.khatabook.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerDetailUiState(
    val customer: CustomerEntity? = null,
    val transactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val customerRepository: CustomerRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val customerId: Long = savedStateHandle.get<Long>("customerId") ?: -1L

    private val _uiState = MutableStateFlow(CustomerDetailUiState())
    val uiState: StateFlow<CustomerDetailUiState> = _uiState.asStateFlow()

    init {
        if (customerId > 0) {
            loadCustomer()
            loadTransactions()
        }
    }

    private fun loadCustomer() {
        viewModelScope.launch {
            customerRepository.getCustomerById(customerId).collect { customer ->
                _uiState.update { it.copy(customer = customer) }
            }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            transactionRepository.getTransactionsForCustomer(customerId).collect { transactions ->
                _uiState.update {
                    it.copy(
                        transactions = transactions,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteCustomer() {
        viewModelScope.launch {
            customerRepository.deleteCustomerById(customerId)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    }
}
