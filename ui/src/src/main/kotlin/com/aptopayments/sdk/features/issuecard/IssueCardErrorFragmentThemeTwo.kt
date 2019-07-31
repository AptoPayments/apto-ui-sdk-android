package com.aptopayments.sdk.features.issuecard

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.RelativeLayout
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.loadFromUrl
import com.aptopayments.sdk.core.extension.remove
import com.aptopayments.sdk.core.extension.viewModel
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.StringUtils
import kotlinx.android.synthetic.main.fragment_issue_card_error_theme_two.*
import java.lang.reflect.Modifier

private const val ERROR_CODE_KEY = "ERROR_CODE"
private const val ERROR_ASSET_KEY = "ERROR_ASSET"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class IssueCardErrorFragmentThemeTwo : BaseFragment(), IssueCardErrorContract.View {
    override fun layoutId() = R.layout.fragment_issue_card_error_theme_two
    private var errorAsset: String? = null
    private var errorCode: Int? = null
    private lateinit var viewModel: IssueCardViewModel
    override var delegate: IssueCardErrorContract.Delegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        errorCode = arguments?.getInt(ERROR_CODE_KEY)
        errorAsset = arguments?.getString(ERROR_ASSET_KEY)
    }

    override fun setupViewModel() {
        viewModel = viewModel(viewModelFactory) {}
    }

    override fun setupUI() {
        setupTheme()
        setupTexts()
    }

    override fun setupListeners() {
        super.setupListeners()
        tv_primary_cta.setOnClickListener {
            delegate?.onRetryIssueCard()
        }
    }

    private fun setupTheme() {
        view?.setBackgroundColor(UIConfig.uiBackgroundSecondaryColor)
        with (themeManager()) {
            customizeLargeTitleLabel(tv_issue_card_error_title)
            customizeRegularTextLabel(tv_description)
            customizeFormTextLink(tv_secondary_cta)
            customizeSubmitButton(tv_primary_cta)
        }
        tv_secondary_cta.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupTexts() {
        if (Failure.ServerError(errorCode).isErrorInsufficientFunds) {
            showInsufficientFundsError()
        } else {
            showGenericError()
        }
    }

    private fun showInsufficientFundsError() {
        context?.let {
            showOrRemoveErrorAsset()
            tv_issue_card_error_title.text = "issue_card.issue_card.error_insufficient_funds.title".localized(it)
            tv_description.text = "issue_card.issue_card.error_insufficient_funds.description".localized(it)
            val title = "issue_card.issue_card.error_insufficient_funds.secondary_cta".localized(it)
            tv_secondary_cta.text = StringUtils.parseHtmlLinks(title)
            tv_primary_cta.text = "issue_card.issue_card.error_insufficient_funds.primary_cta".localized(it)
        }
    }

    private fun showGenericError() {
        context?.let {
            showOrRemoveErrorAsset()
            tv_issue_card_error_title.text = "issue_card.issue_card.generic_error.title".localized(it)
            tv_description.text = "issue_card.issue_card.generic_error.description".localized(it)
            tv_secondary_cta.remove()
            tv_primary_cta.text = "issue_card.issue_card.generic_error.primary_cta".localized(it)
        }
    }

    private fun showOrRemoveErrorAsset() {
        if (errorAsset.isNullOrEmpty()) {
            iv_error_asset.remove()
            val layoutParams = tv_issue_card_error_title.layoutParams as RelativeLayout.LayoutParams
            layoutParams.removeRule(RelativeLayout.BELOW)
        } else {
            errorAsset?.let { iv_error_asset.loadFromUrl(it) }
        }
    }

    override fun viewLoaded() {
        viewModel.trackErrorCode(errorCode)
    }

    companion object {
        fun newInstance(errorCode: Int?, errorAsset: String?) = IssueCardErrorFragmentThemeTwo().apply {
            arguments = Bundle().apply {
                putSerializable(ERROR_CODE_KEY, errorCode)
                putString(ERROR_ASSET_KEY, errorAsset)
            }
        }
    }
}
