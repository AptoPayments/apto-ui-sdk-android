package com.aptopayments.sdk.features.card.cardstats

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aptopayments.mobile.data.transaction.MCC
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.features.card.cardstats.chart.CardTransactionsChartContract
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.threeten.bp.LocalDate

private const val CARD_TRANSACTIONS_CHART_TAG = "CardTransactionsChartFragment"

internal class CardMonthlyStatsAdapter(date: LocalDate, private val cardID: String, fr: Fragment) :
    FragmentStateAdapter(fr),
    CardTransactionsChartContract.Delegate,
    KoinComponent {

    private val fragmentFactory: FragmentFactory by inject()
    val months = mutableListOf(date)
    var delegate: Delegate? = null

    interface Delegate {
        fun onCategorySelected(mcc: MCC, startDate: LocalDate, endDate: LocalDate)
        fun onStatementTapped(month: Int, year: Int)
    }

    fun addPage(date: LocalDate) {
        if (!months.contains(date)) {
            months.add(date)
            this.notifyItemRangeInserted(months.size - 1, 1)
        }
    }

    override fun getItemCount(): Int = months.size

    override fun createFragment(position: Int): Fragment {
        val fragment = fragmentFactory.cardTransactionsChartFragment(
            cardID,
            months[position],
            CARD_TRANSACTIONS_CHART_TAG + position
        )
        fragment.delegate = this
        return fragment as Fragment
    }

    override fun onCategorySelected(mcc: MCC, startDate: LocalDate, endDate: LocalDate) {
        delegate?.onCategorySelected(mcc, startDate, endDate)
    }

    override fun onStatementTapped(month: Int, year: Int) {
        delegate?.onStatementTapped(month, year)
    }
}
