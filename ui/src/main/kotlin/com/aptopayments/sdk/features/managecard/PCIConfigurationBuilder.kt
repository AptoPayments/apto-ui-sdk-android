package com.aptopayments.sdk.features.managecard

import com.aptopayments.mobile.network.ApiKeyProvider
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.ui.views.PCIConfiguration

class PCIConfigurationBuilder {
    fun build(cardId: String) = PCIConfiguration(
        apiKey = ApiKeyProvider.apiKey,
        environment = ApiKeyProvider.environment.name.toLowerCase(),
        token = AptoPlatform.currentToken()?.token ?: "",
        cardId = cardId
    )
}
