package com.aptopayments.sdk.core.di

import com.aptopayments.sdk.core.usecase.*
import org.koin.dsl.module

internal val useCaseModule = module {
    factory { ShouldAuthenticateOnStartUpUseCase(get(), get()) }
    factory { ShouldAuthenticateOnPCIUseCase(get()) }
    factory { AuthenticationCompletedUseCase(get()) }
    factory { SavePasscodeUseCase(get()) }
    factory { ShouldCreatePasscodeUseCase(get()) }
    factory { OnEnterBackgroundUseCase(get(), get()) }
    factory { CanAskBiometricsUseCase(get(), get()) }
    factory { VerifyPasscodeUseCase(get()) }
    factory { BiometricsAuthCorrectUseCase(get()) }
    factory { ForgotPinUseCase(get()) }
    factory { DownloadStatementLocalUseCase(get(), get()) }
    factory { DownloadStatementExternalUseCase(get(), get()) }
    factory { ShouldShowBiometricOption(get(), get()) }
    factory { InitNewOrExistingFlowUseCase(get(), get(), get()) }
    factory { SaveFlowConfigurationDataUseCase(get(), get(), get()) }
    factory { AcceptAchDisclaimerUseCase(get()) }
    factory { DeclineAchDisclaimerUseCase(get()) }
}
