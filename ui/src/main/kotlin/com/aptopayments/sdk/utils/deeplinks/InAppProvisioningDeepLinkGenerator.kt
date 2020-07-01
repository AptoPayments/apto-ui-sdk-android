package com.aptopayments.sdk.utils.deeplinks

import com.aptopayments.sdk.utils.StringProvider

private const val PARAMETER_CARD_ID = "cardId"

class InAppProvisioningDeepLinkGenerator(stringProvider: StringProvider) : DeepLinkIntentGenerator(stringProvider) {

    override val host = "provisioning"

    fun setCardId(cardId: String) {
        parameters[PARAMETER_CARD_ID] = cardId
    }
}
