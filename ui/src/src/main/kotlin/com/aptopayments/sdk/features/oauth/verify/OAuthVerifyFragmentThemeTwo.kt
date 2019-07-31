package com.aptopayments.sdk.features.oauth.verify

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.user.DataPointList
import com.aptopayments.core.data.workflowaction.AllowedBalanceType
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_oauth_verify_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import java.lang.reflect.Modifier

private const val DATA_POINTS_KEY = "DATA_POINTS"
private const val ALLOWED_BALANCE_TYPE_KEY = "ALLOWED_BALANCE_TYPE"
private const val TOKEN_ID_KEY = "OAUTH_TOKEN_ID"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class OAuthVerifyFragmentThemeTwo: BaseFragment(), OAuthVerifyContract.View {

    override fun layoutId() = R.layout.fragment_oauth_verify_theme_two
    private lateinit var viewModel: OAuthVerifyViewModel
    override var delegate: OAuthVerifyContract.Delegate? = null
    private lateinit var dataPoints: DataPointList
    private lateinit var allowedBalanceType: AllowedBalanceType
    private lateinit var tokenId: String
    private var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataPoints = arguments!![DATA_POINTS_KEY] as DataPointList
        allowedBalanceType = arguments!![ALLOWED_BALANCE_TYPE_KEY] as AllowedBalanceType
        tokenId = arguments!![TOKEN_ID_KEY] as String
    }

    override fun onPresented() {
        delegate?.configureStatusBar()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_update_personal_details, menu)
        setupMenuItem(menu, R.id.menu_update_personal_details)
        if(this.menu == null) this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_update_personal_details) {
            showLoading()
            viewModel.retrieveUpdatedUserData(allowedBalanceType, tokenId) {
                it.userData?.let { dataPointList -> dataPoints = dataPointList }
                hideLoading()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setupViewModel() {
        viewModel = viewModel(viewModelFactory) {
            observe(firstName) { updateRow(tv_first_name_label, tv_first_name, it) }
            observe(lastName) { updateRow(tv_last_name_label, tv_last_name, it) }
            observe(email) { updateRow(tv_email_label, tv_email, it) }
            observe(address) { updateRow(tv_address_label, tv_address, it) }
            observe(phone) { updateRow(tv_phone_label, tv_phone, it) }
            observe(birthdate) { updateRow(tv_date_of_birth_label, tv_date_of_birth, it) }
            failure(failure) {
                if (it is Failure.ServerError && it.isOauthTokenRevokedError()) delegate?.onRevokedTokenError(it)
                else handleFailure(it)
            }
        }
        viewModel.setDataPoints(dataPoints)
    }

    override fun viewLoaded() {
        viewModel.viewLoaded()
    }

    private fun updateRow(title: TextView, content: TextView, value: String?) {
        if (value != null) title.show() else title.remove()
        if (value != null) content.show() else content.remove()
        content.text = value
    }

    override fun setupUI() {
        setupToolbar()
        setupTheme()
        setupTexts()
    }

    override fun setupListeners() {
        super.setupListeners()
        tv_submit_bttn.setOnClickListener { delegate?.onAcceptPii(dataPoints) }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        delegate?.onBackFromOAuthVerify()
    }

    override fun updateDataPoints(dataPointList: DataPointList) {
        dataPoints = dataPointList
        viewModel.setDataPoints(dataPointList)
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
            tv_personal_information_header.text = "select_balance_store_oauth_confirm_title".localized(it)
            tv_personal_information_description.text = "select_balance_store_oauth_confirm_explanation".localized(it)
            tv_first_name_label.text = "select_balance_store_oauth_confirm_first_name".localized(it)
            tv_last_name_label.text = "select_balance_store_oauth_confirm_last_name".localized(it)
            tv_email_label.text = "select_balance_store_oauth_confirm_email".localized(it)
            tv_address_label.text = "select_balance_store_oauth_confirm_address".localized(it)
            tv_phone_label.text = "select_balance_store_oauth_confirm_phone_number".localized(it)
            tv_date_of_birth_label.text = "select_balance_store_oauth_confirm_birth_date".localized(it)
            tv_delivery_address_instructions.text = "select_balance_store_oauth_confirm_footer".localized(it)
            tv_delivery_address_text_link.text = "select_balance_store_oauth_confirm_footer_link".localized(it)
            tv_submit_bttn.text = "select_balance_store_oauth_confirm_call_to_action_title".localized(it)
        }
    }

    private fun setupTheme() {
        view?.setBackgroundColor(UIConfig.uiBackgroundPrimaryColor)
        context?.let {
            styleMenuItem(it)
        }
        with (themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_personal_information_header)
            customizeFormLabel(tv_personal_information_description)
            customizeSubmitButton(tv_submit_bttn)
            customizeSectionHeader(tv_first_name_label)
            customizeFormLabel(tv_first_name)
            customizeSectionHeader(tv_last_name_label)
            customizeFormLabel(tv_last_name)
            customizeSectionHeader(tv_email_label)
            customizeFormLabel(tv_email)
            customizeSectionHeader(tv_address_label)
            customizeFormLabel(tv_address)
            customizeSectionHeader(tv_phone_label)
            customizeFormLabel(tv_phone)
            customizeSectionHeader(tv_date_of_birth_label)
            customizeFormLabel(tv_date_of_birth)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun styleMenuItem(context: Context) {
        val refreshIcon = ContextCompat.getDrawable(context, R.drawable.ic_refresh_button)
        refreshIcon?.setTint(UIConfig.uiPrimaryColor)
        menu?.findItem(R.id.menu_update_personal_details)?.icon = refreshIcon
        tb_llsdk_toolbar.post {
            tb_llsdk_toolbar.findViewById<TextView>(R.id.tv_menu_update_personal_details)?.let {
                themeManager().customizeMenuItem(it)
                it.text = "select_balance_store_oauth_confirm_refresh_title".localized(context)
                it.setBackgroundColorKeepShape(UIConfig.uiPrimaryColor)
                it.setTextColor(UIConfig.textButtonColor)
            }
        }
    }

    companion object {
        fun newInstance(dataPointList: DataPointList, allowedBalanceType: AllowedBalanceType,
                        tokenId: String): OAuthVerifyFragmentThemeTwo {
            return OAuthVerifyFragmentThemeTwo().apply {
                this.arguments = Bundle().apply {
                    putSerializable(DATA_POINTS_KEY, dataPointList)
                    putSerializable(ALLOWED_BALANCE_TYPE_KEY, allowedBalanceType)
                    putString(TOKEN_ID_KEY, tokenId)
                }
            }
        }
    }
}
