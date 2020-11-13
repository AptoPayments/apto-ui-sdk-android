package com.aptopayments.sdk.repository

internal interface UserMetadataRepository {
    var data: String?
    fun clear() {
        data = null
    }
}

internal object UserMetadataRepositoryImpl : UserMetadataRepository {
    override var data: String? = null
}
