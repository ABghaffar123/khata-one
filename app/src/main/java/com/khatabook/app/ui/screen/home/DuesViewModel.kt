package com.khatabook.app.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.app.data.entity.CustomerEntity
import com.khatabook.app.data.entity.TransactionEntity
import com.khatabook.app.data.entity.TransactionType
import com.khatabook.app.data.repository.CustomerRepository
import com.khatabook.app.data.repository.TransactionRepository
import com.khatabook.app.util.PkDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class DueCustomer(
    val customer: CustomerEntity,
    val dueAmount: Double,
    val dueDate: Long?,
    val daysOverdue: Int,
    val transactionCount: Int
)

data class DuesUiState(
    val dueCustomers: List<DueCustomer> = emptyList(),
    val totalOverdueCount: Int = 0,
    val totalOverdueAmount: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class DuesViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DuesUiState())
    val uiState: StateFlow<DuesUiState> = _uiState.asStateFlow()

    init {
        loadDueCustomers()
    }

    private fun loadDueCustomers() {
        // Observe all customers with dues
        viewModelScope.launch {
            customerRepository.getTopDebtors().collect { debtors ->
                // For each debtor, find their transactions with due dates
                val allTransactions = mutableListOf<TransactionEntity>()
                for (customer in debtors) {
                    val txns = transactionRepository.getTransactionsForCustomerOnce(customer.id)
                    allTransactions.addAll(txns)
                }

                val nowUtc = PkDateTime.nowUtc()
                val dueCustomerMap = mutableMapOf<Long, DueCustomer>()

                // Find transactions with due dates
                for (txn in allTransactions) {
                    if (txn.type == TransactionType.CREDIT_GIVEN && txn.dueDate != null) {
                        val daysOverdue = if (txn.dueDate <= nowUtc) {
                            ((nowUtc - txn.dueDate) / (1000 * 60 * 60 * 24)).toInt()
                        } else {
                            0
                        }

                        val existing = dueCustomerMap[txn.customerId]
                        if (existing != null) {
                            // Update with latest due info
                            val newDaysOverdue = maxOf(existing.daysOverdue, daysOverdue)
                            val earliestDue = minOf(existing.dueDate ?: Long.MAX_VALUE, txn.dueDate)
                            dueCustomerMap[txn.customerId] = existing.copy(
                                dueAmount = existing.dueAmount + txn.amount,
                                dueDate = earliestDue,
                                daysOverdue = newDaysOverdue,
                                transactionCount = existing.transactionCount + 1
                            )
                        } else {
                            val customer = debtors.find { it.id == txn.customerId }
                            if (customer != null) {
                                dueCustomerMap[txn.customerId] = DueCustomer(
                                    customer = customer,
                                    dueAmount = txn.amount,
                                    dueDate = txn.dueDate,
                                    daysOverdue = daysOverdue,
                                    transactionCount = 1
                                )
                            }
                        }
                    }
                }

                // Also add customers with overdue balance (no due date set but have dues)
                for (customer in debtors) {
                    if (!dueCustomerMap.containsKey(customer.id) && customer.hasDue) {
                        dueCustomerMap[customer.id] = DueCustomer(
                            customer = customer,
                            dueAmount = customer.balance,
                            dueDate = null,
                            daysOverdue = 0,
                            transactionCount = 0
                        )
                    }
                }

                // Sort: most overdue first, then by due date (earliest first)
                val sorted = dueCustomerMap.values.sortedWith(
                    compareByDescending<DueCustomer> { it.daysOverdue }
                        .thenBy { it.dueDate ?: Long.MAX_VALUE }
                )

                _uiState.update {
                    it.copy(
                        dueCustomers = sorted,
                        totalOverdueCount = sorted.size,
                        totalOverdueAmount = sorted.sumOf { d -> d.dueAmount },
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Get the count of customers with pending/overdue dues.
     * Use this for the badge on the Home screen.
     */
    fun getOverdueCount(): Int = _uiState.value.totalOverdueCount
}
