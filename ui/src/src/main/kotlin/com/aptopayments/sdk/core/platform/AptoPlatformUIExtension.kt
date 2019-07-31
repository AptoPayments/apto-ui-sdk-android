package com.aptopayments.sdk.core.platform

import android.app.Activity
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PROTECTED
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.features.managecard.CardOptions
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.di.ApplicationComponent
import com.aptopayments.sdk.core.di.ApplicationModule
import com.aptopayments.sdk.core.di.DaggerApplicationComponent
import com.aptopayments.sdk.features.card.CardActivity
import com.aptopayments.sdk.features.card.CardFlow
import com.aptopayments.sdk.utils.FontsUtil
import java.lang.ref.WeakReference
import java.lang.reflect.Modifier.PRIVATE

@VisibleForTesting(otherwise = PRIVATE)
internal lateinit var _applicationComponent: ApplicationComponent

@VisibleForTesting(otherwise = PROTECTED)
internal val AptoPlatform.applicationComponent: ApplicationComponent
    get() {
        return _applicationComponent
    }

private var _cardFlow: WeakReference<CardFlow>? = null
internal val AptoPlatform.cardFlow: WeakReference<CardFlow>?
   get() {
       return _cardFlow
   }

fun AptoPlatform.startCardFlow(from: Activity, cardOptions: CardOptions = CardOptions(), onSuccess: ((Unit) -> Unit)?, onError: ((Failure) -> Unit)?) {
    _applicationComponent = DaggerApplicationComponent
            .builder()
            .applicationModule(ApplicationModule(application))
            .build()

    AptoPlatform.cardOptions = cardOptions
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
