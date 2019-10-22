package com.aptopayments.sdk.features.oauth.connect

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuInflater
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.oauth.OAuthAttempt
import com.aptopayments.core.data.oauth.OAuthAttemptStatus.FAILED
import com.aptopayments.core.data.oauth.OAuthAttemptStatus.PASSED
import com.aptopayments.core.data.workflowaction.AllowedBalanceType
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.extension.loadFromUrl
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.oauth.OAuthConfig
import com.aptopayments.sdk.utils.MessageBanner.MessageType.ERROR
import com.aptopayments.sdk.utils.StringUtils
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_oauth_connect_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier
import java.net.URL

private const val TITLE_KEY = "TITLE"
private const val EXPLANATION_KEY = "EXPLANATION"
private const val CALL_TO_ACTION_KEY = "CALL_TO_ACTION"
private const val NEW_USER_ACTION_KEY = "NEW_USER_ACTION"
private const val ALLOWED_BALANCE_TYPE_KEY = "ALLOWED_BALANCE_TYPE"
private const val ASSET_URL_KEY = "ASSET_URL"
private const val ERROR_MESSAGE_KEYS_KEY = "ERROR_MESSAGE_KEYS"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class OAuthConnectFragmentThemeTwo: BaseFragment(), OAuthConnectContract.View {

    override fun layoutId() = R.layout.fragment_oauth_connect_theme_two
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        title = arguments!![TITLE_KEY] as String
        explanation = arguments!![EXPLANATION_KEY] as String
        callToAction = arguments!![CALL_TO_ACTION_KEY] as String
        newUserAction = arguments!![NEW_USER_ACTION_KEY] as String
        allowedBalanceType = arguments!![ALLOWED_BALANCE_TYPE_KEY] as AllowedBalanceType
        assetUrl = arguments!!.getString(ASSET_URL_KEY)
        errorMessageKeys = arguments!![ERROR_MESSAGE_KEYS_KEY] as? List<String>
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun setupViewModel() {
        viewModel.apply {
            failure(failure) { handleFailure(it) }
        }
    }

    override fun onPresented() {
        delegate?.configureStatusBar()
    }

    override fun setupUI() {
        setupTheme()
        setupToolbar()
        setupTexts()
        setupImageView()
    }

    override fun onResume() {
        super.onResume()
        reloadStatus()
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    override fun setupListeners() {
        super.setupListeners()
        tv_submit_bttn.setOnClickListener { _ ->
            allowedBalanceType?.let {
                showLoading()
                viewModel.startOAuthAuthentication(it) { oauthAttempt ->
                    this.oauthAttempt = oauthAttempt
                    oauthAttempt.url?.let { url ->
                        startOAuthAuthenticationWith(url = url)
                    }
                }
            }
        }
    }

    private fun setupToolbar() = delegate?.configureToolbar(
            toolbar = tb_llsdk_toolbar,
            title = null,
            backButtonMode = BaseActivity.BackButtonMode.Back(null)
    )

    @SuppressLint("SetTextI18n")
    private fun setupTexts() = context?.let {
        tv_coinbase_header.text = title.localized(it)
        tv_coinbase_info.text = explanation.localized(it)
        tv_submit_bttn.text = callToAction.localized(it)
    }

    private fun setupImageView() {
        assetUrl?.let { image_view.loadFromUrl(it) }
    }

    private fun setupTheme() {
        view?.setBackgroundColor(UIConfig.uiBackgroundPrimaryColor)
        with (themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_coinbase_header)
            customizeRegularTextLabel(tv_coinbase_info)
            customizeSubmitButton(tv_submit_bttn)
            context?.let { context ->
                customizeHtml(tv_new_user, StringUtils.parseHtmlLinks(newUserAction.localized(context)))
            }
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
        oauthAttempt?.let {
            showLoading()
            viewModel.checkOAuthAuthentication(it) { oauthAttempt ->
                hideLoading()
                this.oauthAttempt = oauthAttempt
                when(oauthAttempt.status) {
                    PASSED -> delegate?.onOAuthSuccess(oauthAttempt)
                    FAILED -> { showOauthFailure(oauthAttempt) }
                    else -> {}
                }
            }
        }
    }

    private fun showOauthFailure(oauthAttempt: OAuthAttempt) {
        if (oauthAttempt.status != FAILED) { return }
        oauthAttempt.errorMessageKeys = errorMessageKeys
        context?.let { notify(message = oauthAttempt.localizedErrorMessage(it), type = ERROR) }
    }

    companion object {
        fun newInstance(config: OAuthConfig) = OAuthConnectFragmentThemeTwo().apply {
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
