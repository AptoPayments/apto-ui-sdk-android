package com.aptopayments.sdk.features.card.cardstats

import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.transaction.MCC
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.card.transactionlist.TransactionListConfig
import com.aptopayments.sdk.features.card.transactionlist.TransactionListFlow
import org.threeten.bp.LocalDate
import java.lang.reflect.Modifier

private const val CARD_MONTHLY_STATS_TAG = "CardMonthlyStatsFragment"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class CardStatsFlow (
        var cardId: String,
        var onBack: () -> Unit,
        var onFinish: () -> Unit
) : Flow(), CardMonthlyStatsContract.Delegate {

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        appComponent.inject(this)
        val fragment = fragmentFactory.cardMonthlyStatsFragment(
                uiTheme = UIConfig.uiTheme,
                cardId = cardId,
                tag = CARD_MONTHLY_STATS_TAG)
        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(CARD_MONTHLY_STATS_TAG) as? CardMonthlyStatsContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onBackFromCardMonthlyStats() {
        onBack()
    }

    override fun onCategorySelected(mcc: MCC, startDate: LocalDate, endDate: LocalDate) {
        val config = TransactionListConfig(startDate, endDate, mcc)
        val flow = TransactionListFlow(cardId, config, onBack = { popFlow(animated = true) })
        flow.init { initResult ->
            initResult.either(::handleFailure) { push(flow = flow) }
        }
    }
}
