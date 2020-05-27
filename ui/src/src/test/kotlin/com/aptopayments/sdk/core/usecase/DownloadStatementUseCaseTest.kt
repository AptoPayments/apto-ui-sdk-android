package com.aptopayments.sdk.core.usecase

import com.aptopayments.core.data.statements.MonthlyStatement
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.repository.StatementRepository
import com.aptopayments.sdk.repository.StatementRepositoryImpl
import com.aptopayments.sdk.utils.*
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.threeten.bp.ZonedDateTime
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
internal class DownloadStatementUseCaseTest : UnitTest() {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    lateinit var fileDownloader: FileDownloader

    @Mock
    lateinit var cacheFileManager: CacheFileManager

    lateinit var statement: MonthlyStatement
    lateinit var params: DownloadStatementUseCase.Params

    lateinit var repo: StatementRepository
    lateinit var sut: DownloadStatementUseCase

    @Before
    fun setup() {
        statement = spy(MonthlyStatement("1", 12, 2019, "url", ZonedDateTime.now()))
        params = DownloadStatementUseCase.Params(statement)
        repo = spy(
            StatementRepositoryImpl(
                fileDownloader,
                cacheFileManager,
                TestDispatchers(coroutineRule.testDispatcher)
            )
        )
        sut = DownloadStatementUseCase(repo)
    }

    @Test
    fun `when Usecase is run cache is cleared`() = coroutineRule.runBlockingTest {
        sut.run(params)

        verify(repo).clearCache()
    }

    @Test
    fun `when statement can't download then left is returned`() = coroutineRule.runBlockingTest {
        whenever(statement.canDownload()).thenReturn(false)

        val result = sut.run(params)

        assertTrue(result.isLeft)
    }

    @Test
    fun `when exception thrown downloading then left is returned`() = coroutineRule.runBlockingTest {
        whenever(statement.canDownload()).thenReturn(true)
        val file = mock<File>()
        whenever(cacheFileManager.createTempFile(any(), any(), any())).thenReturn(file)
        whenever(fileDownloader.downloadFile(statement.downloadUrl!!, file)).thenThrow(RuntimeException())

        val result = sut.run(params)

        assertTrue(result.isLeft)
        verify(fileDownloader).downloadFile(statement.downloadUrl!!, file)
    }

    @Test
    fun `when preconditions are Ok then file is correctly downloaded`() = coroutineRule.runBlockingTest {
        val file = mock<File>()
        whenever(cacheFileManager.createTempFile(any(), any(), any())).thenReturn(file)
        whenever(statement.canDownload()).thenReturn(true)

        val result = sut.run(params)

        assertTrue(result.isRight)
        val resultElement = (result as Either.Right).b
        verify(cacheFileManager).createTempFile(any(), any(), any())
        verify(fileDownloader).downloadFile(statement.downloadUrl!!, file)
        assertEquals(file, resultElement.file)
        assertTrue(resultElement.title.isNotEmpty())
    }
}
