package com.aptopayments.sdk.features.card.cardsettings

import android.content.Context
import android.telephony.TelephonyManager

interface TelephonyEnabledChecker {
    fun isEnabled(): Boolean
}

class TelephonyEnabledCheckerImpl(private val context: Context) : TelephonyEnabledChecker {

    override fun isEnabled(): Boolean {
        return (context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)?.let {
            it.simState == TelephonyManager.SIM_STATE_READY
        } ?: false
    }
}
