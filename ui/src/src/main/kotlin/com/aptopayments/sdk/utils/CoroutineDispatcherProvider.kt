package com.aptopayments.sdk.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal interface CoroutineDispatcherProvider {
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
}

internal class ProductionDispatchers : CoroutineDispatcherProvider {
    override val default = Dispatchers.Default
    override val main = Dispatchers.Main
    override val io = Dispatchers.IO
}
