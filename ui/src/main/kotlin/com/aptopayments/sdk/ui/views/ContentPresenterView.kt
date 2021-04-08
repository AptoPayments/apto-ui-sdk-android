package com.aptopayments.sdk.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RelativeLayout
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.remove
import com.aptopayments.sdk.core.extension.show
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.contentpresenter.ContentPresenterContract
import com.aptopayments.sdk.utils.MarkDownBuilder
import com.aptopayments.sdk.utils.ViewUtils.isSmallScreen
import kotlinx.android.synthetic.main.view_content_presenter.view.*
import kotlinx.android.synthetic.main.view_native_content.view.*

class ContentPresenterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), NativeContentContract.Delegate, AptoWebViewDelegate {

    private val markdownProcessor by lazy { MarkDownBuilder().build(context) }

    init {
        inflate(context, R.layout.view_content_presenter, this)
        configureUi()
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

    private fun configureUi() {
        themeManager().customizeContentPlainText(md_content_markdown)
    }

    private fun updateContent() {
        // This is needed because content is mutable and the compiler complain when we try to use
        // it the is cases about a possible value change
        when (val currentContent = content) {
            is Content.Markdown -> showMarkdown(currentContent.markdown)
            is Content.PlainText -> showPlainText(currentContent.text)
            is Content.Web -> showWebPage(currentContent.url.toString())
            is Content.Native -> showNativeContent(currentContent)
            null -> hideAllContent()
        }
    }

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
        tv_native_content_title.localizedText = "disclaimer.native_content.title"
        tv_native_content_description_main.localizedText = "disclaimer.native_content.description.main"
        tv_native_content_description_secondary.localizedText = "disclaimer.native_content.description.secondary"

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
        wb_content_web.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                delegate?.onContentLoaded()
                checkScrolledToBottom(view)
            }

            private fun checkScrolledToBottom(view: WebView?) {
                if ((view?.contentHeight ?: 0) < wb_content_web.height) {
                    delegate?.didScrollToBottom()
                }
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
        markdownProcessor.setMarkdown(md_content_markdown, markdown)
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
