package com.aptopayments.sdk.features.card.statements

import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.card.statements.detail.StatementDetailContract

private const val STATEMENT_LIST_TAG = "StatementListFragment"
private const val PDF_RENDERER_TAG = "PdfRendererFragment"

internal class StatementListFlow(
    val onBack: () -> Unit,
    val onFinish: () -> Unit
) : Flow(), StatementListContract.Delegate, StatementDetailContract.Delegate {

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = fragmentFactory.statementListFragment(STATEMENT_LIST_TAG)
        fragment.delegate = this
        setStartElement(fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun onBackPressed() = onBack()

    override fun onStatementPressed(statementMonth: StatementMonth) {
        val fragment = fragmentFactory.statementDetailsFragment(statementMonth, PDF_RENDERER_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun restoreState() {
        (fragmentWithTag(STATEMENT_LIST_TAG) as? StatementListContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(PDF_RENDERER_TAG) as? StatementDetailContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onPdfBackPressed() {
        popFragment()
    }
}
