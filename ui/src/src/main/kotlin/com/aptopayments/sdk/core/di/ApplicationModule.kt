package com.aptopayments.sdk.core.di

import android.app.Application
import android.content.Context
import com.aptopayments.core.db.DataBaseProvider
import com.aptopayments.core.db.LocalDB
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.core.platform.AptoPlatformProtocol
import com.aptopayments.core.repository.UserPreferencesRepository
import com.aptopayments.core.repository.UserSessionRepository
import com.aptopayments.core.repository.card.local.CardLocalDao
import com.aptopayments.core.repository.fundingsources.local.BalanceLocalDao
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.core.di.fragment.FragmentFactoryImpl
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.voip.TwilioVoipImpl
import com.aptopayments.sdk.features.voip.VoipContract
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class ApplicationModule(private val application: Application) {

    @Provides @Singleton fun provideApplicationContext(): Context = application

    @Provides @Singleton fun provideFragmentFactory(): FragmentFactory = FragmentFactoryImpl()

    @Provides @Singleton fun provideUserSessionRepository(): UserSessionRepository { return UserSessionRepository(provideApplicationContext()) }

    @Provides @Singleton fun provideBalanceLocalDao(): BalanceLocalDao = provideLocalDB().balanceLocalDao()

    @Provides @Singleton fun provideLocalDB(): LocalDB = DataBaseProvider.getInstance(provideApplicationContext())

    @Provides @Singleton fun provideCardLocalDao(): CardLocalDao = provideLocalDB().cardLocalDao()

    @Provides @Singleton fun provideAnalyticsServiceContract(): AnalyticsServiceContract { return AnalyticsManager(application) }

    @Provides @Singleton fun provideVoipHandler(): VoipContract.Handler { return TwilioVoipImpl() }

    @Provides @Singleton fun provideUserPreferencesRepository(): UserPreferencesRepository { return UserPreferencesRepository(context = provideApplicationContext(), userSessionRepository = provideUserSessionRepository()) }

    @Provides @Singleton fun provideAptoPlatformProtocol(): AptoPlatformProtocol { return AptoPlatform }
}

