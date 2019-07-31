package com.aptopayments.sdk.features.nonetwork

import android.graphics.PorterDuff
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.extension.localized
import com.aptopayments.core.network.ApiCatalog
import com.aptopayments.core.network.NetworkHandler
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.viewModel
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.ui.StatusBarUtil
import com.aptopayments.sdk.utils.FontsUtil
import kotlinx.android.synthetic.main.fragment_no_network_theme_two.*
import java.lang.reflect.Modifier
import java.net.URI
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.scheduleAtFixedRate

const val DELAY = 5000L
const val PERIOD = 5000L
const val PORT = 443

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class NoNetworkFragmentThemeTwo: BaseFragment(), NoNetworkContract.View {

    override fun layoutId() = R.layout.fragment_no_network_theme_two

    @Inject lateinit var networkHandler: NetworkHandler
    private lateinit var viewModel: NoNetworkViewModel
    private var timer = Timer()

    override fun setupViewModel() {
        viewModel = viewModel(viewModelFactory) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
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
        context?.let {
            tv_description_text.text = "no_network.description".localized(it)
            tv_loading_text.text = "no_network.reconnect.title".localized(it)
        }
        activity?.window?.let { StatusBarUtil.setStatusBarColor(it, UIConfig.uiNavigationSecondaryColor) }
        view!!.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        tv_description_text.setTextColor(UIConfig.textTopBarColor)
        tv_loading_text.setTextColor(UIConfig.textTertiaryColor)
        pb_progress.indeterminateDrawable.setColorFilter(UIConfig.textTertiaryColor, PorterDuff.Mode.SRC_IN)

        FontsUtil.getFontOfType(FontsUtil.FontType.REGULAR)?.let{
            tv_description_text.typeface = it
            tv_loading_text.typeface = it
        }
    }

    override fun viewLoaded() {
        viewModel.viewLoaded()
    }

    companion object {
        fun newInstance() = NoNetworkFragmentThemeTwo()
    }
}
