package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.data.statements.MonthlyStatement
import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.flatMap
import com.aptopayments.mobile.functional.left
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.features.card.statements.detail.ExternalFileDownloader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class DownloadStatementExternalUseCase(
    private val aptoPlatformProtocol: AptoPlatformProtocol,
    private val downloader: ExternalFileDownloader
) : UseCaseAsync<Unit, StatementMonth>() {

    override suspend fun run(params: StatementMonth): Either<Failure, Unit> {
        return fetch(params).flatMap { monthlyStatement: MonthlyStatement ->
            if (monthlyStatement.downloadUrl != null && monthlyStatement.canDownload()) {
                downloader.download(
                    getFilename(params),
                    monthlyStatement.downloadUrl!!
                )
            } else {
                StatementDownloadFailure().left()
            }
        }
    }

    private fun getFilename(params: StatementMonth) = "Statement-${params.year}-${params.month}.pdf"

    private suspend fun fetch(statement: StatementMonth) = suspendCoroutine<Either<Failure, MonthlyStatement>> { cont ->
        aptoPlatformProtocol.fetchMonthlyStatement(statement.month, statement.year) { result ->
            cont.resume(result)
        }
    }
}
