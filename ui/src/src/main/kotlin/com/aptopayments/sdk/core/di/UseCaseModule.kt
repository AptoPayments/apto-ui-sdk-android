package com.aptopayments.sdk.core.di

import com.aptopayments.sdk.core.usecase.*
import com.aptopayments.sdk.core.usecase.BiometricsAuthCorrectUseCase
import com.aptopayments.sdk.core.usecase.CanAskBiometricsUseCase
import com.aptopayments.sdk.core.usecase.ClearCardDetailsUseCase
import com.aptopayments.sdk.core.usecase.FetchLocalCardDetailsUseCase
import com.aptopayments.sdk.core.usecase.FetchRemoteCardDetailsUseCase
import com.aptopayments.sdk.core.usecase.ShouldAuthenticateOnStartUpUseCase
import com.aptopayments.sdk.core.usecase.ShouldAuthenticateWithPINOnPCIUseCase
import com.aptopayments.sdk.core.usecase.VerifyPinUseCase
import org.koin.dsl.module

internal val useCaseModule = module {
    factory { ShouldAuthenticateOnStartUpUseCase(get(), get()) }
    factory { ShouldAuthenticateWithPINOnPCIUseCase(get()) }
    factory { ClearCardDetailsUseCase(get()) }
    factory { FetchRemoteCardDetailsUseCase(get(), get()) }
    factory { FetchLocalCardDetailsUseCase(get()) }
    factory { SavePinUseCase(get()) }
    factory { ShouldCreatePINUseCase(get()) }
    factory { OnEnterBackgroundUseCase(get(), get()) }
    factory { CanAskBiometricsUseCase(get(), get()) }
    factory { VerifyPinUseCase(get()) }
    factory { BiometricsAuthCorrectUseCase(get()) }
    factory { ForgotPinUseCase() }
}
