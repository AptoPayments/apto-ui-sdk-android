package com.aptopayments.sdk.repository

interface ForceIssueCardRepository {
    var data: Boolean
    fun clear() {
        data = false
    }
}

object ForceIssueCardRepositoryImpl : ForceIssueCardRepository {
    override var data: Boolean = false
}
