package com.aptopayments.sdk.features.oauth.verify

import com.aptopayments.mobile.data.user.DataPointList
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface OAuthVerifyContract {

    interface Delegate : FragmentDelegate {
        fun onAcceptPii(updatedDataPoints: DataPointList)
        fun onBackFromOAuthVerify()
        fun onRevokedTokenError(failure: Failure.ServerError)
    }

    interface View {
        fun updateDataPoints(dataPointList: DataPointList)

        var delegate: Delegate?
    }
}
