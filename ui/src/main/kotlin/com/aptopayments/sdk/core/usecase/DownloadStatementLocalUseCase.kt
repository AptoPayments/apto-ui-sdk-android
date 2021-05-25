package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.data.statements.MonthlyStatement
import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.*
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.repository.StatementRepository
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class DownloadStatementLocalUseCase(
    private val repo: StatementRepository,
    private val aptoPlatformProtocol: AptoPlatformProtocol
) : UseCaseAsync<File, StatementMonth>() {

    override suspend fun run(params: StatementMonth): Either<Failure, File> {
        return fetch(params).flatMapSuspending { statement ->
            repo.clearCache()
            repo.download(statement).either(
                {
                    StatementDownloadFailure().left()
                },
                {
                    it.right()
                }
            )
        }
    }

    private suspend fun fetch(statement: StatementMonth) = suspendCoroutine<Either<Failure, MonthlyStatement>> { cont ->
        aptoPlatformProtocol.fetchMonthlyStatement(statement.month, statement.year) { result ->
            cont.resume(result)
        }
    }
}

internal class StatementDownloadFailure : Failure.FeatureFailure("monthly_statements_report_error")
