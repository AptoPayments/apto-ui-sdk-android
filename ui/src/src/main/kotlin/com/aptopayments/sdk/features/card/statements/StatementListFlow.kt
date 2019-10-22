package com.aptopayments.sdk.features.card.statements

import androidx.annotation.VisibleForTesting
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.data.StatementFile
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.ui.fragments.pdf.PdfRendererContract
import org.koin.core.inject
import java.lang.reflect.Modifier

private const val STATEMENT_LIST_TAG = "StatementListFragment"
private const val PDF_RENDERER_TAG = "PdfRendererFragment"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class StatementListFlow(
    var onBack: () -> Unit,
    var onFinish: () -> Unit
) : Flow(), StatementListContract.Delegate, PdfRendererContract.Delegate {

    val analyticsManager: AnalyticsServiceContract by inject()

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = fragmentFactory.statementListFragment(UIConfig.uiTheme, STATEMENT_LIST_TAG)
        fragment.delegate = this
        setStartElement(fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun onBackPressed() = onBack()

    override fun onStatementDownloaded(file: StatementFile) {
        analyticsManager.track(Event.MonthlyStatementsReportStart)
        val fragment = fragmentFactory.pdfRendererFragment(UIConfig.uiTheme, file.title, file.file, PDF_RENDERER_TAG)
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
