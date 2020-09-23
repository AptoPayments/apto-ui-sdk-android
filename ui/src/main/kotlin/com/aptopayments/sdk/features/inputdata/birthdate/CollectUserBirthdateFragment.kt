package com.aptopayments.sdk.features.inputdata.birthdate

import android.os.Bundle
import android.view.View
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.user.BirthdateDataPoint
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.ui.views.birthdate.BirthdateView
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_birthdate_verification.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate

private const val DATAPOINT_BIRTHDATE = "DATAPOINT_BIRTHDATE"

internal class CollectUserBirthdateFragment : BaseFragment(), CollectUserBirthdateContract.View {

    private var initialValue: BirthdateDataPoint? = null
    override var delegate: CollectUserBirthdateContract.Delegate? = null
    private val viewModel: CollectUserBirthdateViewModel by viewModel()

    override fun layoutId() = R.layout.fragment_birthdate_verification

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
        initialValue = requireArguments()[DATAPOINT_BIRTHDATE] as BirthdateDataPoint?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (hasBirthdateSet()) {
            birthdate_view.setDate(initialValue!!.birthdate)
        }
    }

    private fun hasBirthdateSet() = initialValue?.birthdate != null && birthdate_view.getDate() == null

    override fun onPresented() {
        customizePrimaryNavigationStatusBar()
    }

    override fun setupViewModel() {
        viewModel.apply {
            observeNotNullable(continueClicked) { delegate?.onBirthdateEnteredCorrectly(it) }
            observeNotNullable(viewModel.loading) { handleLoading(it) }
            observeNotNullable(continueEnabled) { continue_button.isEnabled = it }
        }
    }

    override fun setupUI() {
        setupTheme()
        setupToolbar()
        setTexts()
        birthdate_view.delegate = object : BirthdateView.Delegate {
            override fun onDateInput(value: LocalDate?) {
                viewModel.setLocalDate(value)
            }
        }
    }

    private fun setTexts() {
        tv_birthdate_title.localizedText = "birthday_collector_birthdate_title"
        tv_birthdate_subtitle.localizedText = "birthday_collector_birthdate_subtitle"
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    override fun setupListeners() {
        continue_button.setOnClickListener {
            hideKeyboard()
            viewModel.onContinueClicked()
        }
    }

    override fun onBackPressed() {
        delegate?.onBackFromBirthdateVerification()
    }

    private fun setupTheme() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_birthdate_title)
            customizeFormLabel(tv_birthdate_subtitle)
            customizeSubmitButton(continue_button)
        }
    }

    private fun setupToolbar() {
        tb_llsdk_toolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .setPrimaryColors()
                .build()
        )
    }

    companion object {
        fun newInstance(dataPoint: BirthdateDataPoint?, tag: String) = CollectUserBirthdateFragment().apply {
            arguments = Bundle().apply { putSerializable(DATAPOINT_BIRTHDATE, dataPoint) }
            TAG = tag
        }
    }
}