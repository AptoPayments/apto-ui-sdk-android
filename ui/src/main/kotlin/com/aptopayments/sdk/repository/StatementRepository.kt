package com.aptopayments.sdk.repository

import com.aptopayments.mobile.data.statements.MonthlyStatement
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.data.StatementFile
import com.aptopayments.sdk.utils.CacheFileManager
import com.aptopayments.sdk.utils.CoroutineDispatcherProvider
import com.aptopayments.sdk.utils.FileDownloader
import kotlinx.coroutines.withContext

private const val FILE_NAME_PREFIX = "statement_"
private const val FILE_NAME_SUFFIX = ".pdf"
private const val STATEMENT_DIR = "com.aptopayments.sdk.statements"

internal interface StatementRepository {
    suspend fun download(statement: MonthlyStatement): Either<Failure, StatementFile>
    fun clearCache()
    class StatementDownloadFailure : Failure.FeatureFailure()
}

internal class StatementRepositoryImpl(
    private val fileDownloader: FileDownloader,
    private val cacheFileManager: CacheFileManager,
    private val dispatchers: CoroutineDispatcherProvider
) : StatementRepository {

    override suspend fun download(statement: MonthlyStatement) = withContext(dispatchers.io) {
        return@withContext try {
            val file = cacheFileManager.createTempFile(FILE_NAME_PREFIX, FILE_NAME_SUFFIX, STATEMENT_DIR)
            fileDownloader.downloadFile(statement.downloadUrl!!, file)
            Either.Right(StatementFile(file))
        } catch (e: Exception) {
            Either.Left(StatementRepository.StatementDownloadFailure())
        }
    }

    override fun clearCache() {
        cacheFileManager.cleanCache(STATEMENT_DIR)
    }
}
