package com.khatabook.app.ui.screen.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.app.data.entity.CustomerEntity
import com.khatabook.app.data.entity.TransactionEditLogEntity
import com.khatabook.app.data.entity.TransactionEntity
import com.khatabook.app.data.entity.TransactionType
import com.khatabook.app.data.repository.CustomerRepository
import com.khatabook.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditTransactionUiState(
    val transaction: TransactionEntity? = null,
    val originalTransaction: TransactionEntity? = null,
    val customer: CustomerEntity? = null,
    val amount: String = "",
    val description: String = "",
    val type: TransactionType = TransactionType.CREDIT_GIVEN,
    val date: Long = 0L,
    val dueDate: Long? = null,
    val editLogs: List<TransactionEditLogEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val deletedSuccessfully: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showHistory: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val transactionId: Long = savedStateHandle.get<Long>("transactionId") ?: -1L

    private val _uiState = MutableStateFlow(EditTransactionUiState())
    val uiState: StateFlow<EditTransactionUiState> = _uiState.asStateFlow()

    init {
        if (transactionId > 0) {
            loadTransaction()
            loadEditLogs()
        }
    }

    private fun loadTransaction() {
        viewModelScope.launch {
            transactionRepository.getTransactionsForCustomerOnce(-1L) // dummy
        }
        viewModelScope.launch {
            // Observe the transaction by ID
            transactionRepository.getEditLogsForTransaction(transactionId).collect { logs ->
                _uiState.update { it.copy(editLogs = logs) }
            }
        }
        viewModelScope.launch {
            // We need to find the transaction - get all and find by ID
            transactionRepository.getAllTransactions().collect { transactions ->
                val txn = transactions.find { it.id == transactionId }
                if (txn != null) {
                    val customer = findCustomer(txn.customerId)
                    _uiState.update {
                        it.copy(
                            transaction = txn,
                            originalTransaction = it.originalTransaction ?: txn,
                            customer = customer,
                            amount = txn.amount.toString(),
                            description = txn.description ?: "",
                            type = txn.type,
                            date = txn.date,
                            dueDate = txn.dueDate,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun loadEditLogs() {
        viewModelScope.launch {
            transactionRepository.getEditLogsForTransaction(transactionId).collect { logs ->
                _uiState.update { it.copy(editLogs = logs) }
            }
        }
    }

    private suspend fun findCustomer(customerId: Long): CustomerEntity? {
        return try {
            customerRepository.getCustomerByIdOnce(customerId)
        } catch (_: Exception) {
            null
        }
    }

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update { it.copy(type = type) }
    }

    fun onDateChange(date: Long) {
        _uiState.update { it.copy(date = date) }
    }

    fun onDueDateChange(dueDate: Long?) {
        _uiState.update { it.copy(dueDate = dueDate) }
    }

    fun saveChanges() {
        val state = _uiState.value
        val original = state.originalTransaction ?: return
        val amount = state.amount.toDoubleOrNull() ?: return

        if (amount <= 0) {
            _uiState.update { it.copy(error = "Amount must be greater than 0") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val updatedTransaction = original.copy(
                    amount = amount,
                    description = state.description.ifBlank { null },
                    type = state.type,
                    date = state.date,
                    dueDate = state.dueDate
                )

                transactionRepository.updateTransactionWithLog(
                    oldTransaction = original,
                    newTransaction = updatedTransaction,
                    changedBy = "User"
                )

                // Update customer totals if amount or type changed
                val customer = state.customer
                if (customer != null && (original.amount != amount || original.type != state.type)) {
                    recalculateCustomerTotals(customer)
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savedSuccessfully = true,
                        transaction = updatedTransaction,
                        originalTransaction = updatedTransaction
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    private suspend fun recalculateCustomerTotals(customer: CustomerEntity) {
        val transactions = transactionRepository.getTransactionsForCustomerOnce(customer.id)
        var credit = 0.0
        var payment = 0.0
        for (txn in transactions) {
            when (txn.type) {
                TransactionType.CREDIT_GIVEN, TransactionType.STOCK_PURCHASED -> credit += txn.amount
                TransactionType.PAYMENT_RECEIVED, TransactionType.CASH_SALE, TransactionType.EXPENSE -> payment += txn.amount
            }
        }
        customerRepository.updateCustomer(customer.copy(totalCredit = credit, totalPayment = payment))
    }

    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun deleteTransaction() {
        val state = _uiState.value
        val transaction = state.transaction ?: return

        _uiState.update { it.copy(showDeleteConfirmation = false, isSaving = true) }

        viewModelScope.launch {
            try {
                transactionRepository.deleteTransactionWithLog(transaction)

                // Recalculate customer totals
                val customer = state.customer
                if (customer != null) {
                    recalculateCustomerTotals(customer)
                }

                _uiState.update { it.copy(isSaving = false, deletedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun toggleHistory() {
        _uiState.update { it.copy(showHistory = !it.showHistory) }
    }
}
