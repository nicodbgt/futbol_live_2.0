package com.app.relatosfutbolerosnd_20.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.relatosfutbolerosnd_20.data.repository.LicenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LicenseUiState {
    data object Idle : LicenseUiState
    data object Validating : LicenseUiState
    data object Licensed : LicenseUiState
    data class Error(val message: String) : LicenseUiState
}

class LicenseViewModel(private val repository: LicenseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<LicenseUiState>(LicenseUiState.Idle)
    val uiState: StateFlow<LicenseUiState> = _uiState.asStateFlow()

    fun activateLicense(key: String, firstName: String, lastName: String) {
        if (key.isBlank() || firstName.isBlank() || lastName.isBlank()) {
            _uiState.value = LicenseUiState.Error("Todos los campos son obligatorios")
            return
        }

        _uiState.value = LicenseUiState.Validating
        viewModelScope.launch {
            repository.activateLicense(key, firstName, lastName)
                .onSuccess {
                    _uiState.value = LicenseUiState.Licensed
                }
                .onFailure { error ->
                    _uiState.value = LicenseUiState.Error(error.message ?: "Error desconocido")
                }
        }
    }
}