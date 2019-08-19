package com.aptopayments.sdk.core.di

import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.core.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.core.di.fragment.FragmentFactoryImpl
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.voip.TwilioVoipImpl
import com.aptopayments.sdk.features.voip.VoipContract
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

internal val applicationModule = module {
    single<FragmentFactory> { FragmentFactoryImpl() }

    single<AnalyticsServiceContract> { AnalyticsManager(androidApplication()) }

    single<VoipContract.Handler> { TwilioVoipImpl() }

    single<AptoPlatformProtocol> { AptoPlatform }
}
