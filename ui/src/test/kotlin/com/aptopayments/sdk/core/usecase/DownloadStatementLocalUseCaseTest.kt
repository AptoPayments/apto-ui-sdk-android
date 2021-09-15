package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.data.statements.MonthlyStatement
import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.CoroutineDispatcherTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.repository.StatementRepository
import com.aptopayments.sdk.repository.StatementRepositoryImpl
import com.aptopayments.sdk.utils.*
import org.mockito.kotlin.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

private const val MONTH = 12
private const val YEAR = 2019
private const val URL = "url"

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
internal class DownloadStatementLocalUseCaseTest : CoroutineDispatcherTest {

    override lateinit var dispatcher: TestCoroutineDispatcher

    private val fileDownloader: FileDownloader = mock()
    private val cacheFileManager: CacheFileManager = mock()

    private val statement = StatementMonth(MONTH, YEAR)
    private val monthlyStatement: MonthlyStatement =
        TestDataProvider.monthlyStatement(month = MONTH, year = YEAR, downloadUrl = URL)

    lateinit var repo: StatementRepository
    lateinit var sut: DownloadStatementLocalUseCase
    private val aptoPlatform: AptoPlatformProtocol = mock()

    @BeforeEach
    fun setup() {
        repo = spy(
            StatementRepositoryImpl(
                fileDownloader,
                cacheFileManager,
                TestDispatchers(dispatcher)
            )
        )
        sut = DownloadStatementLocalUseCase(repo, aptoPlatform)
    }

    @Test
    fun `when exception thrown downloading then left is returned`() = dispatcher.runBlockingTest {
        val file = mock<File>()
        whenever(cacheFileManager.createTempFile(any(), any(), any())).thenReturn(file)
        configurePlatform(monthlyStatement.right())
        whenever(fileDownloader.downloadFile(URL, file)).thenThrow(RuntimeException())

        val result = sut.run(statement)

        assertTrue(result.isLeft)
        verify(fileDownloader).downloadFile(URL, file)
    }

    @Test
    fun `when preconditions are Ok then file is correctly downloaded`() = dispatcher.runBlockingTest {
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
