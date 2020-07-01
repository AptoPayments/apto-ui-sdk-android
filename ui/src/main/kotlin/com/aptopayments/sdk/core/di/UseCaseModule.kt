package com.aptopayments.sdk.core.di

import com.aptopayments.sdk.core.usecase.*
import org.koin.dsl.module

internal val useCaseModule = module {
    factory { ShouldAuthenticateOnStartUpUseCase(get(), get()) }
    factory { ShouldAuthenticateWithPINOnPCIUseCase(get()) }
    factory { ClearCardDetailsUseCase(get()) }
    factory { FetchRemoteCardDetailsUseCase(get(), get()) }
    factory { FetchLocalCardDetailsUseCase(get()) }
    factory { SavePasscodeUseCase(get()) }
    factory { ShouldCreatePasscodeUseCase(get()) }
    factory { OnEnterBackgroundUseCase(get(), get()) }
    factory { CanAskBiometricsUseCase(get(), get()) }
    factory { VerifyPasscodeUseCase(get()) }
    factory { BiometricsAuthCorrectUseCase(get()) }
    factory { ForgotPinUseCase(get()) }
    factory { DownloadStatementUseCase(get()) }
    factory { ShouldShowBiometricOption(get(), get()) }
}
