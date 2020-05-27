package com.aptopayments.sdk.features.contentpresenter

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface ContentPresenterContract {
    interface ViewActions {
        fun onContentLoaded()
        fun onContentLoadingFailed()
        fun didScrollToBottom()
    }

    interface View : ViewActions {
        var delegate: Delegate?
    }

    interface Delegate : FragmentDelegate {
        fun onCloseTapped()
    }
}
