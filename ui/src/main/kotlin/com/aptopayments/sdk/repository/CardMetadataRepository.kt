package com.aptopayments.sdk.repository

private const val CARD_METADATA = "INITIALIZATION_CARD_METADATA"

internal interface CardMetadataRepository {
    var data: String?
    fun clear() {
        data = null
    }
}

internal object CardMetadataRepositoryImpl : CardMetadataRepository {
    override var data: String? = null
}
