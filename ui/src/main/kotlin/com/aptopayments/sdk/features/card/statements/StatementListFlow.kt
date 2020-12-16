package com.aptopayments.sdk.features.card.statements

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.data.StatementFile
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.ui.fragments.pdf.PdfRendererContract
import org.koin.core.inject

private const val STATEMENT_LIST_TAG = "StatementListFragment"
private const val PDF_RENDERER_TAG = "PdfRendererFragment"

internal class StatementListFlow(
    val onBack: () -> Unit,
    val onFinish: () -> Unit
) : Flow(), StatementListContract.Delegate, PdfRendererContract.Delegate {

    val analyticsManager: AnalyticsServiceContract by inject()

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = fragmentFactory.statementListFragment(STATEMENT_LIST_TAG)
        fragment.delegate = this
        setStartElement(fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun onBackPressed() = onBack()

    override fun onStatementDownloaded(file: StatementFile) {
        analyticsManager.track(Event.MonthlyStatementsReportStart)
        val fragment = fragmentFactory.pdfRendererFragment(file.title, file.file, PDF_RENDERER_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun restoreState() {
        (fragmentWithTag(STATEMENT_LIST_TAG) as? StatementListContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(PDF_RENDERER_TAG) as? PdfRendererContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onPdfBackPressed() {
        popFragment()
    }
}
