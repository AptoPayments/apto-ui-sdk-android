package com.aptopayments.sdk.features.inputdata.name

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.user.NameDataPoint
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseDataBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentCollectUserNameBinding
import com.aptopayments.sdk.utils.NameInputFilter
import com.aptopayments.sdk.utils.extensions.shake
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val DATAPOINT_NAME = "DATAPOINT_NAME"

internal class CollectUserNameSurnameFragment :
    BaseDataBindingFragment<FragmentCollectUserNameBinding>(),
    CollectUserNameSurnameContract.View {

    private var initialValue: NameDataPoint? = null
    private val viewModel: CollectUserNameViewModel by viewModel { parametersOf(initialValue) }
    override var delegate: CollectUserNameSurnameContract.Delegate? = null

    override fun layoutId() = R.layout.fragment_collect_user_name

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
        initialValue = requireArguments()[DATAPOINT_NAME] as NameDataPoint?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
    }

    override fun onPresented() {
        super.onPresented()
        binding.etFirstName.requestFocus()
        showKeyboard()
    }

    override fun setupUI() {
        applyFontsAndColors()
        setupToolBar(binding.tbLlsdkToolbarLayout.tbLlsdkToolbar)
        setScrollOnFocus()
        setHints()
        setEditTextFilters()
    }

    private fun setEditTextFilters() {
        binding.etFirstName.filters = arrayOf(NameInputFilter { binding.firstNameIn.shake() })
        binding.etLastName.filters = arrayOf(NameInputFilter { binding.lastNameIn.shake() })
    }

    private fun setScrollOnFocus() {
        binding.etFirstName.setOnFocusChangeListener { _, hasFocus ->
            scrollToPositionIfHasFocus(hasFocus, binding.firstNameIn.top)
        }
        binding.etLastName.setOnFocusChangeListener { _, hasFocus ->
            scrollToPositionIfHasFocus(hasFocus, binding.lastNameIn.top)
        }
    }

    private fun setHints() {
        binding.etFirstName.hint = "collect_user_data_personal_info_first_name_title".localized()
        binding.etLastName.hint = "collect_user_data_personal_info_last_name_title".localized()
    }

    private fun scrollToPositionIfHasFocus(hasFocus: Boolean, container: Int) {
        if (hasFocus) {
            binding.scroll.post {
                binding.scroll.smoothScrollTo(0, container)
            }
        }
    }

    private fun applyFontsAndColors() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(binding.tbLlsdkToolbarLayout.tbLlsdkToolbarLayoutInternal)
            customizeLargeTitleLabel(binding.tvEmailHeader)
            customizeEditText(binding.etFirstName)
            customizeEditText(binding.etLastName)
            customizeSubmitButton(binding.continueButton)
        }
    }

    private fun setupToolBar(toolbar: Toolbar) {
        toolbar.configure(this, ToolbarConfiguration.Builder().setPrimaryColors().build())
    }

    override fun setupViewModel() {
        observeNotNullable(viewModel.continueNext) { delegate?.onNameEnteredCorrectly(it) }
        observeNotNullable(viewModel.nameError) { if (it) binding.firstNameIn.shake() }
        observeNotNullable(viewModel.surnameError) { if (it) binding.lastNameIn.shake() }
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onBackFromInputName()
    }

    companion object {
        fun newInstance(initialValue: NameDataPoint?, tag: String) = CollectUserNameSurnameFragment().apply {
            arguments = Bundle().apply { putSerializable(DATAPOINT_NAME, initialValue) }
            TAG = tag
        }
    }
}
