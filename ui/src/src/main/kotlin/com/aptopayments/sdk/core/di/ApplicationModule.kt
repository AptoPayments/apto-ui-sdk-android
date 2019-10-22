package com.aptopayments.sdk.core.di

import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.core.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.core.di.fragment.FragmentFactoryImpl
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.voip.TwilioVoipImpl
import com.aptopayments.sdk.features.voip.VoipContract
import com.aptopayments.sdk.repository.StatementRepository
import com.aptopayments.sdk.repository.StatementRepositoryImpl
import com.aptopayments.sdk.utils.FileSharer
import com.aptopayments.sdk.utils.FileSharerImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val applicationModule = module {
    single<FragmentFactory> { FragmentFactoryImpl() }

    single<AnalyticsServiceContract> { AnalyticsManager(androidApplication()) }

    single<VoipContract.Handler> { TwilioVoipImpl() }

    single<AptoPlatformProtocol> { AptoPlatform }

    factory<FileSharer> { FileSharerImpl() }

    factory<StatementRepository> { StatementRepositoryImpl(androidContext()) }
}
