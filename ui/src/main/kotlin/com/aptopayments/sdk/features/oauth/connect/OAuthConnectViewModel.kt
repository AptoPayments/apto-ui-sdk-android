package com.aptopayments.sdk.features.oauth.connect

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.oauth.OAuthAttempt
import com.aptopayments.mobile.data.oauth.OAuthAttemptStatus
import com.aptopayments.mobile.data.workflowaction.AllowedBalanceType
import com.aptopayments.mobile.exception.Failure.ServerError
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent
import java.net.URL

internal class OAuthConnectViewModel(
    private val allowedBalanceType: AllowedBalanceType,
    private val aptoPlatform: AptoPlatformProtocol,
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    private var oauthAttempt: OAuthAttempt? = null
    private var shouldReloadStatus = true

    val action = LiveEvent<Action>()

    init {
        analyticsManager.track(Event.SelectBalanceStoreOauthLogin)
    }

    fun startOAuthAuthentication() {
        showLoading()
        analyticsManager.track(Event.SelectBalanceStoreLoginConnectTap)
        shouldReloadStatus = true
        aptoPlatform.startOauthAuthentication(allowedBalanceType) { result ->
            hideLoading()
            result.either(::handleFailure) { oauthAttemptResult ->
                oauthAttempt = oauthAttemptResult
                if (oauthAttemptResult.url != null) {
                    action.postValue(Action.StartOauth(oauthAttemptResult.url!!))
                } else {
                    handleFailure(ServerError(null))
                }
            }
        }
    }

    fun reloadStatus() {
        if (shouldReloadStatus && oauthAttempt != null) {
            showLoading()
            aptoPlatform.verifyOauthAttemptStatus(oauthAttempt!!) { result ->
                hideLoading()
                result.either(::handleFailure) { attemptResult ->
                    oauthAttempt = attemptResult
                    when (attemptResult.status) {
                        OAuthAttemptStatus.PASSED -> action.postValue(Action.OauthPassed(attemptResult))
                        OAuthAttemptStatus.FAILED -> action.postValue(Action.OauthFailure(attemptResult))
                        OAuthAttemptStatus.PENDING -> action.postValue(Action.OauthPending)
                    }
                }
            }
        }
    }

    sealed class Action {
        class StartOauth(val url: URL) : Action()
        class OauthPassed(val oauthAttempt: OAuthAttempt) : Action()
        class OauthFailure(val oauthAttempt: OAuthAttempt) : Action()
        object OauthPending : Action()
    }
}
