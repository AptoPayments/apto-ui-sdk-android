package com.aptopayments.sdk.ui.views

interface NativeContentContract {
    interface Delegate {
        fun onNativeContentLoaded()
        fun onNativeContentLoadingFailed()
    }
}
