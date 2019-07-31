package com.aptopayments.sdk.features.addbalance

import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.card.SelectBalanceStoreResult
import com.aptopayments.core.data.fundingsources.Balance
import com.aptopayments.core.data.workflowaction.AllowedBalanceType
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.features.oauth.OAuthFlow
import java.lang.reflect.Modifier

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class AddBalanceFlow (
        val allowedBalanceTypes: List<AllowedBalanceType>,
        val cardID: String,
        var onBack: (Unit) -> Unit,
        var onFinish: (balance: Balance) -> Unit
) : Flow() {

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        appComponent.inject(this)
        allowedBalanceTypes.firstOrNull()?.let { allowedBalanceType ->
            initOAuthFlow(allowedBalanceType = allowedBalanceType) { initResult ->
                initResult.either({onInitComplete}) { flow ->
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
        val flow = OAuthFlow(
                allowedBalanceType = allowedBalanceType,
                onBack = { onBack(Unit) },
                onFinish = { oauthAttempt ->
                    val fundingSourceType = "custodian_wallet"
                    val custodianType = allowedBalanceTypes.firstOrNull()?.balanceType?.name ?: "coinbase"
                    val credentialType = "oauth"
                    AptoPlatform.addCardFundingSource(
                            cardId = cardID,
                            fundingSourceType = fundingSourceType,
                            custodianType = custodianType,
                            credentialType = credentialType,
                            tokenId = oauthAttempt.tokenId) { result ->
                        result.either({ failure -> handleAddBalanceFailure(failure) }, { balance ->
                            hideLoading()
                            onFinish(balance)
                        })
                    }
                }
        )
        flow.init { initResult ->
            initResult.either({onComplete}) {
                onComplete(Either.Right(flow))
            }
        }
    }

    private fun handleAddBalanceFailure(failure: Failure) {
        when (failure) {
            is Failure.ServerError -> {
                rootActivity()?.let {
                    notify(SelectBalanceStoreResult.errorMessage(failure.errorCode, it))
                }
            }
            else -> handleFailure(failure)
        }
    }
}

internal class AddBalanceInitFailure : Failure.FeatureFailure()
