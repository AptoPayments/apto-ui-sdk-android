package com.aptopayments.sdk.features.card.cardstats

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.data.StatementFile
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.card.statements.PdfRendererFragmentDouble
import com.aptopayments.sdk.ui.fragments.pdf.PdfRendererContract
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File

private const val TITLE = "TITLE"
private const val PDF_RENDERER_TAG = "PdfRendererFragment"

class CardStatsFlowTest : UnitTest() {

    private lateinit var sut: CardStatsFlow
    private val mockFragmentFactory: FragmentFactory = mock()
    private val analyticsManager: AnalyticsServiceContract = mock()
    private val file: File = mock()
    private val pdfRendererFragmentDelegate: PdfRendererContract.Delegate = mock()

    private val statementFile: StatementFile by lazy { StatementFile(file, TITLE) }

    @Before
    fun setUp() {
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
        startKoin {
            modules(
                module {
                    single<AnalyticsServiceContract> { analyticsManager }
                    single { mockFragmentFactory }
                }
            )
        }
    }

    @Test
    fun `when statement downloaded then event is tracked`() {
        sut = CardStatsFlow("card_id", onBack = {}, onFinish = {})
        val pdfRendererFragment = PdfRendererFragmentDouble(pdfRendererFragmentDelegate).apply {
            this.TAG = PDF_RENDERER_TAG
        }
        given {
            mockFragmentFactory.pdfRendererFragment(statementFile.title, statementFile.file, PDF_RENDERER_TAG)
        }.willReturn(pdfRendererFragment)

        sut.showMonthlyStatement(statementFile)

        verify(analyticsManager).track(Event.MonthlyStatementsReportStart)
    }
}
