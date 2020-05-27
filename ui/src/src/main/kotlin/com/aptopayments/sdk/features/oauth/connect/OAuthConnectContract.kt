package com.aptopayments.sdk.features.oauth.connect

import com.aptopayments.core.data.oauth.OAuthAttempt
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface OAuthConnectContract {

    interface Delegate : FragmentDelegate {
        fun onBackFromOAuthConnect()
        fun show(url: String)
        fun onOAuthSuccess(oauthAttempt: OAuthAttempt)
    }

    interface View {
        var delegate: OAuthConnectContract.Delegate?

        fun reloadStatus()
    }
}
