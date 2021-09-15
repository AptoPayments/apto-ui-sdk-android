package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.data.statements.MonthlyStatement
import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.left
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.card.statements.detail.ExternalFileDownloader
import com.aptopayments.sdk.utils.shouldBeLeftAndInstanceOf
import com.aptopayments.sdk.utils.shouldBeRightAndEqualTo
import org.mockito.kotlin.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private const val MONTH = 5
private const val YEAR = 2021
private const val URL = "https://www.aptopayments.com/download.pdf"

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class)
class DownloadStatementExternalUseCaseTest {

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
