package com.laguipemo.nefroped.features.auth.recoverpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.result.NefroResult
import com.laguipemo.nefroped.core.domain.model.util.ValidationError
import com.laguipemo.nefroped.core.domain.usecase.recoverpassword.RecoverPasswordUseCase
import com.laguipemo.nefroped.core.domain.util.ValidationConstants.isValidEmail
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecoverPasswordViewModel(
    private val recoverPasswordUseCase: RecoverPasswordUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(RecoverPasswordUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffects = MutableSharedFlow<RecoverPasswordUiEffect>()
    val uiEffects = _uiEffects.asSharedFlow()

    fun onEvent(event: RecoverPasswordUserEvent) {
        when (event) {
            is RecoverPasswordUserEvent.EmailChanged -> {
                _uiState.update {
                    it.copy(
                        email = event.value,
                        emailError = validateEmail(event.value)
                    )
                }
            }

            is RecoverPasswordUserEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value

        val emailError = validateEmail(state.email)
        if (emailError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            when (val result = recoverPasswordUseCase(state.email)) {
                is NefroResult.Success -> emitEffect(RecoverPasswordUiEffect.RecoverPasswordSuccess)
                is NefroResult.Error -> emitError(result.error)
            }

            _uiState.update {
                it.copy(isLoading = false)
            }
        }
    }

    private fun emitError(error: AuthError) {
        viewModelScope.launch {
            _uiEffects.emit(RecoverPasswordUiEffect.ShowError(error))
        }
    }

    private fun emitEffect(effect: RecoverPasswordUiEffect) {
        viewModelScope.launch {
            _uiEffects.emit(effect)
        }
    }


    private fun validateEmail(email: String): ValidationError? =
        when {
            email.isBlank() -> ValidationError.EmptyEmail
            !email.isValidEmail() -> ValidationError.InvalidEmailFormat
            else -> null
        }

}