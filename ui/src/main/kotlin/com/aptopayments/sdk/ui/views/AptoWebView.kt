package com.aptopayments.sdk.ui.views

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import kotlin.math.floor

interface AptoWebViewDelegate {
    fun didScrollToBottom()
}

internal class AptoWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : WebView(context, attrs, defStyleAttr, defStyleRes) {

    var delegate: AptoWebViewDelegate? = null

    @Suppress("DEPRECATION")
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        val height = floor((contentHeight * scale).toDouble()).toInt()
        val webViewHeight = getHeight()
        val cutoff = height - webViewHeight - 10
        if (t >= cutoff) delegate?.didScrollToBottom()
    }
}
