package com.aptopayments.sdk.features.card.cardstats

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.transaction.MCC
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.core.extension.monthToString
import com.aptopayments.sdk.core.platform.applicationComponent
import org.threeten.bp.LocalDate
import javax.inject.Inject

private const val CARD_TRANSACTIONS_CHART_TAG = "CardTransactionsChartFragment"

internal class CardTransactionsChartPagerAdapter(
        fm: FragmentManager,
        private val cardID: String,
        private val dates: ArrayList<LocalDate>
) : FragmentStatePagerAdapter(fm), CardTransactionsChartContract.Delegate {

    interface Delegate {
        fun onCategorySelected(mcc: MCC, startDate: LocalDate, endDate: LocalDate)
    }

    init {
        AptoPlatform.applicationComponent?.inject(this)
    }

    @Inject
    lateinit var fragmentFactory: FragmentFactory

    var delegate: Delegate? = null

    override fun getCount(): Int = dates.size

    override fun getItem(position: Int): Fragment {
        val fragment = fragmentFactory.cardTransactionsChartFragment(
                UIConfig.uiTheme,
                cardID,
                dates[position],
                CARD_TRANSACTIONS_CHART_TAG+position)
        fragment.delegate = this
        return fragment as Fragment
    }

    override fun getPageTitle(position: Int): CharSequence =
            if (dates[position] == LocalDate.MAX) ""
            else dates[position].monthToString()

    override fun getItemPosition(any: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun onCategorySelected(mcc: MCC, startDate: LocalDate, endDate: LocalDate) {
        delegate?.onCategorySelected(mcc, startDate, endDate)
    }
}
