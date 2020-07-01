package com.aptopayments.sdk.features.transactiondetails

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.extensions.setColorFilterCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_transaction_details_theme_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.abs

private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
private const val TRANSACTION_KEY = "TRANSACTION"
private const val ZOOM = 16f

internal class TransactionDetailsFragmentThemeTwo : BaseFragment(), TransactionDetailsContract.View,
    OnMapReadyCallback {

    override var delegate: TransactionDetailsContract.Delegate? = null
    private lateinit var transaction: Transaction
    private val viewModel: TransactionDetailsViewModel by viewModel { parametersOf(transaction) }
    private var mMapView: MapView? = null

    override fun layoutId(): Int = R.layout.fragment_transaction_details_theme_two

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setUpArguments() {
        transaction = arguments!![TRANSACTION_KEY] as Transaction
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(layoutId(), container, false)
        mMapView = view.findViewById(R.id.map_view)
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mMapView?.onCreate(mapViewBundle)
        mMapView?.getMapAsync(this)
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }

        mMapView?.onSaveInstanceState(mapViewBundle)
    }

    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as AppCompatActivity).setSupportActionBar(null)
        mMapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView?.onLowMemory()
    }

    override fun onPresented() {
        super.onPresented()
        customizeSecondaryNavigationStatusBar()
    }

    override fun setupViewModel() {
    }

    override fun setupUI() {
        setupToolBar()
        setupTitle()
        setupTheme()
        setupTexts()
        setupAdjustmentsAdapter()
        setupDetailsTitleSection()
    }

    private fun setupDetailsTitleSection() {
        if (!UIConfig.transactionDetailsShowDetailsSectionTitle) {
            ll_expandable_section.show()
            ll_transaction_info_expandable_section_header.remove()
            ll_details_top_separator.remove()
            ll_details_bottom_separator.remove()
        }
    }

    override fun setupListeners() {
        super.setupListeners()
        toolbar.setOnClickListener { onBackPressed() }
        if (UIConfig.transactionDetailsShowDetailsSectionTitle) {
            ll_transaction_info_expandable_section_header.setOnClickListener { toggleDetails() }
        }
    }

    override fun onBackPressed() {
        delegate?.onTransactionDetailsBackPressed()
    }

    override fun onMapReady(map: GoogleMap) {
        val configuration = MapConfigurationFactory().create(viewModel.transaction)
        configuration?.let { configureMap(map, configuration) } ?: hideMap()
    }

    private fun configureMap(map: GoogleMap, config: MapConfiguration) {
        map.uiSettings?.isMapToolbarEnabled = false
        val latLng = LatLng(config.latitude, config.longitude)
        val marker = MarkerOptions().position(latLng).icon(getMapMarker(config.iconResource))
        map.addMarker(marker)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM))
    }

    private fun setupTheme() {
        setTitleBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        iv_address_icon.setColorFilter(UIConfig.uiPrimaryColor)
        ll_address_separator.setBackgroundColor(UIConfig.uiTertiaryColor)
        ll_details_top_separator.setBackgroundColor(UIConfig.uiTertiaryColor)
        ll_details_bottom_separator.setBackgroundColor(UIConfig.uiTertiaryColor)
        if (viewModel.isDeclined) {
            rl_declined_transaction_banner.setBackgroundColorKeepShape(UIConfig.uiErrorColor)
            iv_banner_icon.setColorFilter(UIConfig.iconTertiaryColor)
            rl_declined_transaction_banner.show()
        }

        with(themeManager()) {
            customizeStarredSectionTitle(tv_title)
            customizeAmountBig(tv_subtitle_left)
            customizeSubCurrency(tv_subtitle_right)
            customizeMainItem(tv_address)
            customizeMainItem(tv_transaction_date_label)
            customizeMainItemRight(tv_transaction_description)
            customizeMainItem(tv_transaction_category_label)
            customizeMainItemRight(tv_transaction_category)
            customizeSectionTitle(tv_transaction_info_expandable_section_header)
            customizeMainItem(tv_transaction_device_type_label)
            customizeMainItemRight(tv_transaction_device_type)
            customizeMainItem(tv_transaction_type_label)
            customizeMainItemRight(tv_transaction_type)
            customizeMainItem(tv_transaction_status_label)
            customizeMainItemRight(tv_transaction_status)
            customizeMainItem(tv_transaction_funding_source_name_label)
            customizeMainItemRight(tv_transaction_funding_source_name)
            customizeBannerTitle(tv_banner_title)
            customizeBannerMessage(tv_banner_description)
        }
        activity?.window?.let {
            it.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            it.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
    }

    private fun setupToolBar() {
        toolbar.configure(this,
            ToolbarConfiguration.Builder().backButtonMode(BackButtonMode.Back(UIConfig.textTopBarSecondaryColor))
                .build()
        )
        toolbar.bringToFront()
    }

    private fun setTitleBackgroundColor(color: Int) {
        transaction_details_toolbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout,
                                                                                                      verticalOffset ->
            if (abs(verticalOffset) == appBarLayout?.totalScrollRange) {
                // Collapsed
                toolbar.setBackgroundColor(color)
                collapsing_toolbar.setStatusBarScrimColor(color)
            } else {
                // Expanded
                toolbar.setBackgroundColor(Color.TRANSPARENT)
                collapsing_toolbar.setStatusBarScrimColor(Color.TRANSPARENT)
            }
        })
        collapsing_toolbar.setBackgroundColor(color)
    }

    private fun setupTitle() {
        ll_header.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        tv_title.text = viewModel.transactionDescription
        tv_subtitle_left.text = viewModel.localAmountRepresentation
        tv_subtitle_right.text = viewModel.nativeBalanceRepresentation
    }

    private fun getMapMarker(@DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor? {
        context?.let {
            val backgroundDrawable = ContextCompat.getDrawable(it, R.drawable.ic_map_marker_icon)
            backgroundDrawable?.let { background ->
                background.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)
                background.setColorFilterCompat(UIConfig.uiSecondaryColor, PorterDuff.Mode.SRC_ATOP)
                val merchantIcon = ContextCompat.getDrawable(it, vectorDrawableResourceId)
                merchantIcon?.let { icon ->
                    val backgroundWidth = background.intrinsicWidth
                    val backgroundHeight = background.intrinsicHeight
                    val iconWidth = icon.intrinsicWidth * 2
                    val iconHeight = icon.intrinsicHeight * 2
                    val marginLeft = (backgroundWidth * 0.5 - iconWidth * 0.5).toInt()
                    val marginTop = (backgroundHeight * 0.4 - iconHeight * 0.5).toInt()
                    icon.setBounds(marginLeft, marginTop, iconWidth + marginLeft, iconHeight + marginTop)
                    icon.setColorFilterCompat(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                    val bitmap = Bitmap.createBitmap(
                        background.intrinsicWidth,
                        background.intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    background.draw(canvas)
                    icon.draw(canvas)
                    return BitmapDescriptorFactory.fromBitmap(bitmap)
                }
            }
        }
        return null
    }

    private fun hideMap() {
        transaction_details_toolbar.setExpanded(false)
        nested_scroll_view.isNestedScrollingEnabled = false
        mMapView?.remove()
    }

    private fun toggleDetails() {
        ll_expandable_section.invisibleIf(ll_expandable_section.isShown)
        changeArrowDrawable()
    }

    private fun changeArrowDrawable() {
        context?.let {
            iv_transaction_info_arrow.setImageDrawable(ContextCompat.getDrawable(it, getArrowDrawable()))
        }
    }

    private fun getArrowDrawable() =
        if (ll_expandable_section.isShown) R.drawable.ic_arrow_drop_up_black_24dp else R.drawable.ic_arrow_drop_down_black_24dp

    fun setupTexts() {
        setAddressName()
        tv_transaction_description.text = viewModel.createdAt
        setMccText()
        setFundingSourceName()
        setDeviceTypeText()
        tv_transaction_type.text = viewModel.transactionType
        tv_transaction_status.text = viewModel.transactionStatus
        tv_banner_description.text = viewModel.declinedDescription
    }

    private fun setAddressName() {
        setTextOrRemoveHolderIfNullOrEmpty(tv_address, rl_address_holder, viewModel.addressName)
    }

    private fun setMccText() {
        setTextOrRemoveHolderIfNullOrEmpty(
            tv_transaction_category,
            rl_transaction_category_holder,
            viewModel.mccName
        )
    }

    private fun setFundingSourceName() {
        setTextOrRemoveHolderIfNullOrEmpty(
            tv_transaction_funding_source_name,
            rl_transaction_funding_source_name_holder,
            viewModel.fundingSourceName
        )
    }

    private fun setDeviceTypeText() {
        setTextOrRemoveHolderIfNullOrEmpty(tv_transaction_device_type, rl_device_type_holder, viewModel.deviceType)
    }

    private fun setTextOrRemoveHolderIfNullOrEmpty(tv: TextView, holder: View, text: String?) {
        holder.goneIf(text.isNullOrEmpty())
        tv.text = text ?: ""
    }

    private fun setupAdjustmentsAdapter() {
        adjustments_recycler_view.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        viewModel.transaction.adjustments?.let { adjustments ->
            adjustments_recycler_view.adapter = AdjustmentsAdapter(viewModel.transaction, adjustments)
        }
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    companion object {
        fun newInstance(transaction: Transaction) = TransactionDetailsFragmentThemeTwo().apply {
            arguments = Bundle().apply { putSerializable(TRANSACTION_KEY, transaction) }
        }
    }
}
