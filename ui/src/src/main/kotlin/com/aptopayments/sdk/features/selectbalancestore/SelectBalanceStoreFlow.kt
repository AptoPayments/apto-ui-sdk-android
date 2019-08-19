package com.aptopayments.sdk.features.selectbalancestore

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.card.SelectBalanceStoreResult
import com.aptopayments.core.data.oauth.OAuthAttempt
import com.aptopayments.core.data.oauth.OAuthUserDataUpdateResult
import com.aptopayments.core.data.user.AddressDataPoint
import com.aptopayments.core.data.user.DataPoint
import com.aptopayments.core.data.workflowaction.AllowedBalanceType
import com.aptopayments.core.data.workflowaction.WorkflowActionConfigurationSelectBalanceStore
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.extension.localized
import com.aptopayments.core.functional.Either
import com.aptopayments.core.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.oauth.OAuthConfig
import com.aptopayments.sdk.features.oauth.OAuthFlow
import com.aptopayments.sdk.features.oauth.OAuthVerifyFlow
import org.koin.core.inject
import java.lang.reflect.Modifier

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class SelectBalanceStoreFlow (
        var actionConfiguration: WorkflowActionConfigurationSelectBalanceStore,
        var cardApplicationId: String,
        var onBack: (Unit) -> Unit,
        var onFinish: (oauthAttempt: OAuthAttempt) -> Unit
) : Flow() {

    val aptoPlatformProtocol: AptoPlatformProtocol by inject()
    val analyticsManager: AnalyticsServiceContract by inject()
    private lateinit var verifyFlow: OAuthVerifyFlow

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        actionConfiguration.allowedBalanceTypes?.firstOrNull()?.let { allowedBalanceType ->
            initOAuthFlow(allowedBalanceType = allowedBalanceType) { initResult ->
                initResult.either({onInitComplete}) { flow ->
                    setStartElement(element = flow)
                    onInitComplete(Either.Right(Unit))
                }
            }
        }
    }

    override fun restoreState() = Unit

    private fun handleError(failure: Failure?) {
        hideLoading()
        if (failure is Failure.ServerError && failure.isOauthTokenRevokedError()) {
            popFlow(true)
            showRevokedTokenDialog(failure)
        }
        else handleFailure(failure)
    }

    private fun showRevokedTokenDialog(error: Failure.ServerError) = rootActivity()?.let { context ->
        confirm(title = "select_balance_store.login.error.title".localized(context),
                text = error.errorMessage(context),
                confirm = "select_balance_store.login.error.ok_button".localized(context),
                cancel = "",
                onConfirm = { },
                onCancel = { }
        )
    }

    //
    // OAuth flow
    //
    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun initOAuthFlow(allowedBalanceType: AllowedBalanceType, onComplete: (Either<Failure, Flow>) -> Unit) {
        val config = OAuthConfig(
                title = "select_balance_store.login.title",
                explanation = "select_balance_store.login.explanation",
                callToAction = "select_balance_store.login.call_to_action.title",
                newUserAction = "select_balance_store.login.new_user.title",
                allowedBalanceType = allowedBalanceType
        )
        val flow = OAuthFlow(
                config = config,
                onBack = { onBack(Unit) },
                onFinish = { oauthAttempt -> verifyOAuthData(allowedBalanceType, oauthAttempt, onComplete) }
        )
        flow.init { initResult ->
            initResult.either({onComplete}) {
                onComplete(Either.Right(flow))
            }
        }
    }

    private fun verifyOAuthData(allowedBalanceType: AllowedBalanceType, oauthAttempt: OAuthAttempt,
                                onComplete: (Either<Failure, Flow>) -> Unit) {
        verifyFlow = OAuthVerifyFlow(
                allowedBalanceType = allowedBalanceType,
                oauthAttempt = oauthAttempt,
                onBack = { onBack(Unit) },
                onFinish = { result -> confirmAddressIfNeeded(allowedBalanceType, result) },
                onError = { error -> handleError(error) }
        )
        verifyFlow.init { initResult ->
            initResult.either({onComplete}) {
                push(flow = verifyFlow)
            }
        }
    }

    private fun confirmAddressIfNeeded(allowedBalanceType: AllowedBalanceType, oauthAttempt: OAuthAttempt) {
        (oauthAttempt.userData?.getDataPointsOf(DataPoint.Type.ADDRESS)?.firstOrNull() as? AddressDataPoint)?.let { address ->
            rootActivity()?.let { context ->
                var message = "select_balance_store.oauth_confirm.address.confirmation_start".localized(context)
                val separator = "\n\n"
                message += separator + address.toStringRepresentation() + separator
                message += "select_balance_store.oauth_confirm.address.confirmation_end".localized(context)
                confirm(title = "select_balance_store.oauth_confirm.address.title".localized(context),
                        text = message,
                        confirm = "select_balance_store.oauth_confirm.address.ok_button".localized(context),
                        cancel = "select_balance_store.oauth_confirm.address.cancel_button".localized(context),
                        onConfirm = { updateUserIfNeeded(allowedBalanceType, oauthAttempt) },
                        onCancel = { }
                )
            }
        } ?: updateUserIfNeeded(allowedBalanceType, oauthAttempt)
    }

    private fun updateUserIfNeeded(allowedBalanceType: AllowedBalanceType, oauthAttempt: OAuthAttempt) {
        oauthAttempt.userData?.let { userData ->
            showLoading()
            aptoPlatformProtocol.saveOauthUserData(userData, allowedBalanceType, oauthAttempt.tokenId) { result ->
                result.either({ handleError(it) }, {
                    hideLoading()
                    if (it.result == OAuthUserDataUpdateResult.VALID) selectBalanceStore(oauthAttempt)
                    else {
                        rootActivity()?.let { context ->
                            notify("select_balance_store.oauth_confirm.updated_pii_message.message".localized(context))
                        }
                        it.userData?.let { userData -> verifyFlow.showUpdatedUserData(userData) }
                        Unit
                    }
                })
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
                        analyticsManager.track(selectBalanceStoreResult.errorEvent())
                        rootActivity()?.let { context ->
                            notify(errorTitle(context), selectBalanceStoreResult.errorMessage(context))
                        }
                        Unit
                    }
                }
            }
        }
    }

    private fun errorTitle(context: Context) = "select_balance_store.login.error.title".localized(context)
}
