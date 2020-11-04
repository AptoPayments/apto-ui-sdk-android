package com.aptopayments.sdk.data

/**
 * @property cardMetadata Metadata that will be stored in the card
 * @property userMetadata Metadata that will be stored in the user
 */
data class CardFlowMetadata(
    val cardMetadata: String? = null,
    val userMetadata: String? = null
)
