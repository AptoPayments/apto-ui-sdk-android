package com.aptopayments.sdk.features.card.statements.detail

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.left
import com.aptopayments.mobile.functional.right
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.core.usecase.DownloadStatementExternalUseCase
import com.aptopayments.sdk.core.usecase.DownloadStatementLocalUseCase
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import com.aptopayments.sdk.features.card.statements.detail.StatementDetailViewModel.Action
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val MONTH = 5
private const val YEAR = 2021

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class)
internal class StatementDetailViewModelTest {

    private val statementMonth = StatementMonth(MONTH, YEAR)
    private val downloadLocalUseCase: DownloadStatementLocalUseCase = mock()
    private val downloadExternalUseCase: DownloadStatementExternalUseCase = mock()
    private val analyticsManager: AnalyticsServiceContract = mock()
    private val file: File = mock()

    private lateinit var sut: StatementDetailViewModel

    @Test
    fun `when sut created then correct tracking is made`() = runBlockingTest {
        configureDownloadLocal(file.right())

        createSut()

        verify(analyticsManager).track(Event.MonthlyStatementsReportStart)
    }

    @Test
    fun `given local file can't be loaded when ViewModel is created then file is not ready`() = runBlockingTest {
        configureDownloadLocal(Failure.ServerError(1).left())

        createSut()

        assertNotNull(sut.state.value)
        assertNull(sut.state.value!!.file)
    }

    @Test
    fun `given file can be loaded when ViewModel is created then file contains data`() = runBlockingTest {
        configureDownloadLocal(file.right())

        createSut()

        assertNotNull(sut.state.value)
        assertNotNull(sut.state.value!!.file)
    }

    @Test
    fun `given downloadExternal succeeds when downloadToPhone then useCase is called`() = runBlockingTest {
        configureDownloadLocal(file.right())
        whenever(downloadExternalUseCase.invoke(statementMonth)).thenReturn(Unit.right())
        createSut()

        sut.downloadToPhone()

        verify(downloadExternalUseCase).invoke(statementMonth)
    }

    @Test
    fun `given downloadExternal succeeds when downloadToPhone then action is fired`() = runBlockingTest {
        configureDownloadLocal(file.right())
        whenever(downloadExternalUseCase.invoke(statementMonth)).thenReturn(Unit.right())
        createSut()

        sut.downloadToPhone()

        val action = sut.action.getOrAwaitValue()
        assertEquals(Action.ShowDownloadingSign, action)
    }

    private fun createSut() {
        sut = StatementDetailViewModel(statementMonth, analyticsManager, downloadLocalUseCase, downloadExternalUseCase)
    }

    private suspend fun configureDownloadLocal(response: Either<Failure, File>) {
        whenever(downloadLocalUseCase.run(statementMonth)).thenReturn(response)
    }
}
