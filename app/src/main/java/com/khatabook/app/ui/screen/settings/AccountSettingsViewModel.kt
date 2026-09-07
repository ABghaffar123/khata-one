package com.khatabook.app.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.app.data.entity.CashAccountEntity
import com.khatabook.app.data.entity.CashAccountType
import com.khatabook.app.data.repository.CashAccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountSettingsUiState(
    val accounts: List<CashAccountEntity> = emptyList(),
    val isAdding: Boolean = false,
    val editingAccount: CashAccountEntity? = null,
    val newName: String = "",
    val newType: CashAccountType = CashAccountType.CASH,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val cashAccountRepository: CashAccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountSettingsUiState())
    val uiState: StateFlow<AccountSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            cashAccountRepository.getAllActiveAccounts().collect { accounts ->
                _uiState.update { it.copy(accounts = accounts) }
            }
        }

        // Seed default account if none exists
        viewModelScope.launch {
            val count = cashAccountRepository.getAllActiveAccountsOnce().size
            if (count == 0) {
                cashAccountRepository.insertAccount(
                    CashAccountEntity(
                        name = "Counter Cash",
                        type = CashAccountType.CASH,
                        isDefault = true
                    )
                )
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(newName = name, error = null) }
    }

    fun onTypeChange(type: CashAccountType) {
        _uiState.update { it.copy(newType = type) }
    }

    fun startAddAccount() {
        _uiState.update {
            it.copy(
                isAdding = true,
                editingAccount = null,
                newName = "",
                newType = CashAccountType.CASH,
                error = null
            )
        }
    }

    fun startEditAccount(account: CashAccountEntity) {
        _uiState.update {
            it.copy(
                isAdding = true,
                editingAccount = account,
                newName = account.name,
                newType = account.type,
                error = null
            )
        }
    }

    fun dismissAddEdit() {
        _uiState.update {
            it.copy(isAdding = false, editingAccount = null, newName = "", error = null)
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(success = false) }
    }

    fun saveAccount() {
        val state = _uiState.value
        if (state.newName.isBlank()) {
            _uiState.update { it.copy(error = "Account name is required") }
            return
        }

        viewModelScope.launch {
            try {
                if (state.editingAccount != null) {
                    // Update existing
                    cashAccountRepository.updateAccount(
                        state.editingAccount.copy(
                            name = state.newName.trim(),
                            type = state.newType,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    // Create new
                    cashAccountRepository.insertAccount(
                        CashAccountEntity(
                            name = state.newName.trim(),
                            type = state.newType
                        )
                    )
                }
                _uiState.update {
                    it.copy(isAdding = false, editingAccount = null, newName = "", success = true)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteAccount(account: CashAccountEntity) {
        viewModelScope.launch {
            cashAccountRepository.deleteAccount(account)
        }
    }

    fun setDefault(account: CashAccountEntity) {
        viewModelScope.launch {
            cashAccountRepository.setDefaultAccount(account.id)
        }
    }
}
