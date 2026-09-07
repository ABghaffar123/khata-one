package com.khatabook.app.ui.screen.transactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.app.data.entity.CashAccountEntity
import com.khatabook.app.data.entity.CustomerEntity
import com.khatabook.app.data.entity.SupplierEntity
import com.khatabook.app.data.entity.TransactionEntity
import com.khatabook.app.data.entity.TransactionType
import com.khatabook.app.data.repository.CashAccountRepository
import com.khatabook.app.data.repository.CustomerRepository
import com.khatabook.app.data.repository.SupplierRepository
import com.khatabook.app.data.repository.TransactionRepository
import com.khatabook.app.util.PkDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTransactionUiState(
    val customers: List<CustomerEntity> = emptyList(),
    val selectedCustomer: CustomerEntity? = null,
    val suppliers: List<SupplierEntity> = emptyList(),
    val selectedSupplier: SupplierEntity? = null,
    val accounts: List<CashAccountEntity> = emptyList(),
    val selectedAccount: CashAccountEntity? = null,
    val amount: String = "",
    val description: String = "",
    val type: TransactionType = TransactionType.CREDIT_GIVEN,
    val dueDate: Long? = null,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val savedTransaction: TransactionEntity? = null,
    val error: String? = null
) {
    /** Whether customer selector should be shown */
    val showCustomerField: Boolean get() = type.customerRequired || type.customerOptional

    /** Whether supplier selector should be shown */
    val showSupplierField: Boolean get() = type.supplierOptional

    /** Whether no party field should be shown */
    val showNoPartyField: Boolean get() = type.noPartyField

    /** Whether due date field should be shown */
    val showDueDateField: Boolean get() = type.showsDueDate

    /** Live preview text for the summary card */
    val previewText: String
        get() {
            val amt = amount.toDoubleOrNull() ?: 0.0
            val formatted = "Rs ${String.format("%,.0f", amt)}"
            return when (type) {
                TransactionType.CREDIT_GIVEN -> "$formatted will be recorded as Credit Given"
                TransactionType.PAYMENT_RECEIVED -> "$formatted will be recorded as Payment Received"
                TransactionType.CASH_SALE -> "$formatted will be recorded as Cash Sale"
                TransactionType.SALE_RETURN -> "$formatted will be recorded as Sale Return"
                TransactionType.STOCK_PURCHASED -> "$formatted will be recorded as Stock Purchased"
                TransactionType.PURCHASE_RETURN -> "$formatted will be recorded as Purchase Return"
                TransactionType.EXPENSE -> "$formatted will be recorded as Expense"
                TransactionType.OWNER_WITHDRAWAL -> "$formatted will be recorded as Owner Withdrawal"
                TransactionType.OWNER_INVESTMENT -> "$formatted will be recorded as Owner Investment"
            }
        }

    /** Dynamic save button text */
    val saveButtonText: String
        get() = when (type) {
            TransactionType.CREDIT_GIVEN -> "Save Credit Given"
            TransactionType.PAYMENT_RECEIVED -> "Save Payment Received"
            TransactionType.CASH_SALE -> "Save Cash Sale"
            TransactionType.SALE_RETURN -> "Save Sale Return"
            TransactionType.STOCK_PURCHASED -> "Save Stock Purchased"
            TransactionType.PURCHASE_RETURN -> "Save Purchase Return"
            TransactionType.EXPENSE -> "Save Expense"
            TransactionType.OWNER_WITHDRAWAL -> "Save Owner Withdrawal"
            TransactionType.OWNER_INVESTMENT -> "Save Owner Investment"
        }
}

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val customerRepository: CustomerRepository,
    private val transactionRepository: TransactionRepository,
    private val cashAccountRepository: CashAccountRepository,
    private val supplierRepository: SupplierRepository
) : ViewModel() {

    private val preSelectedCustomerId: Long = savedStateHandle.get<Long>("customerId") ?: -1L

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        // Load customers
        viewModelScope.launch {
            customerRepository.getAllCustomers().collect { customers ->
                val preSelected = if (preSelectedCustomerId > 0) {
                    customers.find { it.id == preSelectedCustomerId }
                } else null
                _uiState.update {
                    it.copy(
                        customers = customers,
                        selectedCustomer = preSelected ?: it.selectedCustomer
                    )
                }
            }
        }

        // Load suppliers
        viewModelScope.launch {
            supplierRepository.getAllSuppliers().collect { suppliers ->
                _uiState.update { it.copy(suppliers = suppliers) }
            }
        }

        // Load accounts and select default
        viewModelScope.launch {
            cashAccountRepository.getAllActiveAccounts().collect { accounts ->
                val defaultAccount = accounts.find { it.isDefault } ?: accounts.firstOrNull()
                _uiState.update {
                    it.copy(
                        accounts = accounts,
                        selectedAccount = it.selectedAccount ?: defaultAccount
                    )
                }
            }
        }
    }

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update {
            it.copy(
                type = type,
                // Clear party selection when switching to no-party types
                selectedCustomer = if (type.noPartyField || type.supplierOptional) null else it.selectedCustomer,
                selectedSupplier = if (!type.supplierOptional) null else it.selectedSupplier,
                // Clear due date when switching away from Credit Given
                dueDate = if (type.showsDueDate) it.dueDate else null
            )
        }
    }

    fun onCustomerSelected(customer: CustomerEntity) {
        _uiState.update { it.copy(selectedCustomer = customer) }
    }

    fun onSupplierSelected(supplier: SupplierEntity) {
        _uiState.update { it.copy(selectedSupplier = supplier) }
    }

    fun onAccountSelected(account: CashAccountEntity) {
        _uiState.update { it.copy(selectedAccount = account) }
    }

    fun onDueDateSelected(dueDate: Long?) {
        _uiState.update { it.copy(dueDate = dueDate) }
    }

    fun saveTransaction() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull() ?: return

        // Validation
        if (amount <= 0) {
            _uiState.update { it.copy(error = "Amount must be greater than 0") }
            return
        }

        // Customer required validation
        if (state.type.customerRequired && state.selectedCustomer == null) {
            _uiState.update { it.copy(error = "Please select a customer") }
            return
        }

        // Due date required for Credit Given
        if (state.type.showsDueDate && state.dueDate == null) {
            _uiState.update { it.copy(error = "Due date is required for Credit Given") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val customer = state.selectedCustomer
                val supplier = state.selectedSupplier

                val transaction = TransactionEntity(
                    customerId = customer?.id ?: 0L,
                    customerName = customer?.name ?: "",
                    customerPhone = customer?.phone ?: "",
                    type = state.type,
                    amount = amount,
                    description = state.description.ifBlank { null },
                    dueDate = state.dueDate,
                    accountId = state.selectedAccount?.id,
                    supplierId = supplier?.id,
                    supplierName = supplier?.name
                )
                transactionRepository.insertTransaction(transaction)

                // Update customer totals
                if (customer != null) {
                    val nowUtc = PkDateTime.nowUtc()
                    val updatedCustomer = when (state.type) {
                        TransactionType.CREDIT_GIVEN -> customer.copy(
                            totalCredit = customer.totalCredit + amount,
                            updatedAt = nowUtc
                        )
                        TransactionType.PAYMENT_RECEIVED -> customer.copy(
                            totalPayment = customer.totalPayment + amount,
                            updatedAt = nowUtc
                        )
                        TransactionType.CASH_SALE -> customer.copy(
                            totalPayment = customer.totalPayment + amount,
                            updatedAt = nowUtc
                        )
                        TransactionType.SALE_RETURN -> customer.copy(
                            totalCredit = customer.totalCredit - amount,
                            updatedAt = nowUtc
                        )
                        else -> customer
                    }
                    customerRepository.updateCustomer(updatedCustomer)
                }

                // Update supplier payable
                if (supplier != null && state.type.affectsSupplierPayable) {
                    val nowUtc = PkDateTime.nowUtc()
                    val updatedSupplier = when (state.type) {
                        TransactionType.STOCK_PURCHASED -> supplier.copy(
                            totalCreditTaken = supplier.totalCreditTaken + amount,
                            updatedAt = nowUtc
                        )
                        TransactionType.PURCHASE_RETURN -> supplier.copy(
                            totalCreditTaken = supplier.totalCreditTaken - amount,
                            updatedAt = nowUtc
                        )
                        else -> supplier
                    }
                    supplierRepository.updateSupplier(updatedSupplier)
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        savedSuccessfully = true,
                        savedTransaction = transaction
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
