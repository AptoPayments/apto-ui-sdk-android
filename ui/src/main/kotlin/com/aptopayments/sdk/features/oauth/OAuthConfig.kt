package com.aptopayments.sdk.features.oauth

import com.aptopayments.mobile.data.workflowaction.AllowedBalanceType
import java.io.Serializable

class OAuthConfig(
    val title: String,
    val explanation: String,
    val callToAction: String,
    val newUserAction: String,
    val allowedBalanceType: AllowedBalanceType,
    val assetUrl: String?,
    val errorMessageKeys: List<String>? = null
) : Serializable
