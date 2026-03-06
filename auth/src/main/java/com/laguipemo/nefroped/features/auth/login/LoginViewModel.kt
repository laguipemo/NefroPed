package com.laguipemo.nefroped.features.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.auth.AuthError
import com.laguipemo.nefroped.core.domain.model.result.NefroResult
import com.laguipemo.nefroped.core.domain.model.util.ValidationError
import com.laguipemo.nefroped.core.domain.usecase.login.ContinueAsGuestUseCase
import com.laguipemo.nefroped.core.domain.usecase.login.LoginUseCase
import com.laguipemo.nefroped.features.auth.util.ValidationConstants.MINIMAL_PASS_LENGTH
import com.laguipemo.nefroped.features.auth.util.ValidationConstants.isValidEmail
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val continueAsGuestUseCase: ContinueAsGuestUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffects = MutableSharedFlow<LoginUiEffect>()
    val uiEffects = _uiEffects.asSharedFlow()

    fun onEvent(event: LoginUserEvent) {
        when (event) {
            is LoginUserEvent.EmailChanged -> {
                _uiState.update {
                    it.copy(
                        email = event.value,
                        emailError = validateEmail(event.value)
                    )
                }
            }

            is LoginUserEvent.PasswordChanged -> {
                _uiState.update {
                    it.copy(
                        password = event.value,
                        passwordError = validatePassword(event.value)
                    )
                }
            }

            LoginUserEvent.Submit -> submit()

            LoginUserEvent.ContinueAsGuest -> {
//                viewModelScope.launch {
//                    continueAsGuestUseCase()
//                }
                viewModelScope.launch {
                    _uiState.update {
                        it.copy(isLoading = true)
                    }
                    when (val result = continueAsGuestUseCase()) {
                        is NefroResult.Success -> emitEffect(LoginUiEffect.LoginSuccess)

                        is NefroResult.Error ->
                            emitError(result.error)

                    }
                    _uiState.update { it.copy(isLoading = false) }
                }
            }

        }
    }

    private fun submit() {
        val state = _uiState.value

        val emailError = validateEmail(state.email)
        val passwordError = validatePassword(state.password)

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }
            when (val result = loginUseCase(state.email, state.password)) {
                is NefroResult.Success -> emitEffect(LoginUiEffect.LoginSuccess)

                is NefroResult.Error ->
                    emitError(result.error)

            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun emitError(error: AuthError) {
        viewModelScope.launch {
            _uiEffects.emit(LoginUiEffect.ShowError(error))
        }
    }

    private fun emitEffect(effect: LoginUiEffect) {
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
}