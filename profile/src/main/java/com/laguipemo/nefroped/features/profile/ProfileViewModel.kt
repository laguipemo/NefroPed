package com.laguipemo.nefroped.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.model.result.NefroResult
import com.laguipemo.nefroped.core.domain.model.session.SessionState
import com.laguipemo.nefroped.core.domain.model.util.ValidationError
import com.laguipemo.nefroped.core.domain.usecase.login.LinkEmailPasswordUseCase
import com.laguipemo.nefroped.core.domain.usecase.login.LoginWithGoogleUseCase
import com.laguipemo.nefroped.core.domain.usecase.logout.LogoutUseCase
import com.laguipemo.nefroped.core.domain.usecase.session.ObserveSessionStateUseCase
import com.laguipemo.nefroped.core.domain.util.ValidationConstants.MINIMAL_PASS_LENGTH
import com.laguipemo.nefroped.core.domain.util.ValidationConstants.isValidEmail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val observeSessionState: ObserveSessionStateUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase,
    private val linkEmailPasswordUseCase: LinkEmailPasswordUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _formEmail = MutableStateFlow("")
    private val _formPassword = MutableStateFlow("")
    private val _emailError = MutableStateFlow<ValidationError?>(null)
    private val _passwordError = MutableStateFlow<ValidationError?>(null)
    private val _showBottomSheet = MutableStateFlow(false)

    val uiState: StateFlow<ProfileUiState> =
        combine(
            observeSessionState(),
            _isLoading,
            _formEmail,
            _formPassword,
            _emailError,
            _passwordError,
            _showBottomSheet
        ) { args ->
            val sessionState = args[0] as SessionState
            val isLoading = args[1] as Boolean
            val formEmail = args[2] as String
            val formPassword = args[3] as String
            val emailError = args[4] as ValidationError?
            val passwordError = args[5] as ValidationError?
            val showBottomSheet = args[6] as Boolean

            when (sessionState) {
                SessionState.Initializing -> ProfileUiState.Loading
                is SessionState.User -> {
                    val name = sessionState.user.displayName ?: sessionState.user.email?.substringBefore("@") ?: "Usuario"
                    ProfileUiState.Content(
                        userDisplayName = name,
                        userEmail = sessionState.user.email ?: "",
                        avatarUrl = sessionState.user.avatarUrl,
                        isGuest = sessionState.isAnonymous,
                        isLoading = isLoading,
                        formEmail = formEmail,
                        formPassword = formPassword,
                        emailError = emailError,
                        passwordError = passwordError,
                        showBottomSheet = showBottomSheet
                    )
                }

                else -> ProfileUiState.Loading
            }
        }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                ProfileUiState.Loading
            )

    fun onLogoutClicked() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    fun onEmailChanged(value: String) {
        _formEmail.update { value }
        _emailError.update { validateEmail(value) }
    }

    fun onPasswordChanged(value: String) {
        _formPassword.update { value }
        _passwordError.update { validatePassword(value) }
    }

    fun onShowBottomSheet(show: Boolean) {
        _showBottomSheet.update { show }
        if (!show) {
            // Reset form when closing
            _formEmail.update { "" }
            _formPassword.update { "" }
            _emailError.update { null }
            _passwordError.update { null }
        }
    }

    fun onLinkWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.update { true }
            when (val result = loginWithGoogleUseCase(idToken)) {
                is NefroResult.Success -> {
                    onShowBottomSheet(false)
                }

                is NefroResult.Error -> {
                    // Handle error
                }
            }
            _isLoading.update { false }
        }
    }

    fun onLinkWithEmailPassword() {
        val email = _formEmail.value
        val password = _formPassword.value

        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)

        if (emailError != null || passwordError != null) {
            _emailError.update { emailError }
            _passwordError.update { passwordError }
            return
        }

        viewModelScope.launch {
            _isLoading.update { true }
            when (val result = linkEmailPasswordUseCase(email, password)) {
                is NefroResult.Success -> {
                    onShowBottomSheet(false)
                }

                is NefroResult.Error -> {
                    // Handle error
                }
            }
            _isLoading.update { false }
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