package com.aptopayments.sdk.features.voip

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.voip.Action
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.extensions.map
import org.json.JSONObject
import java.util.Timer
import kotlin.concurrent.timer

internal class VoipViewModel(
    private val aptoPlatform: AptoPlatformProtocol,
    private val analyticsManager: AnalyticsServiceContract,
    private val voipHandler: VoipContract.Handler
) : BaseViewModel() {

    sealed class CallState {
        object NotInitiated : CallState()
        object Ringing : CallState()
        class Established(val elapsedTime: Int) : CallState()
        object Finished : CallState()
        object Reconnecting : CallState()
        class Error(val error: String?) : CallState()
    }

    private val _callState = MutableLiveData<CallState>()
    private val _elapsedTime = MutableLiveData<Long>()
    private var timer: Timer? = null
    val callState = _callState as LiveData<CallState>
    val callEstablished = _callState.map { isCallEstablished(it) }

    val elapsedTime = _elapsedTime as LiveData<Long>

    init {
        analyticsManager.track(Event.ManageCardVoipCallStarted)
    }

    fun startCall(context: Context, cardID: String, action: Action) {
        _callState.postValue(CallState.NotInitiated)
        aptoPlatform.fetchVoIPToken(cardID, action) { result ->
            result.either(::handleFailure) {
                voipHandler.startCall(
                    context,
                    it,
                    { callRinging() },
                    { callEstablished() },
                    { callReconnecting() },
                    { onCallComplete() },
                    { error -> onCallError(error) }
                )
            }
        }
    }

    private fun isCallEstablished(it: CallState?) =
        it is CallState.Established || it is CallState.Reconnecting

    private fun callReconnecting() {
        _callState.postValue(CallState.Reconnecting)
    }

    fun sendDigits(digits: String) {
        voipHandler.sendDigits(digits)
    }

    fun disconnect() {
        voipHandler.disconnect()
        timer?.cancel()
    }

    fun toggleMute() {
        voipHandler.isMuted = !voipHandler.isMuted
    }

    private fun callRinging() = _callState.postValue(CallState.Ringing)

    private fun callEstablished() {
        _callState.postValue(CallState.Established(0))
        timer = timer(period = 1000L) {
            if (_callState.value != CallState.Reconnecting) {
                _elapsedTime.postValue(voipHandler.timeElapsed)
            }
        }
    }

    private fun onCallComplete() {
        analyticsManager.track(Event.ManageCardVoipCallEnded, JSONObject().put("time_elapsed", 0))
        _callState.postValue(CallState.Finished)
        timer?.cancel()
    }

    private fun onCallError(error: String?) {
        analyticsManager.track(Event.ManageCardVoipCallError)
        _callState.postValue(CallState.Error(error))
        timer?.cancel()
    }
}
