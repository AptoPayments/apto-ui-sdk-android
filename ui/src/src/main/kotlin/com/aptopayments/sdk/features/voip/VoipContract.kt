package com.aptopayments.sdk.features.voip

import android.content.Context
import com.aptopayments.core.data.voip.VoipCall
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface VoipContract {

    interface Delegate : FragmentDelegate {
        fun onVoipCallFinished()
        fun onVoipCallError(error: String?)
    }

    interface View {
        var delegate: Delegate?
    }

    interface Handler {
        var isMuted: Boolean
        var isOnHold: Boolean
        val timeElapsed: Long
        fun startCall(context: Context,
                      destination: VoipCall,
                      onRinging: () -> Unit,
                      onEstablished: () -> Unit,
                      onComplete: () -> Unit,
                      onError: (String?) -> Unit)
        fun sendDigits(digits: String)
        fun disconnect()
    }
}
