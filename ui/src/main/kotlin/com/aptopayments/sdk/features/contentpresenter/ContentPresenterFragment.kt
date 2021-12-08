package com.aptopayments.sdk.features.contentpresenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.sdk.core.extension.BackButtonMode
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.platform.BaseViewBindingFragment
import com.aptopayments.sdk.databinding.FragmentContentPresenterBinding

private const val CONTENT_KEY = "CONTENT"
private const val TITLE_KEY = "TITLE"

internal class ContentPresenterFragment :
    BaseViewBindingFragment<FragmentContentPresenterBinding>(), ContentPresenterContract.View {
    private lateinit var content: Content
    private lateinit var title: String

    override var delegate: ContentPresenterContract.Delegate? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentContentPresenterBinding.inflate(inflater, container, false)

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setUpArguments() {
        content = requireArguments()[CONTENT_KEY] as Content
        title = requireArguments()[TITLE_KEY] as String
    }

    override fun setupViewModel() {
        // do nothing
    }

    override fun setupUI() {
        setupContent()
        setupTheme()
        setupToolbar()
    }

    private fun setupContent() {
        showLoading()
        with(binding.vwContentPresenter) {
            delegate = this@ContentPresenterFragment
            this.content = this@ContentPresenterFragment.content
        }
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

    override fun didScrollToBottom() {
        // do nothing
    }

    private fun setupTheme() {
        binding.vwContentPresenter.setBackgroundColor(UIConfig.uiBackgroundSecondaryColor)
        customizeSecondaryNavigationStatusBar()
    }

    private fun setupToolbar() {
        binding.toolbar.tbLlsdkToolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .backButtonMode(BackButtonMode.Close(UIConfig.iconTertiaryColor))
                .title(title)
                .setSecondaryTertiaryColors()
                .build()
        )
    }

    companion object {
        fun newInstance(content: Content, title: String): ContentPresenterFragment {
            return ContentPresenterFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(CONTENT_KEY, content)
                    putString(TITLE_KEY, title)
                }
            }
        }
    }
}
