package com.aptopayments.sdk.core.di

import com.aptopayments.sdk.core.usecase.BiometricsAuthCorrectUseCase
import com.aptopayments.sdk.core.usecase.CanAskBiometricsUseCase
import com.aptopayments.sdk.core.usecase.DownloadStatementUseCase
import com.aptopayments.sdk.core.usecase.ForgotPinUseCase
import com.aptopayments.sdk.core.usecase.InitNewOrExistingFlowUseCase
import com.aptopayments.sdk.core.usecase.OnEnterBackgroundUseCase
import com.aptopayments.sdk.core.usecase.SaveFlowConfigurationDataUseCase
import com.aptopayments.sdk.core.usecase.SavePasscodeUseCase
import com.aptopayments.sdk.core.usecase.ShouldAuthenticateOnPCIUseCase
import com.aptopayments.sdk.core.usecase.ShouldAuthenticateOnStartUpUseCase
import com.aptopayments.sdk.core.usecase.ShouldCreatePasscodeUseCase
import com.aptopayments.sdk.core.usecase.ShouldShowBiometricOption
import com.aptopayments.sdk.core.usecase.VerifyPasscodeUseCase
import org.koin.dsl.module

internal val useCaseModule = module {
    factory { ShouldAuthenticateOnStartUpUseCase(get(), get()) }
    factory { ShouldAuthenticateOnPCIUseCase(get()) }
    factory { SavePasscodeUseCase(get()) }
    factory { ShouldCreatePasscodeUseCase(get()) }
    factory { OnEnterBackgroundUseCase(get(), get()) }
    factory { CanAskBiometricsUseCase(get(), get()) }
    factory { VerifyPasscodeUseCase(get()) }
    factory { BiometricsAuthCorrectUseCase(get()) }
    factory { ForgotPinUseCase(get()) }
    factory { DownloadStatementUseCase(get()) }
    factory { ShouldShowBiometricOption(get(), get()) }
    factory { InitNewOrExistingFlowUseCase(get(), get(), get()) }
    factory { SaveFlowConfigurationDataUseCase(get(), get(), get()) }
}
