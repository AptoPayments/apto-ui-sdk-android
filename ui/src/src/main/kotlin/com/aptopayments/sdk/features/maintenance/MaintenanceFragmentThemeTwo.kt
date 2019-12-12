package com.aptopayments.sdk.features.maintenance

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.network.NetworkHandler
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.core.ui.StatusBarUtil
import kotlinx.android.synthetic.main.fragment_maintenance_theme_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.inject
import java.lang.reflect.Modifier

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class MaintenanceFragmentThemeTwo : BaseFragment(), MaintenanceContract.View {

    private val networkHandler: NetworkHandler by inject()
    private val viewModel: MaintenanceViewModel by viewModel()
    override fun layoutId() = R.layout.fragment_maintenance_theme_two

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
        view!!.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        iv_maintenance.setColorFilter(UIConfig.uiTertiaryColor)
        themeManager().apply {
            customizeContentPlainInvertedText(tv_description_text)
            customizeSubmitButton(continue_button)
        }
    }

    override fun setupListeners() = continue_button.setOnClickListener { networkHandler.checkMaintenanceMode() }

    companion object {
        fun newInstance() = MaintenanceFragmentThemeTwo()
    }
}
