package com.aptopayments.sdk.features.disclaimer

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.content.Content
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.loadFromUrl
import com.aptopayments.sdk.core.extension.remove
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.contentpresenter.ContentPresenterContract
import kotlinx.android.synthetic.main.fragment_disclaimer_theme_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier

private const val CONTENT_KEY = "CONTENT"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class DisclaimerFragmentThemeTwo : BaseFragment(), DisclaimerContract.View,
        ContentPresenterContract.ViewActions {
    private lateinit var content: Content
    private val viewModel: DisclaimerViewModel by viewModel()

    override var delegate: DisclaimerContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_disclaimer_theme_two

    override fun setUpArguments() {
        content = arguments!![CONTENT_KEY] as Content
    }

    override fun setupViewModel() {
    }

    override fun setupUI() {
        setUpTheme()
        localizeStrings()
        showLoading()
        setUpContent()
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
        view?.setBackgroundColor(UIConfig.disclaimerBackgroundColor)
        with(themeManager()) {
            customizeLargeTitleLabel(tv_disclaimer_title)
            customizeSubmitButton(tv_accept_disclaimer)
            customizeFormTextLink(tv_reject_disclaimer)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun localizeStrings() = context?.let {
        tv_accept_disclaimer.text = "disclaimer_disclaimer_call_to_action_title".localized(it)
        tv_reject_disclaimer.text = "disclaimer_disclaimer_cancel_action_button".localized(it)
        tv_disclaimer_title.text = "disclaimer_disclaimer_title".localized(it)
    }

    override fun setupListeners() {
        super.setupListeners()
        tv_accept_disclaimer.setOnClickListener { delegate?.onDisclaimerAccepted() }
        tv_reject_disclaimer.setOnClickListener {
            context?.let { context ->
                confirm(
                        title="disclaimer.disclaimer.cancel_action.title".localized(context),
                        text = "disclaimer.disclaimer.cancel_action.message".localized(context),
                        confirm = "disclaimer.disclaimer.cancel_action.ok_button".localized(context),
                        cancel = "disclaimer.disclaimer.cancel_action.cancel_button".localized(context),
                        onConfirm = { delegate?.onDisclaimerRejected() },
                        onCancel = { }
                )
            }
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
        fun newInstance(content: Content) = DisclaimerFragmentThemeTwo().apply {
            arguments = Bundle().apply { putSerializable(CONTENT_KEY, content) }
        }
    }
}
