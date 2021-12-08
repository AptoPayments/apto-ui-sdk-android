package com.aptopayments.sdk.features.inputdata.id

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.Toolbar
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.user.IdDataPointConfiguration
import com.aptopayments.mobile.data.user.IdDocumentDataPoint
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseDataBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentCollectUserIdBinding
import com.aptopayments.sdk.utils.CustomArrayAdapter
import com.google.android.material.appbar.AppBarLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val CONFIGURATION_BUNDLE = "CONFIGURATION_BUNDLE"
private const val DATAPOINT_ID = "DATAPOINT_ID"

internal class CollectUserIdFragment :
    BaseDataBindingFragment<FragmentCollectUserIdBinding>(),
    CollectUserIdContract.View {

    private lateinit var idTypeAdapter: CustomArrayAdapter<String>
    private lateinit var config: IdDataPointConfiguration
    private var initialValue: IdDocumentDataPoint? = null
    private val viewModel: CollectUserIdViewModel by viewModel { parametersOf(initialValue, config) }
    override var delegate: CollectUserIdContract.Delegate? = null

    override fun layoutId() = R.layout.fragment_collect_user_id

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
    }

    override fun onPresented() {
        super.onPresented()
        if (!viewModel.countryIsVisible) {
            binding.collectIdNumberEdittext.requestFocus()
            showKeyboard()
        }
    }

    override fun setupUI() {
        applyFontsAndColors()
        setupToolBar(binding.tbLlsdkToolbarLayout.tbLlsdkToolbar)

        setTypeSpinnerChangeListener()

        idTypeAdapter = CustomArrayAdapter(requireContext(), mutableListOf())
        binding.collectIdTypeSpinner.adapter = idTypeAdapter
    }

    private fun setTypeSpinnerChangeListener() {
        binding.collectIdTypeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                viewModel.onIdTypeSelected(position)
                binding.collectIdNumberEdittext.requestFocus()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                viewModel.onIdTypeSelected(UNSELECTED_VALUE)
            }
        }
    }

    private fun applyFontsAndColors() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(binding.tbLlsdkToolbarLayout as AppBarLayout)
            customizeLargeTitleLabel(binding.tvIdHeader)
            customizeFormLabel(binding.collectIdCountryTitle)
            customizeFormLabel(binding.collectIdTypeTitle)
            customizeFormLabel(binding.collectIdNumberTitle)
            customizeEditText(binding.collectIdNumberEdittext)
            customizeSubmitButton(binding.continueButton)
        }
    }

    private fun setupToolBar(toolbar: Toolbar) {
        with(toolbar) {
            setTitleTextColor(UIConfig.textTopBarPrimaryColor)
            setBackgroundColor(UIConfig.uiNavigationPrimaryColor)
            configure(this@CollectUserIdFragment, ToolbarConfiguration.Builder().setPrimaryColors().build())
        }
    }

    override fun setupViewModel() {
        observeNotNullable(viewModel.continueNext) { delegate?.onIdEnteredCorrectly(it) }
        observeNotNullable(viewModel.typeList) { list ->
            idTypeAdapter.clear()
            idTypeAdapter.addAll(list.map { type -> type.toLocalizedString() })
            idTypeAdapter.notifyDataSetChanged()
        }
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onBackFromCollectId()
    }

    override fun setUpArguments() {
        config = requireArguments()[CONFIGURATION_BUNDLE] as IdDataPointConfiguration
        initialValue = requireArguments()[DATAPOINT_ID] as IdDocumentDataPoint?
    }

    companion object {
        fun newInstance(dataPoint: IdDocumentDataPoint?, config: IdDataPointConfiguration, tag: String) =
            CollectUserIdFragment().apply {
                TAG = tag
                arguments = Bundle().apply {
                    putSerializable(DATAPOINT_ID, dataPoint)
                    putSerializable(CONFIGURATION_BUNDLE, config)
                }
            }
    }
}
