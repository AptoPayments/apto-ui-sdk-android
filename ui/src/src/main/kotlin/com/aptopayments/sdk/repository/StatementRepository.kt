package com.aptopayments.sdk.repository

import android.content.Context
import com.aptopayments.core.data.statements.MonthlyStatement
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.extension.localized
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.core.extension.monthLocalized
import com.aptopayments.sdk.data.StatementFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

internal interface StatementRepository {
    suspend fun download(statement: MonthlyStatement): Either<Failure, StatementFile>
    fun clearCache()

    class StatementDownloadFailure : Failure.FeatureFailure("monthly_statements.report.error")
    class StatementExpiredFailure :
        Failure.FeatureFailure("monthly_statements.report.error_url_expired.message")
}

private const val FILE_NAME_PREFIX = "statement_"
private const val STATEMENT_DIR = "com.aptopayments.sdk.statements"

internal class StatementRepositoryImpl(private val context: Context) :
    StatementRepository {

    override suspend fun download(statement: MonthlyStatement) = withContext(Dispatchers.IO) {
        return@withContext if (statement.canDownload()) {
            try {
                val title = getDownloadTitle(statement)
                val dir = getCacheDir()
                removeOldFiles(dir)
                val tempFile = createTempFile(dir)
                val file = downloadFile(statement.downloadUrl!!, tempFile)
                Either.Right(StatementFile(title, file))
            } catch (e: Exception) {
                Either.Left(StatementRepository.StatementDownloadFailure())
            }
        } else {
            Either.Left(StatementRepository.StatementExpiredFailure())
        }
    }

    override fun clearCache() {
        val cacheDir = getCacheDir()
        removeOldFiles(cacheDir)
    }

    private fun getDownloadTitle(statement: MonthlyStatement): String {
        return "monthly_statements.report.title"
            .localized(context)
            .replace("<<MONTH>>", statement.getLocalDate().monthLocalized())
            .replace("<<YEAR>>", statement.year.toString())
    }

    private fun getCacheDir(): File {
        val cacheDir = context.cacheDir
        val directory = File(cacheDir.absolutePath + "/$STATEMENT_DIR")
        directory.mkdir()
        return directory
    }

    private fun removeOldFiles(dir: File) = dir.listFiles().forEach { it.delete() }

    private fun createTempFile(dir: File) = File.createTempFile(FILE_NAME_PREFIX, ".pdf", dir)

    private fun downloadFile(link: String, file: File): File {
        URL(link).openStream().use { input -> FileOutputStream(file).use { output -> input.copyTo(output) } }
        return file
    }
}
