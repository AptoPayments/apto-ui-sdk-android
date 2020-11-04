package com.aptopayments.sdk.features.maintenance

import android.os.Bundle
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.network.NetworkHandler
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.core.ui.StatusBarUtil
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import kotlinx.android.synthetic.main.fragment_maintenance.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.inject

internal class MaintenanceFragment : BaseFragment(), MaintenanceContract.View {

    private val networkHandler: NetworkHandler by inject()
    private val viewModel: MaintenanceViewModel by viewModel()
    override fun layoutId() = R.layout.fragment_maintenance

    override fun backgroundColor(): Int = UIConfig.uiNavigationSecondaryColor

    override fun setupViewModel() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideLoading()
    }

    override fun setupUI() {
        setupTheme()
    }

    private fun setupTheme() {
        activity?.window?.let { StatusBarUtil.setStatusBarColor(it, UIConfig.uiNavigationSecondaryColor) }
        iv_maintenance.setColorFilter(UIConfig.uiTertiaryColor)
        themeManager().apply {
            customizeContentPlainInvertedText(tv_description_text)
            customizeSubmitButton(continue_button)
        }
    }

    override fun setupListeners() = continue_button.setOnClickListenerSafe { networkHandler.checkMaintenanceMode() }

    companion object {
        fun newInstance() = MaintenanceFragment()
    }
}
