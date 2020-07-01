package com.aptopayments.sdk.features.card.cardstats

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.transaction.MCC
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.data.StatementFile
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.card.transactionlist.TransactionListConfig
import com.aptopayments.sdk.features.card.transactionlist.TransactionListFlow
import com.aptopayments.sdk.ui.fragments.pdf.PdfRendererContract
import org.koin.core.inject
import org.threeten.bp.LocalDate

private const val CARD_MONTHLY_STATS_TAG = "CardMonthlyStatsFragment"
private const val PDF_RENDERER_TAG = "PdfRendererFragment"

internal class CardStatsFlow(
    var cardId: String,
    var onBack: () -> Unit,
    var onFinish: () -> Unit
) : Flow(), CardMonthlyStatsContract.Delegate, PdfRendererContract.Delegate {

    val analyticsManager: AnalyticsServiceContract by inject()

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = fragmentFactory.cardMonthlyStatsFragment(cardId, CARD_MONTHLY_STATS_TAG)
        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(CARD_MONTHLY_STATS_TAG) as? CardMonthlyStatsContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(PDF_RENDERER_TAG) as? PdfRendererContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onBackFromCardMonthlyStats() = onBack()

    override fun onCategorySelected(mcc: MCC, startDate: LocalDate, endDate: LocalDate) {
        val config = TransactionListConfig(startDate, endDate, mcc)
        val flow = TransactionListFlow(cardId, config, onBack = { popFlow(animated = true) })
        flow.init { initResult ->
            initResult.either(::handleFailure) { push(flow = flow) }
        }
    }

    override fun showMonthlyStatement(file: StatementFile) {
        analyticsManager.track(Event.MonthlyStatementsReportStart)
        val fragment = fragmentFactory.pdfRendererFragment(file.title, file.file, PDF_RENDERER_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun onPdfBackPressed() {
        popFragment()
    }
}
