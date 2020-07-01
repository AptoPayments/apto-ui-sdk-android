package com.aptopayments.sdk.features.addbalance

import com.aptopayments.mobile.data.card.SelectBalanceStoreResult
import com.aptopayments.mobile.data.card.SelectBalanceStoreResult.Type.INVALID
import com.aptopayments.mobile.data.fundingsources.Balance
import com.aptopayments.mobile.data.workflowaction.AllowedBalanceType
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.features.oauth.OAuthConfig
import com.aptopayments.sdk.features.oauth.OAuthFlow

private const val CUSTODIAN_WALLET_FUNDING_SOURCE = "custodian_wallet"
private const val OAUTH_CREDENTIAL_TYPE = "oauth"

internal class AddBalanceFlow(
    val allowedBalanceTypes: List<AllowedBalanceType>,
    val cardID: String,
    var onBack: (Unit) -> Unit,
    var onFinish: (balance: Balance) -> Unit
) : Flow() {

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        allowedBalanceTypes.firstOrNull()?.let { allowedBalanceType ->
            initOAuthFlow(allowedBalanceType = allowedBalanceType) { initResult ->
                initResult.either({ onInitComplete }) { flow ->
                    setStartElement(element = flow)
                    onInitComplete(Either.Right(Unit))
                }
            }
        } ?: onInitComplete(Either.Left(AddBalanceInitFailure()))
    }

    override fun restoreState() = Unit

    //
    // OAuth flow
    //
    private fun initOAuthFlow(allowedBalanceType: AllowedBalanceType, onComplete: (Either<Failure, Flow>) -> Unit) {
        val config = OAuthConfig(
            title = "external_oauth.login.title",
            explanation = "external_oauth.login.explanation",
            callToAction = "external_oauth.login.call_to_action.title",
            newUserAction = "external_oauth.login.new_user.title",
            allowedBalanceType = allowedBalanceType,
            assetUrl = null,
            errorMessageKeys = oauthErrorMessageKeys
        )
        val flow = OAuthFlow(
            config = config,
            onBack = { onBack(Unit) },
            onFinish = { oauthAttempt ->
                val custodianType = allowedBalanceTypes.firstOrNull()?.balanceType ?: ""
                AptoPlatform.addCardFundingSource(
                    cardId = cardID,
                    fundingSourceType = CUSTODIAN_WALLET_FUNDING_SOURCE,
                    custodianType = custodianType,
                    credentialType = OAUTH_CREDENTIAL_TYPE,
                    tokenId = oauthAttempt.tokenId
                ) { result ->
                    result.either({ failure -> handleAddBalanceFailure(failure) }, { balance ->
                        hideLoading()
                        onFinish(balance)
                    })
                }
            }
        )
        flow.init { initResult ->
            initResult.either({ onComplete }) {
                onComplete(Either.Right(flow))
            }
        }
    }

    private fun handleAddBalanceFailure(failure: Failure) {
        when (failure) {
            is Failure.ServerError -> {
                val result = SelectBalanceStoreResult(INVALID, failure.code, errorMessageKeys)
                notify(result.errorMessage())
            }
            else -> handleFailure(failure)
        }
    }

    private val errorMessageKeys: List<String>
        get() = listOf(
            "external_auth.login.error_wrong_country.message",
            "external_auth.login.error_wrong_region.message",
            "external_auth.login.error_unverified_address.message",
            "external_auth.login.error_unsupported_currency.message",
            "external_auth.login.error_cant_capture_funds.message",
            "external_auth.login.error_insufficient_funds.message",
            "external_auth.login.error_balance_not_found.message",
            "external_auth.login.error_access_token_invalid.message",
            "external_auth.login.error_scopes_required.message",
            "external_auth.login.error_missing_legal_name.message",
            "external_auth.login.error_missing_birthdate.message",
            "external_auth.login.error_wrong_birthdate.message",
            "external_auth.login.error_missing_address.message",
            "external_auth.login.error_missing_email.message",
            "external_auth.login.error_wrong_email.message",
            "external_auth.login.error_email_sends_disabled.message",
            "external_auth.login.error_insufficient_application_limit.message",
            "external_auth.login.error_identity_not_verified.message",
            "external_auth.login.error_unknown.message"
        )
    private val oauthErrorMessageKeys: List<String>
        get() = listOf(
            "external_auth.login.error_oauth_invalid_request.message",
            "external_auth.login.error_oauth_unauthorised_client.message",
            "external_auth.login.error_oauth_access_denied.message",
            "external_auth.login.error_oauth_unsupported_response_type.message",
            "external_auth.login.error_oauth_invalid_scope.message",
            "external_auth.login.error_oauth_server_error.message",
            "external_auth.login.error_oauth_temporarily_unavailable.message",
            "external_auth.login.error_oauth_unknown.message"
        )
}

internal class AddBalanceInitFailure : Failure.FeatureFailure()
