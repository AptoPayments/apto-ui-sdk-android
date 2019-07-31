package com.aptopayments.sdk.features.voip

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.data.voip.Action
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.core.analytics.Event
import org.json.JSONObject
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.timer

internal class VoipViewModel @Inject constructor(
        private val analyticsManager: AnalyticsServiceContract,
        private val voipHandler: VoipContract.Handler
) : BaseViewModel() {

    sealed class CallState {
        object NotInitiated : CallState()
        object Ringing : CallState()
        class Established(val elapsedTime: Int) : CallState()
        object Finished : CallState()
        class Error(val error: String?) : CallState()
    }

    var callState: MutableLiveData<CallState> = MutableLiveData()
    var elapsedTime: MutableLiveData<Long> = MutableLiveData()
    var timer: Timer? = null

    fun startCall(context: Context, cardID: String, action: Action) {
        callState.postValue(CallState.NotInitiated)
        AptoPlatform.fetchVoIPToken(cardID, action) { result ->
            result.either(::handleFailure) {
                voipHandler.startCall(
                        context,
                        it,
                        { callRinging() },
                        { callEstablished() },
                        { onCallComplete() },
                        { error -> onCallError(error) }
                )
            }
        }
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

    fun viewLoaded() = analyticsManager.track(Event.ManageCardVoipCallStarted)

    private fun callRinging() = callState.postValue(CallState.Ringing)

    private fun callEstablished() {
        callState.postValue(CallState.Established(0))
        timer = timer(period = 1000L) {
            elapsedTime.postValue(voipHandler.timeElapsed)
        }
    }

    private fun onCallComplete() {
        analyticsManager.track(Event.ManageCardVoipCallEnded, JSONObject().put("time_elapsed", 0))
        callState.postValue(CallState.Finished)
        timer?.cancel()
    }

    private fun onCallError(error: String?) {
        analyticsManager.track(Event.ManageCardVoipCallError)
        callState.postValue(CallState.Error(error))
        timer?.cancel()
    }
}
