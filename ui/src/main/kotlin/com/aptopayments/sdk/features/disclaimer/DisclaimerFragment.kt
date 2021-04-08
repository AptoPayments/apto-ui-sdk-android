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
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import kotlinx.android.synthetic.main.fragment_disclaimer.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.Serializable

private const val CONTENT_KEY = "CONTENT"
private const val CONFIGURATION_KEY = "CONFIGURATION"

internal class DisclaimerFragment :
    BaseFragment(),
    DisclaimerContract.View,
    ContentPresenterContract.ViewActions {
    private lateinit var content: Content
    private lateinit var configuration: Configuration

    private val viewModel: DisclaimerViewModel by viewModel()

    override var delegate: DisclaimerContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_disclaimer

    override fun backgroundColor(): Int = UIConfig.disclaimerBackgroundColor

    override fun setUpArguments() {
        content = requireArguments()[CONTENT_KEY] as Content
        configuration = requireArguments()[CONFIGURATION_KEY] as Configuration
    }

    override fun setupViewModel() {
        // do nothing
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
        tv_accept_disclaimer.text = configuration.screenAcceptAgreement.localized()
        tv_disclaimer_title.text = configuration.screenTitle.localized()
        tv_reject_disclaimer.text = configuration.screenRejectAgreement.localized()
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
        tv_accept_disclaimer.setOnClickListenerSafe { delegate?.onDisclaimerAccepted() }
        tv_reject_disclaimer.setOnClickListenerSafe {
            confirm(
                title = configuration.alertTitle.localized(),
                text = configuration.alertText.localized(),
                confirm = "disclaimer.disclaimer.cancel_action.ok_button".localized(),
                cancel = "disclaimer.disclaimer.cancel_action.cancel_button".localized(),
                onConfirm = { delegate?.onDisclaimerDeclined() },
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

    data class Configuration(
        val screenTitle: String = "disclaimer_disclaimer_title",
        val screenAcceptAgreement: String = "disclaimer_disclaimer_call_to_action_title",
        val screenRejectAgreement: String = "disclaimer_disclaimer_cancel_action_button",
        val alertTitle: String = "disclaimer.disclaimer.cancel_action.title",
        val alertText: String = "disclaimer.disclaimer.cancel_action.message"
    ) : Serializable

    companion object {
        fun newInstance(content: Content, configuration: Configuration) = DisclaimerFragment().apply {
            arguments = Bundle().apply {
                putSerializable(CONTENT_KEY, content)
                putSerializable(CONFIGURATION_KEY, configuration)
            }
        }
    }
}
