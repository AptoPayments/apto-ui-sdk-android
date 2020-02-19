package com.aptopayments.sdk.features.oauth.connect

import com.aptopayments.sdk.core.platform.BaseFragment

internal class OAuthConnectFragmentDouble(override var delegate: OAuthConnectContract.Delegate?) :
        BaseFragment(),
        OAuthConnectContract.View
{
    override fun layoutId(): Int = 0
    override fun backgroundColor() = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
    override fun reloadStatus() {}
}
