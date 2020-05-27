package com.aptopayments.sdk.features.oauth.connect

import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.oauth.OAuthAttempt
import com.aptopayments.core.data.workflowaction.AllowedBalanceType
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal class OAuthConnectViewModel constructor(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    var oauthAttempt: MutableLiveData<OAuthAttempt> = MutableLiveData()

    fun startOAuthAuthentication(
        allowedBalanceType: AllowedBalanceType,
        callback: (oauthAttempt: OAuthAttempt) -> Unit
    ) = launch {
        analyticsManager.track(Event.SelectBalanceStoreLoginConnectTap)
        AptoPlatform.startOauthAuthentication(allowedBalanceType) { result ->
            result.either(::handleFailure) { oauthAttemptResult ->
                oauthAttempt.value = oauthAttemptResult
                callback(oauthAttemptResult)
            }
        }
    }

    fun checkOAuthAuthentication(oauthAttempt: OAuthAttempt, callback: (oauthAttempt: OAuthAttempt) -> Unit) {
        AptoPlatform.verifyOauthAttemptStatus(oauthAttempt) { result ->
            result.either(::handleFailure) { oauthAttempt ->
                this.oauthAttempt.value = oauthAttempt
                callback(oauthAttempt)
            }
        }
    }

    fun viewLoaded() {
        analyticsManager.track(Event.SelectBalanceStoreOauthLogin)
    }
}
