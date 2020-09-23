package com.aptopayments.sdk.features.disclaimer

import android.os.Bundle
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.loadFromUrl
import com.aptopayments.sdk.core.extension.remove
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.contentpresenter.ContentPresenterContract
import kotlinx.android.synthetic.main.fragment_disclaimer.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val CONTENT_KEY = "CONTENT"

internal class DisclaimerFragment : BaseFragment(), DisclaimerContract.View,
    ContentPresenterContract.ViewActions {
    private lateinit var content: Content
    private val viewModel: DisclaimerViewModel by viewModel()

    override var delegate: DisclaimerContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_disclaimer

    override fun backgroundColor(): Int = UIConfig.disclaimerBackgroundColor

    override fun setUpArguments() {
        content = requireArguments()[CONTENT_KEY] as Content
    }

    override fun setupViewModel() {
    }

    override fun setupUI() {
        setUpTheme()
        showLoading()
        setUpContent()
        hideKeyboard()
    }

    private fun setUpContent() {
        vw_content_presenter.delegate = this
        vw_content_presenter.content = content
        if (content is Content.Web) tv_accept_disclaimer.isEnabled = false
        if (content is Content.Native) {
            (content as Content.Native).backgroundColor?.let { view?.setBackgroundColor(it) }
            tv_disclaimer_title.remove()
            (content as Content.Native).backgroundImage?.let { iv_background.loadFromUrl(it.toString()) }
        }
    }

    private fun setUpTheme() {
        with(themeManager()) {
            customizeLargeTitleLabel(tv_disclaimer_title)
            customizeSubmitButton(tv_accept_disclaimer)
            customizeFormTextLink(tv_reject_disclaimer)
        }
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
        fun newInstance(content: Content) = DisclaimerFragment().apply {
            arguments = Bundle().apply { putSerializable(CONTENT_KEY, content) }
        }
    }
}
