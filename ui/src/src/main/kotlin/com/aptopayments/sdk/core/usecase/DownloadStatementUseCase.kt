package com.aptopayments.sdk.core.usecase

import com.aptopayments.core.data.statements.MonthlyStatement
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.extension.localized
import com.aptopayments.core.functional.Either
import com.aptopayments.sdk.core.extension.monthLocalized
import com.aptopayments.sdk.data.StatementFile
import com.aptopayments.sdk.repository.StatementRepository

internal class DownloadStatementUseCase(private val repo: StatementRepository) :
    UseCaseAsync<StatementFile, DownloadStatementUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, StatementFile> {
        repo.clearCache()
        return if (params.statement.canDownload()) {
            val file = repo.download(params.statement)
            file.either({ StatementDownloadFailure() }) { setTitleToStatementFile(params.statement, it) }
            file
        } else {
            Either.Left(StatementExpiredFailure())
        }
    }

    class Params(val statement: MonthlyStatement)

    private fun setTitleToStatementFile(statement: MonthlyStatement, file: StatementFile) {
        file.title = getDownloadTitle(statement)
    }

    private fun getDownloadTitle(statement: MonthlyStatement): String {
        return "monthly_statements_report_title".localized()
            .replace("<<MONTH>>", statement.getLocalDate().monthLocalized())
            .replace("<<YEAR>>", statement.year.toString())
    }

    class StatementDownloadFailure : Failure.FeatureFailure("monthly_statements_report_error")
    class StatementExpiredFailure : Failure.FeatureFailure("monthly_statements_report_error_url_expired_message")
}
