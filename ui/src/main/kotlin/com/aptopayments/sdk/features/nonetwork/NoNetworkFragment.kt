package com.aptopayments.sdk.features.nonetwork

import android.graphics.PorterDuff
import android.os.Bundle
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.network.ApiKeyProvider
import com.aptopayments.mobile.network.NetworkHandler
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.ui.StatusBarUtil
import com.aptopayments.sdk.utils.FontsUtil
import com.aptopayments.sdk.utils.extensions.setColorFilterCompat
import kotlinx.android.synthetic.main.fragment_no_network.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.android.ext.android.inject
import java.net.URI
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate

const val DELAY = 2000L
const val PERIOD = 2000L
const val PORT = 443

internal class NoNetworkFragment : BaseFragment(), NoNetworkContract.View {

    override fun layoutId() = R.layout.fragment_no_network

    private val networkHandler: NetworkHandler by inject()
    private val viewModel: NoNetworkViewModel by viewModel()
    private var timer: Timer? = null

    override fun backgroundColor(): Int = UIConfig.uiNavigationSecondaryColor

    override fun setupViewModel() {
        // do nothing
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.hideLoading()
    }

    override fun onStart() {
        super.onStart()
        val uri = URI(ApiKeyProvider.getEnvironmentUrl())
        timer = Timer()
        timer?.scheduleAtFixedRate(delay = DELAY, period = PERIOD) {
            networkHandler.checkNetworkReachability(uri.host, PORT)
        }
    }

    override fun onStop() {
        timer?.cancel()
        super.onStop()
    }

    override fun setupUI() {
        activity?.window?.let { StatusBarUtil.setStatusBarColor(it, UIConfig.uiNavigationSecondaryColor) }
        tv_description_text.setTextColor(UIConfig.textTopBarSecondaryColor)
        tv_loading_text.setTextColor(UIConfig.textTertiaryColor)
        pb_progress.indeterminateDrawable.setColorFilterCompat(UIConfig.textTertiaryColor, PorterDuff.Mode.SRC_IN)

        FontsUtil.getFontOfType(FontsUtil.FontType.REGULAR)?.let {
            tv_description_text.typeface = it
            tv_loading_text.typeface = it
        }
    }

    companion object {
        fun newInstance() = NoNetworkFragment()
    }
}
