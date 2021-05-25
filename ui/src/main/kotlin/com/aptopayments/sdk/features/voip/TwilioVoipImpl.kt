package com.aptopayments.sdk.features.voip

import android.content.Context
import com.aptopayments.mobile.data.voip.VoipCall
import com.twilio.voice.Call
import com.twilio.voice.CallException
import com.twilio.voice.ConnectOptions
import com.twilio.voice.Voice
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit

internal class TwilioVoipImpl : VoipContract.Handler, Call.Listener {

    override var isMuted: Boolean
        get() {
            activeCall?.let {
                return it.isMuted
            }
            return false
        }
        set(value) {
            activeCall?.mute(value)
        }

    override var isOnHold: Boolean
        get() {
            activeCall?.let {
                return it.isOnHold
            }
            return false
        }
        set(value) {
            activeCall?.hold(value)
        }

    override val timeElapsed: Long
        get() {
            startedAt?.let {
                return ChronoUnit.SECONDS.between(it, LocalTime.now())
            }
            return 0
        }

    private var activeCall: Call? = null
    private var startedAt: LocalTime? = null
    private var onRinging: (() -> Unit)? = null
    private var onEstablished: (() -> Unit)? = null
    private var onReconnecting: (() -> Unit)? = null
    private var onComplete: (() -> Unit)? = null
    private var onError: ((String?) -> Unit)? = null

    override fun startCall(
        context: Context,
        destination: VoipCall,
        onRinging: () -> Unit,
        onEstablished: () -> Unit,
        onReconnecting: () -> Unit,
        onComplete: () -> Unit,
        onError: (String?) -> Unit
    ) {
        this.onRinging = onRinging
        this.onEstablished = onEstablished
        this.onReconnecting = onReconnecting
        this.onComplete = onComplete
        this.onError = onError
        val params: HashMap<String, String> = HashMap()
        params["request_token"] = destination.requestToken
        val connectOptions = ConnectOptions.Builder(destination.accessToken).params(params).build()
        activeCall = Voice.connect(context, connectOptions, this)
    }

    override fun sendDigits(digits: String) {
        activeCall?.sendDigits(digits)
    }

    override fun disconnect() {
        activeCall?.disconnect()
        activeCall = null
    }

    override fun onConnectFailure(call: Call, error: CallException) {
        onError?.invoke(error.message)
        clearCallbacks()
    }

    override fun onConnected(call: Call) {
        activeCall = call
        startedAt = LocalTime.now()
        onEstablished?.invoke()
    }

    override fun onReconnecting(call: Call, callException: CallException) {
        onReconnecting?.invoke()
    }

    override fun onReconnected(call: Call) {
        onEstablished?.invoke()
    }

    override fun onDisconnected(call: Call, error: CallException?) {
        error?.let {
            onError?.invoke(error.message)
        } ?: onComplete?.invoke()
        clearCallbacks()
    }

    override fun onRinging(call: Call) {
        onRinging?.invoke()
    }

    private fun clearCallbacks() {
        onRinging = null
        onEstablished = null
        onReconnecting = null
        onComplete = null
        onError = null
    }
}
