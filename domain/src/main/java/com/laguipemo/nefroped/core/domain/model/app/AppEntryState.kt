package com.laguipemo.nefroped.core.domain.model.app

interface AppEntryState {
    data object Loading: AppEntryState
    data object  RequireLogin: AppEntryState
    data object RequireOnboarding: AppEntryState
    data object Ready: AppEntryState
    data object Error: AppEntryState
}