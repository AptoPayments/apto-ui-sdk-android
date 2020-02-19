package com.aptopayments.sdk.features.nonetwork

import android.graphics.PorterDuff
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.network.ApiCatalog
import com.aptopayments.core.network.NetworkHandler
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.ui.StatusBarUtil
import com.aptopayments.sdk.utils.FontsUtil
import kotlinx.android.synthetic.main.fragment_no_network_theme_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.inject
import java.lang.reflect.Modifier
import java.net.URI
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate

const val DELAY = 5000L
const val PERIOD = 5000L
const val PORT = 443

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class NoNetworkFragmentThemeTwo: BaseFragment(), NoNetworkContract.View {

    override fun layoutId() = R.layout.fragment_no_network_theme_two

    private val networkHandler: NetworkHandler by inject()
    private val viewModel: NoNetworkViewModel by viewModel()
    private var timer = Timer()

    override fun backgroundColor(): Int = UIConfig.uiNavigationSecondaryColor

    override fun setupViewModel() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideLoading()
    }

    override fun onStart() {
        super.onStart()
        val uri = URI(ApiCatalog.environment.baseUrl)
        timer = Timer()
        timer.scheduleAtFixedRate(delay = DELAY, period = PERIOD) {
            networkHandler.checkNetworkReachability(uri.host, PORT)
        }
    }

    override fun onStop() {
        timer.cancel()
        super.onStop()
    }

    override fun setupUI() {
        activity?.window?.let { StatusBarUtil.setStatusBarColor(it, UIConfig.uiNavigationSecondaryColor) }
        tv_description_text.setTextColor(UIConfig.textTopBarSecondaryColor)
        tv_loading_text.setTextColor(UIConfig.textTertiaryColor)
        pb_progress.indeterminateDrawable.setColorFilter(UIConfig.textTertiaryColor, PorterDuff.Mode.SRC_IN)

        FontsUtil.getFontOfType(FontsUtil.FontType.REGULAR)?.let{
            tv_description_text.typeface = it
            tv_loading_text.typeface = it
        }
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    companion object {
        fun newInstance() = NoNetworkFragmentThemeTwo()
    }
}
