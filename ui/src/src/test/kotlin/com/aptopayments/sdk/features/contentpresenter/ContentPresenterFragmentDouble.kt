package com.aptopayments.sdk.features.contentpresenter

import com.aptopayments.sdk.core.platform.BaseFragment

internal class ContentPresenterFragmentDouble(override var delegate: ContentPresenterContract.Delegate?) :
        BaseFragment(),
        ContentPresenterContract.View
{
    override fun onContentLoaded() {}
    override fun onContentLoadingFailed() {}
    override fun didScrollToBottom() {}
    override fun layoutId(): Int = 0
    override fun backgroundColor() = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
