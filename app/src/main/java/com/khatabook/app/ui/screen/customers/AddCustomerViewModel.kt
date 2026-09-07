package com.khatabook.app.ui.screen.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.app.data.entity.CustomerEntity
import com.khatabook.app.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddCustomerUiState(
    val name: String = "",
    val phone: String = "",
    val whatsappNumber: String = "",
    val sameAsPhone: Boolean = true,  // WhatsApp same as phone by default
    val photoUri: String? = null,
    val address: String = "",
    val notes: String = "",
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddCustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCustomerUiState())
    val uiState: StateFlow<AddCustomerUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    fun onWhatsappNumberChange(number: String) {
        _uiState.update { it.copy(whatsappNumber = number) }
    }

    fun onSameAsPhoneChange(same: Boolean) {
        _uiState.update { it.copy(sameAsPhone = same) }
    }

    fun onPhotoUriChange(uri: String?) {
        _uiState.update { it.copy(photoUri = uri) }
    }

    fun onAddressChange(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun saveCustomer() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }
        if (state.phone.isBlank()) {
            _uiState.update { it.copy(error = "Phone number is required") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val whatsapp = if (state.sameAsPhone) null else state.whatsappNumber.trim().ifBlank { null }
                val customer = CustomerEntity(
                    name = state.name.trim(),
                    phone = state.phone.trim(),
                    whatsappNumber = whatsapp,
                    photoUri = state.photoUri,
                    address = state.address.trim().ifBlank { null },
                    notes = state.notes.trim().ifBlank { null }
                )
                customerRepository.insertCustomer(customer)
                _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
