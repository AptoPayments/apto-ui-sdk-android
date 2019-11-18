package com.aptopayments.sdk.features.transactiondetails

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.*
import androidx.annotation.DrawableRes
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.transaction.Transaction
import com.aptopayments.core.extension.localized
import com.aptopayments.core.extension.toTransactionDetailsFormat
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.data.transaction.iconResource
import com.aptopayments.sdk.core.extension.hide
import com.aptopayments.sdk.core.extension.remove
import com.aptopayments.sdk.core.extension.setBackgroundColorKeepShape
import com.aptopayments.sdk.core.extension.show
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
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
import kotlinx.android.synthetic.main.info_banner.*
import kotlinx.android.synthetic.main.rl_transaction_address.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier
import kotlin.math.abs

private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
private const val TRANSACTION_KEY = "TRANSACTION"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class TransactionDetailsFragmentThemeTwo : BaseFragment(), TransactionDetailsContract.View, OnMapReadyCallback {

    override var delegate: TransactionDetailsContract.Delegate? = null
    private lateinit var transaction: Transaction
    private val viewModel: TransactionDetailsViewModel by viewModel()
    private var mMapView: MapView? = null

    override fun layoutId(): Int = R.layout.fragment_transaction_details_theme_two

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun setUpArguments() {
        transaction = arguments!![TRANSACTION_KEY] as Transaction
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
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
        mMapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView?.onLowMemory()
    }

    override fun onPresented() {
        super.onPresented()
        delegate?.configureSecondaryStatusBar()
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

    override fun onMapReady(map: GoogleMap?) {
        transaction.store?.let {
            val latitude = it.latitude
            val longitude = it.longitude
            map?.uiSettings?.isMapToolbarEnabled = false
            val latLng = longitude?.let { lng -> latitude?.let { lat -> LatLng(lat, lng) } }
            transaction.merchant?.mcc?.let { mcc ->
                latLng?.let { position ->
                    val marker = MarkerOptions().position(position).icon(getMapMarker(mcc.iconResource))
                    map?.addMarker(marker)
                    map?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 16.0f))
                } ?: hideMap()
            } ?: hideMap()
        } ?: hideMap()
    }

    private fun setupTheme() {
        view?.setBackgroundColor(UIConfig.uiBackgroundSecondaryColor)
        setTitleBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        iv_address_icon.setColorFilter(UIConfig.uiPrimaryColor)
        ll_address_separator.setBackgroundColor(UIConfig.uiTertiaryColor)
        ll_details_top_separator.setBackgroundColor(UIConfig.uiTertiaryColor)
        ll_details_bottom_separator.setBackgroundColor(UIConfig.uiTertiaryColor)
        if (transaction.state == Transaction.TransactionState.DECLINED) {
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
        delegate?.configureToolbar(
                toolbar = toolbar,
                title = null,
                backButtonMode = BaseActivity.BackButtonMode.Back(null, UIConfig.textTopBarSecondaryColor)
        )
        toolbar.bringToFront()
    }

    private fun setTitleBackgroundColor(color: Int) {
        transaction_details_toolbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout,
                                                                                                      verticalOffset ->
            if (abs(verticalOffset) == appBarLayout?.totalScrollRange) {
                //Collapsed
                toolbar.setBackgroundColor(color)
                collapsing_toolbar.setStatusBarScrimColor(color)
            } else {
                //Expanded
                toolbar.setBackgroundColor(Color.TRANSPARENT)
                collapsing_toolbar.setStatusBarScrimColor(Color.TRANSPARENT)
            }
        })
        collapsing_toolbar.setBackgroundColor(color)
    }

    private fun setupTitle() {
        ll_header.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        tv_title.text = transaction.transactionDescription
        tv_subtitle_left.text = transaction.getLocalAmountRepresentation()
        if (transaction.getNativeBalanceRepresentation().isNotBlank()
                && transaction.localAmount?.currency != transaction.nativeBalance?.currency) {
            tv_subtitle_right.text = String.format("≈ %s", transaction.getNativeBalanceRepresentation())
        }
    }

    private fun getMapMarker(@DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor? {
        activity?.let {
            val backgroundDrawable = ContextCompat.getDrawable(it, R.drawable.ic_map_marker_icon)
            backgroundDrawable?.let { background ->
                background.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)
                background.setColorFilter(UIConfig.uiSecondaryColor, PorterDuff.Mode.SRC_ATOP)
                val merchantIcon = ContextCompat.getDrawable(it, vectorDrawableResourceId)
                merchantIcon?.let { icon ->
                    val backgroundWidth = background.intrinsicWidth
                    val backgroundHeight = background.intrinsicHeight
                    val iconWidth = icon.intrinsicWidth * 2
                    val iconHeight = icon.intrinsicHeight * 2
                    val marginLeft = (backgroundWidth * 0.5 - iconWidth * 0.5).toInt()
                    val marginTop = (backgroundHeight * 0.4 - iconHeight * 0.5).toInt()
                    icon.setBounds(marginLeft, marginTop, iconWidth + marginLeft, iconHeight + marginTop)
                    icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                    val bitmap = Bitmap.createBitmap(background.intrinsicWidth, background.intrinsicHeight,
                            Bitmap.Config.ARGB_8888)
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
        if (ll_expandable_section.isShown) ll_expandable_section.hide() else ll_expandable_section.show()
        if (ll_expandable_section.isShown) flipExpandableSectionArrowUp() else flipExpandableSectionArrowDown()
    }

    private fun flipExpandableSectionArrowUp() = context?.let {
        iv_transaction_info_arrow.setImageDrawable(
                ContextCompat.getDrawable(it, R.drawable.ic_arrow_drop_up_black_24dp))
    }

    private fun flipExpandableSectionArrowDown() = context?.let {
        iv_transaction_info_arrow.setImageDrawable(
                ContextCompat.getDrawable(it, R.drawable.ic_arrow_drop_down_black_24dp))
    }

    @SuppressLint("SetTextI18n")
    fun setupTexts() = context?.let {
        transaction.store?.address?.let { address ->
            if (address.toStringRepresentation() != address.country?.name) tv_address.text = address.toStringRepresentation()
            else rl_address_holder.remove()
        } ?: rl_address_holder.remove()
        tv_transaction_date_label.text = "transaction_details.basic_info.transaction_date.title".localized()
        tv_transaction_description.text = transaction.createdAt.toTransactionDetailsFormat()
        transaction.merchant?.mcc?.toLocalizedString()?.let { category ->
            tv_transaction_category_label.text = "transaction_details.details.category.title".localized()
            tv_transaction_category.text = category
        } ?: rl_transaction_category_holder.remove()
        tv_transaction_info_expandable_section_header.text = "transaction_details_details_title".localized()
        transaction.fundingSourceName?.let { name ->
            tv_transaction_funding_source_name_label.text = "transaction_details.basic_info.funding_source.title".localized()
            tv_transaction_funding_source_name.text = name
        } ?: rl_transaction_funding_source_name_holder.remove()

        val deviceType = transaction.deviceType().toLocalizedString()
        if (deviceType.isEmpty()) {
            rl_device_type_holder.remove()
        } else {
            tv_transaction_device_type_label.text = "transaction_details.details.device_type.title".localized()
            tv_transaction_device_type.text = deviceType
        }

        tv_transaction_type_label.text = "transaction_details.details.transaction_type.title".localized()
        tv_transaction_type.text = transaction.transactionType.toLocalizedString()
        tv_transaction_status_label.text = "transaction_details.basic_info.transaction_status.title".localized()
        tv_transaction_status.text = transaction.state.toLocalizedString()
        tv_banner_title.text = "transaction_details.basic_info.declined_transaction_banner.title".localized()
        tv_banner_description.text = transaction.declineCode?.toLocalizedString()
    }

    private fun setupAdjustmentsAdapter() = transaction.adjustments?.let { adjustments ->
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        adjustments_recycler_view.layoutManager = linearLayoutManager
        adjustments_recycler_view.adapter = AdjustmentsAdapter(transaction, adjustments)
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    companion object {
        fun newInstance(transaction: Transaction) = TransactionDetailsFragmentThemeTwo().apply {
            arguments = Bundle().apply { putSerializable(TRANSACTION_KEY, transaction) }
        }
    }
}
