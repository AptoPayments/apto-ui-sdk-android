package com.aptopayments.sdk.features.oauth.connect

import android.os.Bundle
import android.text.method.LinkMovementMethod
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.oauth.OAuthAttempt
import com.aptopayments.mobile.data.oauth.OAuthAttemptStatus.FAILED
import com.aptopayments.mobile.data.oauth.OAuthAttemptStatus.PASSED
import com.aptopayments.mobile.data.workflowaction.AllowedBalanceType
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.extension.loadFromUrl
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.oauth.OAuthConfig
import com.aptopayments.sdk.utils.MessageBanner.MessageType.ERROR
import com.aptopayments.sdk.utils.extensions.parseHtmlLinks
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_oauth_connect.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.URL

private const val TITLE_KEY = "TITLE"
private const val EXPLANATION_KEY = "EXPLANATION"
private const val CALL_TO_ACTION_KEY = "CALL_TO_ACTION"
private const val NEW_USER_ACTION_KEY = "NEW_USER_ACTION"
private const val ALLOWED_BALANCE_TYPE_KEY = "ALLOWED_BALANCE_TYPE"
private const val ASSET_URL_KEY = "ASSET_URL"
private const val ERROR_MESSAGE_KEYS_KEY = "ERROR_MESSAGE_KEYS"

internal class OAuthConnectFragment : BaseFragment(), OAuthConnectContract.View {

    override fun layoutId() = R.layout.fragment_oauth_connect
    private val viewModel: OAuthConnectViewModel by viewModel()
    override var delegate: OAuthConnectContract.Delegate? = null
    private lateinit var title: String
    private lateinit var explanation: String
    private lateinit var callToAction: String
    private lateinit var newUserAction: String
    private lateinit var allowedBalanceType: AllowedBalanceType
    private var assetUrl: String? = null
    private var errorMessageKeys: List<String>? = null
    private var oauthAttempt: OAuthAttempt? = null
    private var shouldReloadStatus = false

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    @Suppress("UNCHECKED_CAST")
    override fun setUpArguments() {
        title = requireArguments()[TITLE_KEY] as String
        explanation = requireArguments()[EXPLANATION_KEY] as String
        callToAction = requireArguments()[CALL_TO_ACTION_KEY] as String
        newUserAction = requireArguments()[NEW_USER_ACTION_KEY] as String
        allowedBalanceType = requireArguments()[ALLOWED_BALANCE_TYPE_KEY] as AllowedBalanceType
        assetUrl = requireArguments().getString(ASSET_URL_KEY)
        errorMessageKeys = requireArguments()[ERROR_MESSAGE_KEYS_KEY] as? List<String>
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(failure) { handleFailure(it) }
        }
    }

    override fun onPresented() {
        customizePrimaryNavigationStatusBar()
    }

    override fun onResume() {
        super.onResume()
        reloadStatus()
    }

    override fun setupUI() {
        setupTheme()
        setupToolbar()
        setupTexts()
        setupImageView()
    }

    override fun setupListeners() {
        super.setupListeners()
        tv_submit_bttn.setOnClickListener { _ ->
            shouldReloadStatus = true
            showLoading()
            viewModel.startOAuthAuthentication(allowedBalanceType) { oauthAttempt ->
                this.oauthAttempt = oauthAttempt
                oauthAttempt.url?.let { url ->
                    startOAuthAuthenticationWith(url = url)
                }
            }
        }
    }

    private fun setupToolbar() {
        tb_llsdk_toolbar.configure(this, ToolbarConfiguration.Builder().setPrimaryColors().build())
    }

    private fun setupTexts() {
        tv_oauth_header.localizedText = title
        tv_oauth_info.localizedText = explanation
        tv_submit_bttn.localizedText = callToAction
    }

    private fun setupImageView() {
        assetUrl?.let { image_view.loadFromUrl(it) }
    }

    private fun setupTheme() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_oauth_header)
            customizeRegularTextLabel(tv_oauth_info)
            customizeSubmitButton(tv_submit_bttn)
            customizeHtml(tv_new_user, newUserAction.localized().parseHtmlLinks())
        }
        tv_new_user.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onBackPressed() {
        delegate?.onBackFromOAuthConnect()
    }

    private fun startOAuthAuthenticationWith(url: URL) {
        hideLoading()
        delegate?.show(url = url.toString())
    }

    override fun reloadStatus() {
        if (shouldReloadStatus) {
            oauthAttempt?.let {
                shouldReloadStatus = false
                showLoading()
                viewModel.checkOAuthAuthentication(it) { oauthAttempt ->
                    hideLoading()
                    this.oauthAttempt = oauthAttempt
                    when (oauthAttempt.status) {
                        PASSED -> delegate?.onOAuthSuccess(oauthAttempt)
                        FAILED -> {
                            showOauthFailure(oauthAttempt)
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }

    private fun showOauthFailure(oauthAttempt: OAuthAttempt) {
        if (oauthAttempt.status != FAILED) {
            return
        }
        oauthAttempt.errorMessageKeys = errorMessageKeys
        notify(message = oauthAttempt.localizedErrorMessage(), type = ERROR)
    }

    companion object {
        fun newInstance(config: OAuthConfig) = OAuthConnectFragment().apply {
            this.arguments = Bundle().apply {
                putSerializable(TITLE_KEY, config.title)
                putSerializable(EXPLANATION_KEY, config.explanation)
                putSerializable(CALL_TO_ACTION_KEY, config.callToAction)
                putSerializable(NEW_USER_ACTION_KEY, config.newUserAction)
                putSerializable(ALLOWED_BALANCE_TYPE_KEY, config.allowedBalanceType)
                config.assetUrl?.let { putString(ASSET_URL_KEY, it) }
                config.errorMessageKeys?.let { putStringArrayList(ERROR_MESSAGE_KEYS_KEY, ArrayList(it)) }
            }
        }
    }
}
