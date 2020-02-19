package com.aptopayments.sdk.features.disclaimer

import android.os.Bundle
import android.widget.RelativeLayout
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.content.Content
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.contentpresenter.ContentPresenterContract
import kotlinx.android.synthetic.main.fragment_disclaimer_theme_one.*
import kotlinx.android.synthetic.main.fragment_disclaimer_theme_two.tv_accept_disclaimer
import kotlinx.android.synthetic.main.fragment_disclaimer_theme_two.tv_reject_disclaimer
import kotlinx.android.synthetic.main.fragment_disclaimer_theme_two.vw_content_presenter
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val CONTENT_KEY = "CONTENT"

internal class DisclaimerFragmentThemeOne : BaseFragment(), DisclaimerContract.View, ContentPresenterContract.ViewActions {
    private lateinit var content: Content
    private val viewModel: DisclaimerViewModel by viewModel()

    override var delegate: DisclaimerContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_disclaimer_theme_one

    override fun backgroundColor(): Int = UIConfig.disclaimerBackgroundColor

    override fun setUpArguments() {
        content = arguments!![CONTENT_KEY] as Content
    }

    override fun setupViewModel() {
    }

    override fun setupUI() {
        setUpTheme()
        showLoading()
        setUpContent()
    }

    private fun setUpContent() {
        vw_content_presenter.delegate = this
        vw_content_presenter.content = content
        if (content is Content.Web) {
            tv_accept_disclaimer.isEnabled = false
        }
        if (content is Content.Native) {
            val layoutParams = vw_content_presenter.layoutParams as RelativeLayout.LayoutParams
            layoutParams.removeRule(RelativeLayout.ABOVE)
        }
    }

    private fun setUpTheme() {
        activity?.window?.let { themeManager().customizeStatusBar(it) }
        vw_content_presenter.setBackgroundColor(UIConfig.uiBackgroundPrimaryColor)
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout)
            customizeSubmitButton(tv_accept_disclaimer)
            customizeFormTextLink(tv_reject_disclaimer)
        }
        tv_toolbar_title.setTextColor(UIConfig.textTopBarPrimaryColor)
        tb_llsdk_toolbar_layout.setBackgroundColor(UIConfig.uiTertiaryColor)
    }

    override fun setupListeners() {
        super.setupListeners()
        tv_accept_disclaimer.setOnClickListener { delegate?.onDisclaimerAccepted() }
        tv_reject_disclaimer.setOnClickListener {
            confirm(
                title = "disclaimer.disclaimer.cancel_action.title".localized(),
                text = "disclaimer.disclaimer.cancel_action.message".localized(),
                confirm = "disclaimer.disclaimer.cancel_action.ok_button".localized(),
                cancel = "disclaimer.disclaimer.cancel_action.cancel_button".localized(),
                onConfirm = { delegate?.onDisclaimerRejected() },
                onCancel = { }
            )
        }
    }

    override fun onContentLoaded() {
        hideLoading()
    }

    override fun onContentLoadingFailed() {
        hideLoading()
    }

    override fun didScrollToBottom() {
        tv_accept_disclaimer.isEnabled = true
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    companion object {
        fun newInstance(content: Content) = DisclaimerFragmentThemeOne().apply {
            arguments = Bundle().apply { putSerializable(CONTENT_KEY, content) }
        }
    }
}
