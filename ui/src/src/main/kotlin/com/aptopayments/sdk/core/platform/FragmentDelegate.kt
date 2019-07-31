package com.aptopayments.sdk.core.platform

import androidx.appcompat.widget.Toolbar
import com.aptopayments.sdk.core.platform.BaseActivity.BackButtonMode

interface FragmentDelegate {

    fun configureStatusBar()

    fun configureSecondaryStatusBar()

    fun configureToolbar(
            toolbar: Toolbar,
            title: String?,
            backButtonMode: BackButtonMode)
}
