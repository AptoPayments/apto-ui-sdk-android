package com.aptopayments.sdk.features.voip

import android.content.Context
import android.widget.Toast
import com.aptopayments.mobile.data.voip.VoipCall

class DummyVoipHandler : VoipContract.Handler {
    override var isMuted: Boolean = false
    override var isOnHold: Boolean = false
    override val timeElapsed: Long = 0

    override fun startCall(
        context: Context,
        destination: VoipCall,
        onRinging: () -> Unit,
        onEstablished: () -> Unit,
        onReconnecting: () -> Unit,
        onComplete: () -> Unit,
        onError: (String?) -> Unit
    ) {
        Toast.makeText(context, "Error. VoiP dependency not imported.", Toast.LENGTH_LONG).show()
    }

    override fun sendDigits(digits: String) {
        // do nothing
    }

    override fun disconnect() {
        // do nothing
    }
}
