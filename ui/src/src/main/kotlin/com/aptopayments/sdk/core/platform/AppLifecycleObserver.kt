package com.aptopayments.sdk.core.platform

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.aptopayments.sdk.core.usecase.OnEnterBackgroundUseCase
import org.koin.core.KoinComponent
import org.koin.core.inject

internal class AppLifecycleObserver : LifecycleObserver, KoinComponent {

    private val onEnterBackgroundUseCase: OnEnterBackgroundUseCase by inject()

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        onEnterBackgroundUseCase()
    }
}
