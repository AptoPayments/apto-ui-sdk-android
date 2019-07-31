package com.aptopayments.sdk.utils

import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.content.Intent
import android.content.Intent.ACTION_DIAL
import android.net.Uri
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.*
import java.lang.ref.WeakReference

class PhoneDialer (
        val context: Context
) : PhoneStateListener() {

    private var lastKnownState: Int = CALL_STATE_IDLE
    private var delegate: WeakReference<Delegate?>? = null

    init {
        (context.getSystemService(TELEPHONY_SERVICE) as? TelephonyManager)?.listen(this, LISTEN_CALL_STATE)
    }

    fun dialPhone(phone: String, delegate: Delegate?) {
        this.delegate = WeakReference(delegate)
        if (isTelephonyEnabled()) {
            try {
                val intent = Intent(ACTION_DIAL)
                intent.data = Uri.parse("tel:$phone")
                context.startActivity(intent)
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

    override fun onCallStateChanged(state: Int, incomingNumber: String?) {
        when (state) {
            CALL_STATE_IDLE -> {
                when (lastKnownState) {
                    CALL_STATE_RINGING -> {
                        delegate?.get()?.onCallCancelled()
                    }
                    CALL_STATE_OFFHOOK -> {
                        delegate?.get()?.onCallEnded()
                    }
                }
            }
            CALL_STATE_RINGING -> {
                lastKnownState = state
                delegate?.get()?.onCallStarted()
            }
            else -> { lastKnownState = state }
        }
    }

    interface Delegate {
        fun onTelephonyNotAvailable()
        fun onCallStarted()
        fun onCallEnded()
        fun onCallCancelled()
    }
}
