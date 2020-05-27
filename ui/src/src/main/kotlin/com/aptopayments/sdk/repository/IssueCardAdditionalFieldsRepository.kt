package com.aptopayments.sdk.repository

import com.aptopayments.core.data.card.IssueCardAdditionalFields

interface IssueCardAdditionalFieldsRepository {
    fun set(fields: IssueCardAdditionalFields)
    fun get(): IssueCardAdditionalFields?
}

object IssueCardAdditionalFieldsRepositoryImpl : IssueCardAdditionalFieldsRepository {

    var fields: IssueCardAdditionalFields? = null

    override fun set(fields: IssueCardAdditionalFields) {
        this.fields = fields
    }

    override fun get() = fields
}
