package com.aptopayments.sdk.features.card.statements

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.data.StatementFile
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import com.aptopayments.sdk.ui.fragments.pdf.PdfRendererContract
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Spy
import java.io.File

private const val TITLE = "TITLE"
private const val PDF_RENDERER_TAG = "PdfRendererFragment"

class StatementListFlowTest : AndroidTest() {

    private lateinit var sut: StatementListFlow

    @Mock
    private lateinit var mockFragmentFactory: FragmentFactory

    @Spy
    private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()

    @Mock
    private lateinit var file: File

    @Mock
    private lateinit var pdfRendererFragmentDelegate: PdfRendererContract.Delegate

    private val statementFile: StatementFile by lazy { StatementFile(file, TITLE) }

    @Before
    override fun setUp() {
        super.setUp()
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
        startKoin {
            modules(module {
                single<AnalyticsServiceContract> { analyticsManager }
                single { mockFragmentFactory }
            })
        }
    }

    @Test
    fun `when statement downloaded then event is tracked`() {
        sut = StatementListFlow(onBack = {}, onFinish = {})
        val pdfRendererFragment =
            PdfRendererFragmentDouble(pdfRendererFragmentDelegate).apply { this.TAG = PDF_RENDERER_TAG }
        given {
            mockFragmentFactory.pdfRendererFragment(statementFile.title, statementFile.file, PDF_RENDERER_TAG)
        }.willReturn(pdfRendererFragment)

        sut.onStatementDownloaded(statementFile)

        verify(analyticsManager).track(Event.MonthlyStatementsReportStart)
    }
}
