package com.aptopayments.sdk.data

import com.aptopayments.mobile.data.card.IssueCardDesign

/**
 * @param userMetadata: A string up to 256 characters that will be attached to the user after signing up.
 * @param cardMetadata: A string up to 256 characters that will be attached to the card after issuance.
 * @param custodianUid: A string up to 256 characters that will be attached to the user after signing up.
 * @param design: A [IssueCardDesign] object containing additional Design configurations.
 * referring to the user ID in the external platform.

 */
data class InitializationData(
    var userMetadata: String? = null,
    var cardMetadata: String? = null,
    var custodianUid: String? = null,
    var design: IssueCardDesign? = null,
)
