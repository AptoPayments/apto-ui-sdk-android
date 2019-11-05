package com.aptopayments.sdk.ui.views

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RelativeLayout
import com.aptopayments.sdk.R
import com.aptopayments.core.data.content.Content
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.core.extension.remove
import com.aptopayments.sdk.core.extension.show
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.contentpresenter.ContentPresenterContract
import com.aptopayments.sdk.utils.ViewUtils.isSmallScreen
import kotlinx.android.synthetic.main.view_content_presenter.view.*
import kotlinx.android.synthetic.main.view_native_content.view.*

class ContentPresenterView : RelativeLayout, NativeContentContract.Delegate, AptoWebViewDelegate {
    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
            : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        inflate(context, R.layout.view_content_presenter, this)
        setUpWebView()
    }

    private fun setUpWebView() {
        wb_content_web.webViewClient = WebViewClient()
    }

    var delegate: ContentPresenterContract.ViewActions? = null

    var content: Content? = null
        set(value) {
            field = value
            updateContent()
        }

    private fun updateContent() {
        // This is needed because content is mutable and the compiler complain when we try to use
        // it the is cases about a possible value change
        val currentContent = content
        when(currentContent) {
            is Content.Markdown -> showMarkdown(currentContent.markdown)
            is Content.PlainText -> showPlainText(currentContent.text)
            is Content.Web -> showWebPage(currentContent.url.toString())
            is Content.Native -> showNativeContent(currentContent)
            null -> hideAllContent()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showNativeContent(content: Content.Native) {
        vw_native_content.delegate = this
        if (isSmallScreen(context)) {
            vw_native_content.content = Content.Native(
                backgroundColor = content.backgroundColor,
                backgroundImage = content.backgroundImage,
                asset = null
            )
        } else {
            vw_native_content.content = content
        }
        tv_native_content_title.text = "disclaimer.native_content.title".localized()
        tv_native_content_description_main.text = "disclaimer.native_content.description.main".localized()
        tv_native_content_description_secondary.text = "disclaimer.native_content.description.secondary".localized()

        wb_content_web.remove()
        tv_content_text.remove()
        md_content_markdown.remove()
        vw_native_content.show()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showWebPage(url: String) {
        wb_content_web.show()
        tv_content_text.remove()
        md_content_markdown.remove()
        wb_content_web.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress == 100) delegate?.onContentLoaded()
            }
        }
        wb_content_web.settings.javaScriptEnabled = true
        wb_content_web.delegate = this
        wb_content_web.loadUrl(url)
    }

    override fun didScrollToBottom() {
        delegate?.didScrollToBottom()
    }

    private fun showPlainText(text: String) {
        wb_content_web.remove()
        tv_content_text.show()
        md_content_markdown.remove()
        tv_content_text.text = text
        themeManager().customizeContentPlainText(tv_content_text)
        delegate?.onContentLoaded()
    }

    private fun showMarkdown(markdown: String) {
        wb_content_web.remove()
        tv_content_text.remove()
        md_content_markdown.show()
        md_content_markdown.loadMarkdown(markdown)
        delegate?.onContentLoaded()
    }

    private fun hideAllContent() {
        wb_content_web.remove()
        tv_content_text.remove()
        md_content_markdown.remove()
    }

    override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
        wb_content_web.setBackgroundColor(color)
        tv_content_text.setBackgroundColor(color)
        md_content_markdown.setBackgroundColor(color)
    }

    override fun onNativeContentLoaded() {
        delegate?.onContentLoaded()
    }

    override fun onNativeContentLoadingFailed() {
        delegate?.onContentLoadingFailed()
    }
}
