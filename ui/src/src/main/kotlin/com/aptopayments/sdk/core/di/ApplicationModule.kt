package com.aptopayments.sdk.core.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.core.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.core.di.fragment.FragmentFactoryImpl
import com.aptopayments.sdk.core.platform.*
import com.aptopayments.sdk.core.platform.AppLifecycleObserver
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.biometric.BiometricWrapper
import com.aptopayments.sdk.features.auth.birthdateverification.FormatOrderGenerator
import com.aptopayments.sdk.features.voip.TwilioVoipImpl
import com.aptopayments.sdk.features.voip.VoipContract
import com.aptopayments.sdk.repository.*
import com.aptopayments.sdk.repository.StatementRepository
import com.aptopayments.sdk.repository.StatementRepositoryImpl
import com.aptopayments.sdk.utils.DateProvider
import com.aptopayments.sdk.utils.FileSharer
import com.aptopayments.sdk.utils.FileSharerImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val PREF_FILE_NAME = "com.aptopayments.sdk.sharedPreference"

internal val applicationModule = module {

    single<AuthStateProvider> { AuthStateProviderImpl() }

    single<FragmentFactory> { FragmentFactoryImpl() }

    single<AnalyticsServiceContract> { AnalyticsManager(androidApplication()) }

    single<VoipContract.Handler> { TwilioVoipImpl() }

    single<AptoPlatformProtocol> { AptoPlatform }

    factory<FileSharer> { FileSharerImpl() }

    factory<StatementRepository> { StatementRepositoryImpl(androidContext()) }

    single { provideSharedPreferences(androidApplication()) }

    factory<AuthenticationRepository> { AuthenticationRepositoryImpl(get()) }

    factory { DateProvider() }
    single<LocalCardDetailsRepository> { InMemoryLocalCardDetailsRepository(get()) }
    factory<RemoteCardDetailsRepository> { RemoteCardDetailsRepositoryImpl() }
    single { AppLifecycleObserver() }
    single { BiometricWrapper(androidContext()) }
    factory { FormatOrderGenerator(get()) }
}

private fun provideSharedPreferences(app: Application): SharedPreferences =
    app.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
