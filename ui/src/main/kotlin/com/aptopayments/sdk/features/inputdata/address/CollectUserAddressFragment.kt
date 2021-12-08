package com.aptopayments.sdk.features.inputdata.address

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.Toolbar
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.user.AddressDataPoint
import com.aptopayments.mobile.data.user.AllowedCountriesConfiguration
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseDataBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentCollectUserAddressBinding
import com.aptopayments.sdk.utils.extensions.shake
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.appbar.AppBarLayout
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val ADDRESS_CONFIG = "config"
private const val MAPS_DEFAULT = "replace_me"
private const val DATAPOINT_ADDRESS = "DATAPOINT_ADDRESS"

internal class CollectUserAddressFragment :
    BaseDataBindingFragment<FragmentCollectUserAddressBinding>(),
    CollectUserAddressContract.View {

    private var initialValue: AddressDataPoint? = null
    private val viewModel: CollectUserAddressViewModel by viewModel { parametersOf(initialValue) }
    private val placesClient: PlacesClient by inject()
    override var delegate: CollectUserAddressContract.Delegate? = null
    private lateinit var config: AllowedCountriesConfiguration
    private lateinit var autocompleteAdapter: PlacesAutocompleteAdapter

    override fun layoutId() = R.layout.fragment_collect_user_address

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
        config = requireArguments()[ADDRESS_CONFIG] as AllowedCountriesConfiguration
        initialValue = requireArguments()[DATAPOINT_ADDRESS] as AddressDataPoint?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initializePlaces()
        super.onCreate(savedInstanceState)
        autocompleteAdapter = PlacesAutocompleteAdapter(requireContext(), placesClient, config.allowedCountries)
    }

    private fun initializePlaces() {
        if (!Places.isInitialized()) {
            val key = getString(R.string.google_maps_key)
            if (key != MAPS_DEFAULT) {
                Places.initialize(requireActivity().application, key)
            } else {
                handleFailure(NoApiKeyFailure())
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.collectAddressSearchEdit.requestFocus()
    }

    override fun setupUI() {
        applyFontsAndColors()
        setupToolBar(binding.tbLlsdkToolbarLayout.tbLlsdkToolbar)
        setHints()
        configureSearchField()
    }

    private fun setHints() {
        binding.collectAddressSearchEdit.hint = "collect_user_data_address_address_placeholder".localized()
        binding.collectAddressOptionalEdit.hint = "collect_user_data_address_apt_unit_placeholder".localized()
    }

    private fun applyFontsAndColors() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(binding.tbLlsdkToolbarLayout as AppBarLayout)
            customizeLargeTitleLabel(binding.collectAddressTitle)
            customizeFormLabel(binding.collectAddressSubtitle)
            customizeEditText(binding.collectAddressSearchEdit)
            binding.collectAddressSearchEdit.setDropDownBackgroundDrawable(ColorDrawable(UIConfig.uiBackgroundPrimaryColor))
            customizeEditText(binding.collectAddressOptionalEdit)
            customizeSubmitButton(binding.continueButton)
        }
    }

    private fun setupToolBar(toolbar: Toolbar) {
        toolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .setPrimaryColors()
                .build()
        )
    }

    override fun setupViewModel() {
        observeNotNullable(viewModel.continueClicked) { delegate?.onAddressSelected(it) }
        observeNotNullable(viewModel.failure) { handleFailure(it) }
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onBackFromAddress()
    }

    private fun configureSearchField() {
        with(binding.collectAddressSearchEdit) {
            setAdapter(autocompleteAdapter)
            setOnDismissListener { viewModel.onAddressDismissed() }
            setOnKeyListener { _, _, _ ->
                viewModel.onEditingAddress()
                false
            }
            onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
                val item = autocompleteAdapter.getItem(i)
                viewModel.onAddressClicked(item.placeId)
            }
        }
    }

    override fun handleFailure(failure: Failure?) {
        when (failure) {
            is CollectUserAddressViewModel.IncorrectAddressFailure -> binding.collectAddressSearchContainer.shake()
            else -> super.handleFailure(failure)
        }
    }

    private class NoApiKeyFailure : Failure.FeatureFailure("error_something_went_wrong")

    companion object {
        fun newInstance(dataPoint: AddressDataPoint?, config: AllowedCountriesConfiguration, tag: String) =
            CollectUserAddressFragment().apply {
                TAG = tag
                arguments = Bundle().apply {
                    putSerializable(ADDRESS_CONFIG, config)
                    putSerializable(DATAPOINT_ADDRESS, dataPoint)
                }
            }
    }
}
