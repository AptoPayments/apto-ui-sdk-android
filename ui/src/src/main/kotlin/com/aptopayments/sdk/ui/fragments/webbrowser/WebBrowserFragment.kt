package com.aptopayments.sdk.ui.fragments.webbrowser

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.aptopayments.sdk.R
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_web_browser.*
import kotlinx.android.synthetic.main.include_toolbar_two.*

private const val URL_KEY = "URL"

internal class WebBrowserFragment : BaseFragment(), WebBrowserContract.View {
    private lateinit var url: String
    private val webClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            if (delegate?.shouldStopLoadingAndClose(request.url.toString()) == true) {
                delegate?.onCloseWebBrowser()
                return true
            }
            return false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            view?.let { setupToolbar(it.title) }
        }
    }

    override var delegate: WebBrowserContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_web_browser

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setupViewModel() = Unit

    override fun setUpArguments() {
        url = arguments!![URL_KEY] as String
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let { view?.findViewById<WebView>(R.id.wb_web_view)?.restoreState(savedInstanceState) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        wb_web_view.saveState(outState)
    }

    override fun setupUI() {
        setupTheme()
        setupToolbar()
        setupWebView()
    }

    private fun setupTheme() {
        wb_web_view.setBackgroundColor(UIConfig.uiBackgroundPrimaryColor)
        themeManager().customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
    }

    private fun setupToolbar(title: String? = null) {
        tb_llsdk_toolbar?.let {
            delegate?.configureToolbar(
                    toolbar = it,
                    title = title,
                    backButtonMode = BaseActivity.BackButtonMode.Close(null)
            )
        }
    }

    private fun setupWebView() {
        wb_web_view.settings.javaScriptEnabled = true
        wb_web_view.webViewClient = webClient
    }

    override fun onPresented() {
        super.onPresented()
        wb_web_view.loadUrl(url)
    }

    override fun onBackPressed() {
        delegate?.onCloseWebBrowser()
    }

    companion object {
        fun newInstance(url: String) = WebBrowserFragment().apply {
            arguments = Bundle().apply { putString(URL_KEY, url) }
        }
    }
}
