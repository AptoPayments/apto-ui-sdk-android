package com.aptopayments.sdk.features.oauth.verify

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.core.graphics.drawable.DrawableCompat
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
import com.aptopayments.sdk.utils.StringUtils
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_oauth_verify_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier

private const val DATA_POINTS_KEY = "DATA_POINTS"
private const val ALLOWED_BALANCE_TYPE_KEY = "ALLOWED_BALANCE_TYPE"
private const val TOKEN_ID_KEY = "OAUTH_TOKEN_ID"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class OAuthVerifyFragmentThemeTwo: BaseFragment(), OAuthVerifyContract.View {

    override fun layoutId() = R.layout.fragment_oauth_verify_theme_two
    private val viewModel: OAuthVerifyViewModel by viewModel()
    override var delegate: OAuthVerifyContract.Delegate? = null
    private lateinit var dataPoints: DataPointList
    private lateinit var allowedBalanceType: AllowedBalanceType
    private lateinit var tokenId: String
    private var menu: Menu? = null

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
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
        viewModel.apply {
            observe(firstName) { updateRow(tv_first_name_label, tv_first_name, it) }
            observe(lastName) { updateRow(tv_last_name_label, tv_last_name, it) }
            observe(id) { updateRow(tv_id_document_label, tv_id_document, it) }
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

    override fun viewLoaded() = viewModel.viewLoaded()

    private fun updateRow(title: TextView, content: TextView, value: String?) {
        title.goneIf(value.isNullOrEmpty())
        content.goneIf(value.isNullOrEmpty())
        content.text = value
    }

    override fun setupUI() {
        setupToolbar()
        setupTheme()
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
        tb_llsdk_toolbar.setBackgroundColor(UIConfig.uiNavigationPrimaryColor)
        delegate?.configureToolbar(
            toolbar = tb_llsdk_toolbar,
            title = null,
            backButtonMode = BaseActivity.BackButtonMode.Back(null)
        )
    }

    private fun setupTheme() {
        styleMenuItem()
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
            customizeSectionHeader(tv_id_document_label)
            customizeFormLabel(tv_id_document)
            val title = "select_balance_store_oauth_confirm_footer".localized()
            customizeHtml(tv_delivery_address_instructions, StringUtils.parseHtmlLinks(title))
        }
        tv_delivery_address_instructions.movementMethod = LinkMovementMethod.getInstance()
    }

    @SuppressLint("SetTextI18n")
    private fun styleMenuItem() {
        tb_llsdk_toolbar.post {
            tb_llsdk_toolbar.findViewById<TextView>(R.id.tv_menu_update_personal_details)?.let {
                themeManager().customizeMenuItem(it)
                it.text = "select_balance_store_oauth_confirm_refresh_title".localized()
                it.setBackgroundColorKeepShape(UIConfig.uiPrimaryColor)
                it.setTextColor(UIConfig.textButtonColor)
            }
            tb_llsdk_toolbar.findViewById<ImageView>(R.id.iv_menu_refresh)?.let {
                DrawableCompat.setTint(it.drawable, UIConfig.uiPrimaryColor)
            }
        }
    }

    companion object {
        fun newInstance(dataPointList: DataPointList, allowedBalanceType: AllowedBalanceType, tokenId: String) =
                OAuthVerifyFragmentThemeTwo().apply {
                    this.arguments = Bundle().apply {
                        putSerializable(DATA_POINTS_KEY, dataPointList)
                        putSerializable(ALLOWED_BALANCE_TYPE_KEY, allowedBalanceType)
                        putString(TOKEN_ID_KEY, tokenId)
                    }
                }
    }
}
