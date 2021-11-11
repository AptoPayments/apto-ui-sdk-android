package com.aptopayments.sdk.utils

import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.content.Intent
import android.content.Intent.ACTION_DIAL
import android.net.Uri
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.*
import java.lang.ref.WeakReference

internal class PhoneDialer(
    val context: Context
) {

    private var delegate: WeakReference<Delegate?>? = null

    fun dialPhone(phone: String, delegate: Delegate?) {
        this.delegate = WeakReference(delegate)
        if (isTelephonyEnabled()) {
            try {
                val intent = Intent(ACTION_DIAL)
                intent.data = Uri.parse("tel:$phone")
                context.startActivity(intent)
                delegate?.onCallStarted()
            } catch (e: Exception) {
                delegate?.onTelephonyNotAvailable()
            }
        } else {
            delegate?.onTelephonyNotAvailable()
        }
    }

    private fun isTelephonyEnabled(): Boolean {
        return (context.getSystemService(TELEPHONY_SERVICE) as? TelephonyManager)?.let {
            it.simState == SIM_STATE_READY
        } ?: false
    }

    interface Delegate {
        fun onTelephonyNotAvailable()
        fun onCallStarted()
    }
}
