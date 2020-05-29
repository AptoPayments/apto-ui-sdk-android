package com.aptopayments.sdk.repository

interface IssueCardAdditionalFieldsRepository {
    fun set(fields: Map<String, Any>?)
    fun get(): Map<String, Any>?
}

object IssueCardAdditionalFieldsRepositoryImpl : IssueCardAdditionalFieldsRepository {

    var fields: Map<String, Any>? = null

    override fun set(fields: Map<String, Any>?) {
        this.fields = fields
    }

    override fun get() = fields
}
