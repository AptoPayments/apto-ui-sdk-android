package com.aptopayments.sdk.utils.deeplinks

import android.content.Context

private const val PARAMETER_CARD_ID = "cardId"

class InAppProvisioningDeepLinkGenerator(context: Context, cardId: String) : DeepLinkIntentGenerator(context) {

    override val host = "provisioning"

    init {
        parameters[PARAMETER_CARD_ID] = cardId
    }
}
