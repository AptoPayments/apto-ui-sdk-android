package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.data.statements.MonthlyStatement
import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.repository.StatementRepository
import com.aptopayments.sdk.repository.StatementRepositoryImpl
import com.aptopayments.sdk.utils.*
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.test.assertTrue

private const val MONTH = 12
private const val YEAR = 2019
private const val URL = "url"

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
internal class DownloadStatementLocalUseCaseTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val fileDownloader: FileDownloader = mock()
    private val cacheFileManager: CacheFileManager = mock()

    private val statement = StatementMonth(MONTH, YEAR)
    private val monthlyStatement: MonthlyStatement =
        TestDataProvider.monthlyStatement(month = MONTH, year = YEAR, downloadUrl = URL)

    lateinit var repo: StatementRepository
    lateinit var sut: DownloadStatementLocalUseCase
    private val aptoPlatform: AptoPlatformProtocol = mock()

    @Before
    fun setup() {
        repo = spy(
            StatementRepositoryImpl(
                fileDownloader,
                cacheFileManager,
                TestDispatchers(coroutineRule.testDispatcher)
            )
        )
        sut = DownloadStatementLocalUseCase(repo, aptoPlatform)
    }

    @Test
    fun `when exception thrown downloading then left is returned`() = coroutineRule.runBlockingTest {
        val file = mock<File>()
        whenever(cacheFileManager.createTempFile(any(), any(), any())).thenReturn(file)
        configurePlatform(monthlyStatement.right())
        whenever(fileDownloader.downloadFile(URL, file)).thenThrow(RuntimeException())

        val result = sut.run(statement)

        assertTrue(result.isLeft)
        verify(fileDownloader).downloadFile(URL, file)
    }

    @Test
    fun `when preconditions are Ok then file is correctly downloaded`() = coroutineRule.runBlockingTest {
        val file = mock<File>()
        whenever(cacheFileManager.createTempFile(any(), any(), any())).thenReturn(file)
        configurePlatform(monthlyStatement.right())

        val result = sut.run(statement)

        result.shouldBeRightAndEqualTo(file)

        verify(cacheFileManager).createTempFile(any(), any(), any())
        verify(fileDownloader).downloadFile(URL, file)
        verify(repo).clearCache()
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
