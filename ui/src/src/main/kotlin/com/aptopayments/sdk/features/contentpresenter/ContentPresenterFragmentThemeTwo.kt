package com.aptopayments.sdk.features.contentpresenter

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.content.Content
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import kotlinx.android.synthetic.main.fragment_content_presenter_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import java.lang.reflect.Modifier

private const val CONTENT_KEY = "CONTENT"
private const val TITLE_KEY = "TITLE"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
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
        super.onBackPressed()
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
        activity?.window?.let { themeManager().customizeSecondaryNavigationStatusBar(it) }
    }

    private fun setupToolbar() {
        tb_llsdk_toolbar.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        tb_llsdk_toolbar.setTitleTextColor(UIConfig.iconTertiaryColor)
        delegate?.configureToolbar(
                toolbar = tb_llsdk_toolbar,
                title = title,
                backButtonMode = BaseActivity.BackButtonMode.Close(null, UIConfig.iconTertiaryColor)
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
