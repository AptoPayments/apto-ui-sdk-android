package com.aptopayments.sdk.core.extension

import android.os.Bundle

inline fun Bundle.prepareBundleArgs(func: Bundle.() -> Unit) =
        func()
