package com.laguipemo.nefroped.features.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.result.NefroResult
import com.laguipemo.nefroped.core.domain.model.util.ValidationError
import com.laguipemo.nefroped.core.domain.usecase.register.RegisterUseCase
import com.laguipemo.nefroped.features.auth.util.ValidationConstants.MINIMAL_PASS_LENGTH
import com.laguipemo.nefroped.features.auth.util.ValidationConstants.isValidEmail
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffects = MutableSharedFlow<RegisterUiEffect>()
    val uiEffects = _uiEffects.asSharedFlow()

    fun onEvent(event: RegisterUserEvent) {
        when (event) {
            is RegisterUserEvent.EmailChanged -> {
                _uiState.update {
                    it.copy(
                        email = event.value,
                        emailError = validateEmail(event.value)
                    )
                }
            }

            is RegisterUserEvent.PasswordChanged -> {
                _uiState.update {
                    it.copy(
                        password = event.value,
                        passwordError = validatePassword(event.value)
                    )
                }
            }

            is RegisterUserEvent.ConfirmPasswordChanged -> {
                _uiState.update {
                    it.copy(
                        confirmPassword = event.value,
                        confirmPasswordError = validateConfirmPassword(
                            it.password,
                            event.value
                        )
                    )
                }
            }

            RegisterUserEvent.Submit -> submit()

        }
    }

    private fun submit() {
        val state = _uiState.value

        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password)
        val confirmPasswordError = validateConfirmPassword(
            state.password, state.confirmPassword
        )
        if (emailError != null || passwordError != null || confirmPasswordError != null ) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            when (val result = registerUseCase(state.email, state.password)) {
                is NefroResult.Success -> emitEffect(RegisterUiEffect.RegisterSuccess)
                is NefroResult.Error -> emitError(result.error)
            }

            _uiState.update {
                it.copy(isLoading = false)
            }
        }
    }

    private fun emitError(error: AuthError) {
        viewModelScope.launch {
            _uiEffects.emit(RegisterUiEffect.ShowError(error))
        }
    }

    private fun emitEffect(effect: RegisterUiEffect) {
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

    private fun validatePassword(password: String): ValidationError? =
        when {
            password.isBlank() -> ValidationError.EmptyPassword
            password.length < MINIMAL_PASS_LENGTH ->
                ValidationError.PasswordTooShort(MINIMAL_PASS_LENGTH)

            else -> null
        }

    private fun validateConfirmPassword(
        password: String, confirmPassword: String
    ): ValidationError? =
        when {
            confirmPassword.isBlank() -> ValidationError.EmptyPassword
            password != confirmPassword -> ValidationError.PasswordsDoNotMatch
            else -> null
        }
}
