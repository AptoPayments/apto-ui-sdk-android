package com.aptopayments.sdk.features.oauth.verify

import com.aptopayments.mobile.data.user.DataPointList
import com.aptopayments.sdk.core.platform.BaseFragment

internal class OAuthVerifyFragmentDouble(override var delegate: OAuthVerifyContract.Delegate?) :
    BaseFragment(),
    OAuthVerifyContract.View {
    override fun updateDataPoints(dataPointList: DataPointList) {}

    override fun layoutId(): Int {
        return 0
    }

    override fun backgroundColor() = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
