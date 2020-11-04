package com.aptopayments.sdk.repository

private const val CARD_METADATA = "INITIALIZATION_CARD_METADATA"

interface CardMetadataRepository {
    var data: String?
    fun clear() {
        data = null
    }
}

object CardMetadataRepositoryImpl : CardMetadataRepository {
    override var data: String? = null
}
