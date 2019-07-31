package com.aptopayments.sdk.ui.views

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebView

interface AptoWebViewDelegate {
    fun didScrollToBottom()
}

class AptoWebView: WebView {
    var delegate: AptoWebViewDelegate? = null

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
            : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        val height = Math.floor((contentHeight * scale).toDouble()).toInt()
        val webViewHeight = getHeight()
        val cutoff = height - webViewHeight - 10
        if (t >= cutoff) delegate?.didScrollToBottom()
    }
}
