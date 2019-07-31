package com.aptopayments.sdk.ui.fragments.webbrowser

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface WebBrowserContract {
    interface Delegate: FragmentDelegate {
        fun onCloseWebBrowser()
        fun shouldStopLoadingAndClose(url: String): Boolean
    }

    interface View {
        var delegate: WebBrowserContract.Delegate?
    }
}
