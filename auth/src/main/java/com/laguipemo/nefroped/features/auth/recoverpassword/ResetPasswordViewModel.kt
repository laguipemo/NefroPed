package com.laguipemo.nefroped.features.auth.recoverpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.result.NefroResult
import com.laguipemo.nefroped.core.domain.model.util.ValidationError
import com.laguipemo.nefroped.core.domain.usecase.recoverpassword.UpdatePasswordUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val updatePasswordUseCase: UpdatePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffects = MutableSharedFlow<ResetPasswordUiEffect>()
    val uiEffects = _uiEffects.asSharedFlow()

    fun onEvent(event: ResetPasswordUserEvent) {
        when (event) {
            is ResetPasswordUserEvent.PasswordChanged -> {
                _uiState.update {
                    it.copy(
                        password = event.value,
                        passwordError = validatePassword(event.value)
                    )
                }
            }

            is ResetPasswordUserEvent.ConfirmPasswordChanged -> {
                _uiState.update {
                    it.copy(
                        confirmPassword = event.value,
                        confirmPasswordError = validateConfirmPassword(it.password, event.value)
                    )
                }
            }

            ResetPasswordUserEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        val passwordError = validatePassword(state.password)
        val confirmPasswordError = validateConfirmPassword(state.password, state.confirmPassword)

        if (passwordError != null || confirmPasswordError != null) {
            _uiState.update {
                it.copy(
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = updatePasswordUseCase(state.password)) {
                is NefroResult.Success -> _uiEffects.emit(ResetPasswordUiEffect.ResetPasswordSuccess)
                is NefroResult.Error -> {
                    _uiEffects.emit(ResetPasswordUiEffect.ShowError(result.error))
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun validatePassword(password: String): ValidationError? =
        if (password.length < 6) ValidationError.InvalidPasswordFormat else null

    private fun validateConfirmPassword(password: String, confirm: String): ValidationError? =
        if (password != confirm) ValidationError.PasswordsDoNotMatch else null
}