package com.aptopayments.sdk.core.platform

import android.app.Activity
import android.app.Application
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.features.managecard.CardOptions
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.core.platform.AptoPlatformDelegate
import com.aptopayments.core.platform.AptoSdkEnvironment
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.di.applicationModule
import com.aptopayments.sdk.core.di.useCaseModule
import com.aptopayments.sdk.core.di.viewmodel.viewModelModule
import com.aptopayments.sdk.features.card.CardActivity
import com.aptopayments.sdk.features.card.CardFlow
import com.aptopayments.sdk.utils.FontsUtil
import java.lang.ref.WeakReference

interface AptoUiSdkProtocol {
    fun initializeWithApiKey(
        application: Application,
        apiKey: String,
        environment: AptoSdkEnvironment = AptoSdkEnvironment.PRD
    )

    fun startCardFlow(
        from: Activity,
        cardOptions: CardOptions = CardOptions(),
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    )

    fun userTokenPresent(): Boolean
    fun registerFirebaseToken(firebaseToken: String)
    fun logout()
}

object AptoUiSdk : AptoUiSdkProtocol {

    internal var cardFlow: WeakReference<CardFlow>? = null

    var cardOptions: CardOptions = CardOptions()

    override fun initializeWithApiKey(
        application: Application,
        apiKey: String,
        environment: AptoSdkEnvironment
    ) {
        AptoPlatform.setUiModules(listOf(applicationModule, useCaseModule, viewModelModule))
        AptoPlatform.initializeWithApiKey(application, apiKey, environment)
    }

    fun setDelegate(delegate: AptoPlatformDelegate) {
        AptoPlatform.delegate = delegate
    }

    override fun startCardFlow(
        from: Activity,
        cardOptions: CardOptions,
        onSuccess: (() -> Unit)?,
        onError: ((Failure) -> Unit)?
    ) {
        this.cardOptions = cardOptions
        with(cardOptions.fontOptions) {
            FontsUtil.overrideFonts(
                regularFont = regularFont,
                mediumFont = mediumFont,
                semiBoldFont = semiBoldFont,
                boldFont = boldFont
            )
        }

        val flow = CardFlow()
        cardFlow = WeakReference(flow)
        flow.init { result ->
            result.either({
                onError?.invoke(it)
                Unit
            }) {
                from.startActivity(CardActivity.callingIntent(from))
                from.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                onSuccess?.invoke()
                Unit
            }
        }
    }

    override fun userTokenPresent(): Boolean = AptoPlatform.userTokenPresent()

    override fun registerFirebaseToken(firebaseToken: String) {
        AptoPlatform.registerFirebaseToken(firebaseToken)
    }

    override fun logout() {
        AptoPlatform.logout()
    }
}
