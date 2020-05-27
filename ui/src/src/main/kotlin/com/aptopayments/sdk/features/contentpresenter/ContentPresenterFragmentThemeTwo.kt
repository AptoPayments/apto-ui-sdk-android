package com.aptopayments.sdk.features.contentpresenter

import android.os.Bundle
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.content.Content
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.BackButtonMode
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.platform.BaseFragment
import kotlinx.android.synthetic.main.fragment_content_presenter_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*

private const val CONTENT_KEY = "CONTENT"
private const val TITLE_KEY = "TITLE"

internal class ContentPresenterFragmentThemeTwo : BaseFragment(), ContentPresenterContract.View {
    private lateinit var content: Content
    private lateinit var title: String

    override var delegate: ContentPresenterContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_content_presenter_theme_two

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setUpArguments() {
        content = arguments!![CONTENT_KEY] as Content
        title = arguments!![TITLE_KEY] as String
    }

    override fun setupViewModel() {
    }

    override fun setupUI() {
        setupContent()
        setupTheme()
        setupToolbar()
    }

    private fun setupContent() {
        showLoading()
        vw_content_presenter.delegate = this
        vw_content_presenter.content = content
    }

    override fun onBackPressed() {
        delegate?.onCloseTapped()
    }

    override fun onContentLoaded() {
        hideLoading()
    }

    override fun onContentLoadingFailed() {
        hideLoading()
    }

    override fun didScrollToBottom() {}

    private fun setupTheme() {
        vw_content_presenter.setBackgroundColor(UIConfig.uiBackgroundSecondaryColor)
        customizeSecondaryNavigationStatusBar()
    }

    private fun setupToolbar() {
        tb_llsdk_toolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .backButtonMode(BackButtonMode.Close(UIConfig.iconTertiaryColor))
                .title(title)
                .setSecondaryTertiaryColors()
                .build()
        )
    }

    companion object {
        fun newInstance(content: Content, title: String): ContentPresenterFragmentThemeTwo {
            return ContentPresenterFragmentThemeTwo().apply {
                arguments = Bundle().apply {
                    putSerializable(CONTENT_KEY, content)
                    putString(TITLE_KEY, title)
                }
            }
        }
    }
}
