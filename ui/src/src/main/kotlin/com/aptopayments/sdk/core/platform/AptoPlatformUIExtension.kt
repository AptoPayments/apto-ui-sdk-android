package com.aptopayments.sdk.core.platform

import android.annotation.SuppressLint
import android.app.Activity
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.features.managecard.CardOptions
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.di.applicationModule
import com.aptopayments.sdk.core.di.viewmodel.viewModelModule
import com.aptopayments.sdk.features.card.CardActivity
import com.aptopayments.sdk.features.card.CardFlow
import com.aptopayments.sdk.utils.FontsUtil
import org.koin.core.context.loadKoinModules
import java.lang.ref.WeakReference

private var _cardFlow: WeakReference<CardFlow>? = null
internal val AptoPlatform.cardFlow: WeakReference<CardFlow>?
   get() {
       return _cardFlow
   }

@SuppressLint("VisibleForTests")
fun AptoPlatform.startCardFlow(from: Activity, cardOptions: CardOptions = CardOptions(), onSuccess: ((Unit) -> Unit)?, onError: ((Failure) -> Unit)?) {
    KoinLoader.instance.loadModules()

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
    _cardFlow = WeakReference(flow)
    flow.init { result ->
        result.either({
            onError?.invoke(it)
            Unit
        }) {
            from.startActivity(CardActivity.callingIntent(from))
            from.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            onSuccess?.invoke(Unit)
            Unit
        }
    }
}

private class KoinLoader private constructor() {
    fun loadModules() {
        if (modulesLoaded) return
        modulesLoaded = true
        loadKoinModules(listOf(applicationModule, viewModelModule))
    }

    companion object {
        private var modulesLoaded = false
        val instance = KoinLoader()
    }
}
