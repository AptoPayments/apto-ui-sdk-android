package com.aptopayments.sdk.core.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.core.di.fragment.FragmentFactoryImpl
import com.aptopayments.sdk.core.platform.*
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.biometric.BiometricWrapper
import com.aptopayments.sdk.features.card.cardstats.chart.CategorySpendingSorter
import com.aptopayments.sdk.features.inputdata.address.AddressDataPointGenerator
import com.aptopayments.sdk.features.inputdata.address.PlaceFetcher
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElementMapper
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourcesRepository
import com.aptopayments.sdk.features.loadfunds.result.PaymentResultElementMapper
import com.aptopayments.sdk.features.voip.TwilioVoipImpl
import com.aptopayments.sdk.features.voip.VoipContract
import com.aptopayments.sdk.repository.*
import com.aptopayments.sdk.ui.views.birthdate.FormatOrderGenerator
import com.aptopayments.sdk.utils.*
import com.aptopayments.sdk.utils.deeplinks.IntentGenerator
import com.google.android.libraries.places.api.Places
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val PREF_FILE_NAME = "com.aptopayments.sdk.sharedPreference"

internal val applicationModule = module {
    factory<StringProvider> { StringProviderImpl(androidApplication()) }
    factory<CoroutineDispatcherProvider> { ProductionDispatchers() }
    single<AuthStateProvider> { AuthStateProviderImpl() }
    single<FragmentFactory> { FragmentFactoryImpl() }
    single<AnalyticsServiceContract> { AnalyticsManager(androidApplication()) }
    single<VoipContract.Handler> { TwilioVoipImpl() }
    single<AptoPlatformProtocol> { AptoPlatform }
    single<AptoUiSdkProtocol> { AptoUiSdk }
    factory<FileSharer> { FileSharerImpl() }
    factory<FileDownloader> { FileDownloaderImpl() }
    factory<CacheFileManager> { CacheFileManagerImpl(get()) }
    factory<StatementRepository> { StatementRepositoryImpl(get(), get(), get()) }
    single { provideSharedPreferences(androidApplication()) }
    single<AuthenticationRepository> { AuthenticationRepositoryImpl(get(), get()) }
    factory { DateProvider() }
    factory<Timer> { RealTimer() }
    single<LocalCardDetailsRepository> { InMemoryLocalCardDetailsRepository(get()) }
    single { AppLifecycleObserver() }
    single { BiometricWrapper(androidContext()) }
    factory { FormatOrderGenerator(get()) }
    factory { CategorySpendingSorter() }
    single<IAPHelper> { IAPHelperMock() }
    factory { IntentGenerator() }
    factory { Places.createClient(get()) }
    factory { PlaceFetcher(get(), get()) }
    factory { AddressDataPointGenerator() }
    factory<IssueCardAdditionalFieldsRepository> { IssueCardAdditionalFieldsRepositoryImpl }
    single { PaymentSourcesRepository(get(), get()) }
    factory { PaymentSourceElementMapper() }
    factory { PaymentResultElementMapper(get()) }
}

private fun provideSharedPreferences(app: Application): SharedPreferences =
    app.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
