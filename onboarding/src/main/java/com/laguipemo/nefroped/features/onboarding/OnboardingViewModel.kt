package com.laguipemo.nefroped.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.laguipemo.nefroped.core.domain.usecase.onboarding.CompleteOnboardingUseCase
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    fun onOnboardingFinished() {
        viewModelScope.launch {
            completeOnboardingUseCase()
        }
    }
}