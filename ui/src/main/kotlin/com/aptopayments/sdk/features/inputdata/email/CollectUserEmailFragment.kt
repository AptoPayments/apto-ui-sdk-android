package com.aptopayments.sdk.features.inputdata.email

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.user.EmailDataPoint
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseDataBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentCollectUserEmailBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val DATAPOINT_EMAIL = "DATAPOINT_EMAIL"

internal class CollectUserEmailFragment :
    BaseDataBindingFragment<FragmentCollectUserEmailBinding>(),
    CollectUserEmailContract.View {

    private var initialValue: EmailDataPoint? = null
    private val viewModel: CollectUserEmailViewModel by viewModel { parametersOf(initialValue) }
    override var delegate: CollectUserEmailContract.Delegate? = null

    override fun layoutId() = R.layout.fragment_collect_user_email

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
        initialValue = requireArguments()[DATAPOINT_EMAIL] as EmailDataPoint?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
    }

    override fun onPresented() {
        super.onPresented()
        binding.etEmail.requestFocus()
    }

    override fun setupUI() {
        applyFontsAndColors()
        setupToolBar(binding.tbLlsdkToolbarLayout.tbLlsdkToolbar)
        setHints()
    }

    private fun setHints() {
        binding.etEmail.hint = "collect_user_data_personal_info_email_placeholder".localized()
    }

    private fun applyFontsAndColors() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(binding.tbLlsdkToolbarLayout.tbLlsdkToolbarLayoutInternal)
            customizeLargeTitleLabel(binding.tvEmailHeader)
            customizeEditText(binding.etEmail)
            customizeSubmitButton(binding.continueButton)
        }
    }

    private fun setupToolBar(toolbar: Toolbar) {
        toolbar.configure(this, ToolbarConfiguration.Builder().setPrimaryColors().build())
    }

    override fun setupViewModel() {
        observeNotNullable(viewModel.continueNext) { delegate?.onEmailEnteredCorrectly(it) }
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onBackFromCollectEmail()
    }

    companion object {
        fun newInstance(dataPoint: EmailDataPoint?, tag: String) = CollectUserEmailFragment().apply {
            TAG = tag
            arguments = Bundle().apply { putSerializable(DATAPOINT_EMAIL, dataPoint) }
        }
    }
}
