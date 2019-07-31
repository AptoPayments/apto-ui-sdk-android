package com.aptopayments.sdk.features.oauth.verify

import com.aptopayments.core.data.user.DataPointList
import com.aptopayments.core.exception.Failure
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface OAuthVerifyContract {

    interface Delegate: FragmentDelegate {
        fun onAcceptPii(updatedDataPoints: DataPointList)
        fun onBackFromOAuthVerify()
        fun onRevokedTokenError(failure: Failure.ServerError)
    }

    interface View {
        fun updateDataPoints(dataPointList: DataPointList)

        var delegate: Delegate?
    }

}
