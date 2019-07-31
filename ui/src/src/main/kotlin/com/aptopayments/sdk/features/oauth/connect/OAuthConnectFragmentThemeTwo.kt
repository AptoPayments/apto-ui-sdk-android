package com.aptopayments.sdk.features.oauth.connect

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuInflater
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.oauth.OAuthAttempt
import com.aptopayments.core.data.oauth.OAuthAttemptStatus
import com.aptopayments.core.data.workflowaction.AllowedBalanceType
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.extension.viewModel
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.StringUtils
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_oauth_connect_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import java.lang.reflect.Modifier
import java.net.URL

private const val ALLOWED_BALANCE_TYPE_KEY = "ALLOWED_BALANCE_TYPE"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class OAuthConnectFragmentThemeTwo: BaseFragment(), OAuthConnectContract.View {

    override fun layoutId() = R.layout.fragment_oauth_connect_theme_two
    private lateinit var viewModel: OAuthConnectViewModel
    override var delegate: OAuthConnectContract.Delegate? = null
    var allowedBalanceType: AllowedBalanceType? = null
    private var oauthAttempt: OAuthAttempt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        allowedBalanceType = arguments!![ALLOWED_BALANCE_TYPE_KEY] as AllowedBalanceType
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun setupViewModel() {
        viewModel = viewModel(viewModelFactory) {
            failure(failure) {
                handleFailure(it)
            }
        }
    }

    override fun onPresented() {
        delegate?.configureStatusBar()
    }

    override fun setupUI() {
        setupTheme()
        setupToolbar()
        setupTexts()
    }

    override fun onResume() {
        super.onResume()
        reloadStatus()
    }

    override fun viewLoaded() {
        viewModel.viewLoaded()
    }

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

    private fun setupToolbar() {
        delegate?.configureToolbar(
                toolbar = tb_llsdk_toolbar,
                title = null,
                backButtonMode = BaseActivity.BackButtonMode.Back(null)
        )
    }

    @SuppressLint("SetTextI18n")
    private fun setupTexts() {
        context?.let {
            tv_coinbase_header.text = "select_balance_store.login.title".localized(it)
            tv_coinbase_info.text = "select_balance_store.login.explanation".localized(it)
            tv_submit_bttn.text = "select_balance_store.login.call_to_action.title".localized(it)
            val title = "select_balance_store_login_new_user_title".localized(it)
            tv_new_user.text = StringUtils.parseHtmlLinks(title)
        }
    }

    private fun setupTheme() {
        view?.setBackgroundColor(UIConfig.uiBackgroundPrimaryColor)
        with (themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_coinbase_header)
            customizeRegularTextLabel(tv_coinbase_info)
            customizeSubmitButton(tv_submit_bttn)
            customizeFormTextLink(tv_new_user)
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
                if (oauthAttempt.status == OAuthAttemptStatus.PASSED) {
                    delegate?.onOAuthSuccess(oauthAttempt)
                }
            }
        }
    }

    companion object {
        fun newInstance(allowedBalanceType: AllowedBalanceType) = OAuthConnectFragmentThemeTwo().apply {
            this.arguments = Bundle().apply { putSerializable(ALLOWED_BALANCE_TYPE_KEY, allowedBalanceType) }
        }
    }
}
