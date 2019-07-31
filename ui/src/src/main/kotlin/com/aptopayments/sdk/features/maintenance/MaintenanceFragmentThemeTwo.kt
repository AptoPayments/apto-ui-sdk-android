package com.aptopayments.sdk.features.maintenance

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.extension.localized
import com.aptopayments.core.network.NetworkHandler
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.viewModel
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.core.ui.StatusBarUtil
import kotlinx.android.synthetic.main.fragment_maintenance_theme_two.*
import java.lang.reflect.Modifier
import javax.inject.Inject

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class MaintenanceFragmentThemeTwo : BaseFragment(), MaintenanceContract.View {

    @Inject lateinit var networkHandler: NetworkHandler
    private lateinit var viewModel: MaintenanceViewModel
    override fun layoutId() = R.layout.fragment_maintenance_theme_two

    override fun setupViewModel() {
        viewModel = viewModel(viewModelFactory) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
        hideLoading()
    }

    override fun setupUI() {
        setupTheme()
        setupTexts()
    }

    private fun setupTheme() {
        activity?.window?.let { StatusBarUtil.setStatusBarColor(it, UIConfig.uiNavigationSecondaryColor) }
        view!!.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        iv_maintenance.setColorFilter(UIConfig.uiTertiaryColor)
        themeManager().apply {
            customizeContentPlainInvertedText(tv_description_text)
            customizeSubmitButton(continue_button)
        }
    }

    private fun setupTexts() {
        context?.let {
            tv_description_text.text = "maintenance.description".localized(it)
            continue_button.text = "maintenance.retry.title".localized(it)
        }
    }

    override fun setupListeners() =
        continue_button.setOnClickListener { networkHandler.checkMaintenanceMode() }

    companion object {
        fun newInstance() = MaintenanceFragmentThemeTwo()
    }
}
