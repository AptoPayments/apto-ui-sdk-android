package com.aptopayments.sdk.repository

interface UserMetadataRepository {
    var data: String?
    fun clear() {
        data = null
    }
}

object UserMetadataRepositoryImpl : UserMetadataRepository {
    override var data: String? = null
}
