package com.aptopayments.sdk.utils

import kotlinx.coroutines.CoroutineDispatcher

internal class TestDispatchers(testDispatcher: CoroutineDispatcher) : CoroutineDispatcherProvider {
    override val default = testDispatcher
    override val main = testDispatcher
    override val io = testDispatcher
}
