package com.aptopayments.sdk.features.selectbalancestore

import androidx.annotation.VisibleForTesting
import com.aptopayments.mobile.data.card.SelectBalanceStoreResult
import com.aptopayments.mobile.data.oauth.OAuthAttempt
import com.aptopayments.mobile.data.oauth.OAuthUserDataUpdateResult
import com.aptopayments.mobile.data.user.AddressDataPoint
import com.aptopayments.mobile.data.user.DataPoint
import com.aptopayments.mobile.data.workflowaction.AllowedBalanceType
import com.aptopayments.mobile.data.workflowaction.WorkflowActionConfigurationSelectBalanceStore
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.exception.server.ErrorOauthTokenRevoked
import com.aptopayments.mobile.extension.localized
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.left
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.oauth.OAuthConfig
import com.aptopayments.sdk.features.oauth.OAuthFlow
import com.aptopayments.sdk.features.oauth.OAuthVerifyFlow
import org.koin.core.inject
import java.lang.reflect.Modifier

internal class SelectBalanceStoreFlow(
    val actionConfiguration: WorkflowActionConfigurationSelectBalanceStore,
    val cardApplicationId: String,
    val onBack: () -> Unit,
    val onFinish: (oauthAttempt: OAuthAttempt) -> Unit
) : Flow() {

    private val aptoPlatformProtocol: AptoPlatformProtocol by inject()
    private val analyticsManager: AnalyticsServiceContract by inject()
    private lateinit var verifyFlow: OAuthVerifyFlow

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        actionConfiguration.allowedBalanceTypes?.firstOrNull()?.let { allowedBalanceType ->
            initOAuthFlow(
                allowedBalanceType = allowedBalanceType,
                assetUrl = actionConfiguration.assetUrl
            ) { initResult ->
                initResult.either({ onInitComplete(it.left()) }) { flow ->
                    setStartElement(element = flow)
                    onInitComplete(Either.Right(Unit))
                }
            }
        }
    }

    override fun restoreState() = Unit

    private fun handleError(failure: Failure?) {
        hideLoading()
        if (failure is ErrorOauthTokenRevoked) {
            popFlow(true)
            showRevokedTokenDialog(failure)
        } else handleFailure(failure)
    }

    private fun showRevokedTokenDialog(error: Failure.ServerError) {
        confirm(
            title = "select_balance_store.login.error.title".localized(),
            text = error.errorMessage(),
            confirm = "select_balance_store.login.error.ok_button".localized(),
            cancel = "",
            onConfirm = { },
            onCancel = { }
        )
    }

    //
    // OAuth flow
    //
    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun initOAuthFlow(
        allowedBalanceType: AllowedBalanceType,
        assetUrl: String?,
        onComplete: (Either<Failure, Flow>) -> Unit
    ) {
        val config = OAuthConfig(
            title = "select_balance_store.login.title",
            explanation = "select_balance_store.login.explanation",
            callToAction = "select_balance_store.login.call_to_action.title",
            newUserAction = "select_balance_store.login.new_user.title",
            assetUrl = assetUrl,
            allowedBalanceType = allowedBalanceType
        )
        val flow = OAuthFlow(
            config = config,
            onBack = { onBack.invoke() },
            onFinish = { oauthAttempt -> verifyOAuthData(allowedBalanceType, oauthAttempt, onComplete) }
        )
        flow.init { initResult ->
            initResult.either({ onComplete(it.left()) }) {
                onComplete(Either.Right(flow))
            }
        }
    }

    private fun verifyOAuthData(
        allowedBalanceType: AllowedBalanceType,
        oauthAttempt: OAuthAttempt,
        onComplete: (Either<Failure, Flow>) -> Unit
    ) {
        verifyFlow = OAuthVerifyFlow(
            allowedBalanceType = allowedBalanceType,
            oauthAttempt = oauthAttempt,
            onBack = { onBack.invoke() },
            onFinish = { result -> confirmAddressIfNeeded(allowedBalanceType, result) },
            onError = { error -> handleError(error) }
        )
        verifyFlow.init { initResult ->
            initResult.either({ onComplete(it.left()) }) {
                push(flow = verifyFlow)
            }
        }
    }

    private fun confirmAddressIfNeeded(allowedBalanceType: AllowedBalanceType, oauthAttempt: OAuthAttempt) {
        (
            oauthAttempt.userData?.getDataPointsOf(DataPoint.Type.ADDRESS)
                ?.firstOrNull() as? AddressDataPoint
            )?.let { address ->
            val message = "select_balance_store.oauth_confirm.address.confirmation_message".localized()
                .replace("<<ADDRESS>>", address.toStringRepresentation())
            confirm(
                title = "select_balance_store.oauth_confirm.address.confirmation_title".localized(),
                text = message,
                confirm = "select_balance_store.oauth_confirm.address.ok_button".localized(),
                cancel = "select_balance_store.oauth_confirm.address.cancel_button".localized(),
                onConfirm = { updateUserIfNeeded(allowedBalanceType, oauthAttempt) },
                onCancel = { }
            )
        } ?: updateUserIfNeeded(allowedBalanceType, oauthAttempt)
    }

    private fun updateUserIfNeeded(allowedBalanceType: AllowedBalanceType, oauthAttempt: OAuthAttempt) {
        oauthAttempt.userData?.let { userData ->
            showLoading()
            aptoPlatformProtocol.saveOauthUserData(userData, allowedBalanceType, oauthAttempt.tokenId) { result ->
                result.either(
                    { handleError(it) },
                    {
                        hideLoading()
                        if (it.result == OAuthUserDataUpdateResult.VALID) {
                            selectBalanceStore(oauthAttempt)
                        } else {
                            notify("select_balance_store.oauth_confirm.updated_pii_message.message".localized())
                            it.userData?.let { userData -> verifyFlow.showUpdatedUserData(userData) }
                        }
                    }
                )
            }
        } ?: selectBalanceStore(oauthAttempt)
    }

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun selectBalanceStore(oauthAttempt: OAuthAttempt) {
        showLoading()
        aptoPlatformProtocol.setBalanceStore(cardApplicationId, oauthAttempt.tokenId) { result ->
            hideLoading()
            result.either({
                handleError(it)
            }) { selectBalanceStoreResult ->
                when (selectBalanceStoreResult.result) {
                    SelectBalanceStoreResult.Type.VALID -> onFinish(oauthAttempt)
                    else -> {
                        analyticsManager.track(selectBalanceStoreResult.getErrorEvent())
                        notify(
                            "select_balance_store.login.error.title".localized(),
                            selectBalanceStoreResult.errorMessage()
                        )
                    }
                }
            }
        }
    }
}
