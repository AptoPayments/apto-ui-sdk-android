package com.aptopayments.sdk.data

/**
 * @param userMetadata: A string up to 256 characters that will be attached to the user after signing up.
 * @param cardMetadata: A string up to 256 characters that will be attached to the card after issuance.
 * @param custodianUid: A string up to 256 characters that will be attached to the user after signing up
 * referring to the user ID in the external platform.

 */
data class InitializationData(
    var userMetadata: String? = null,
    var cardMetadata: String? = null,
    var custodianUid: String? = null,
)
