package com.aptopayments.sdk.core.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.data.statements.MonthlyStatement
import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.left
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.card.statements.detail.ExternalFileDownloader
import com.aptopayments.sdk.utils.shouldBeLeftAndInstanceOf
import com.aptopayments.sdk.utils.shouldBeRightAndEqualTo
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

private const val MONTH = 5
private const val YEAR = 2021
private const val URL = "https://www.aptopayments.com/download.pdf"

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
class DownloadStatementExternalUseCaseTest {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val downloader: ExternalFileDownloader = mock()

    private val sut = DownloadStatementExternalUseCase(aptoPlatform, downloader)

    private val statementMonth = StatementMonth(MONTH, YEAR)

    @Test
    fun `given statement data fetched with error when run UseCase then error is returned`() = runBlockingTest {
        configurePlatform(Failure.NetworkConnection.left())

        val result = sut.run(statementMonth)

        result.shouldBeLeftAndInstanceOf(Failure.NetworkConnection::class.java)
    }

    @Test
    fun `given statement data fetched correctly when run UseCase then downloader is called correctly`() =
        runBlockingTest {
            configurePlatform(TestDataProvider.monthlyStatement(month = MONTH, year = YEAR, downloadUrl = URL).right())
            whenever(downloader.download(any(), any())).thenReturn(Unit.right())

            val result = sut.run(statementMonth)

            result.shouldBeRightAndEqualTo(Unit)
            verify(downloader).download(eq("Statement-$YEAR-$MONTH.pdf"), eq(URL))
        }

    private fun configurePlatform(result: Either<Failure, MonthlyStatement>) {
        whenever(
            aptoPlatform.fetchMonthlyStatement(
                eq(MONTH),
                eq(YEAR),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, MonthlyStatement>) -> Unit).invoke(result)
        }
    }
}
