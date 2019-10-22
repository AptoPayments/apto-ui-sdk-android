package com.aptopayments.sdk.features.card.cardstats

import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.stats.MonthlySpending
import com.aptopayments.core.data.transaction.MCC
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.extension.monthToString
import com.aptopayments.sdk.core.extension.yearToString
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_card_monthly_stats.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.threeten.bp.LocalDate
import java.lang.reflect.Modifier

private const val CARD_ID_KEY = "CARD_ID"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class CardMonthlyStatsFragmentThemeTwo : BaseFragment(), CardMonthlyStatsContract.View,
        CardTransactionsChartPagerAdapter.Delegate {

    private val viewModel: CardMonthlyStatsViewModel by sharedViewModel()
    private lateinit var cardId: String
    private lateinit var pagerAdapter: CardTransactionsChartPagerAdapter
    private lateinit var dateList: ArrayList<LocalDate>
    private var tabAdded = true

    override var delegate: CardMonthlyStatsContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_card_monthly_stats

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cardId = arguments!![CARD_ID_KEY] as String
    }

    override fun setupViewModel() {
        viewModel.apply { failure(failure) { handleFailure(it) } }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dateList = ArrayList(3)
        val localDate = LocalDate.now()
        dateList.add(localDate.minusMonths(1))
        dateList.add(localDate)
        dateList.add(LocalDate.MAX)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setupUI() {
        view?.setBackgroundColor(UIConfig.uiBackgroundSecondaryColor)
        setupToolBar()
    }

    override fun setupListeners() {
        super.setupListeners()
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.text.isNullOrEmpty()) {
                    Handler().postDelayed({ tabLayout.getTabAt(1)?.select() }, 100)
                    return
                }
                viewPager.currentItem = tab.position
                val title = tab.customView as TextView?
                title?.let { themeManager().customizeSectionTitle(it) }
                if (tabLayout.selectedTabPosition == 0 && !tabAdded) addPreviousMonthTab()
                else if (tabLayout.selectedTabPosition == 2 && !tabAdded) addNextMonthTab()
                tb_llsdk_toolbar.title = context?.let { "stats.monthly_spending.title".localized(it).replace("<<YEAR>>", dateList[1].year.toString()) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val title = tab.customView as TextView?
                title?.let { themeManager().customizeTimestamp(it) }
                tabAdded = false
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupToolBar() {
        tb_llsdk_toolbar.setTitleTextColor(UIConfig.textTopBarSecondaryColor)
        tb_llsdk_toolbar.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        delegate?.configureToolbar(
                toolbar = tb_llsdk_toolbar,
                title = context?.let { "stats.monthly_spending.title".localized(it)
                        .replace("<<YEAR>>", LocalDate.now().yearToString()) },
                backButtonMode = BaseActivity.BackButtonMode.Back(null, UIConfig.textTopBarSecondaryColor)
        )
    }

    override fun onBackPressed() {
        delegate?.onBackFromCardMonthlyStats()
    }

    override fun onDestroy() {
        viewModel.invalidateCache()
        super.onDestroy()
    }

    private fun addPreviousMonthTab() {
        tabAdded = true
        val currentDate = dateList[0]
        context?.let { context ->
            viewModel.getMonthlySpending(context, cardId, currentDate.monthToString(), currentDate.yearToString()) {
                dateList.removeAt(2)
                dateList.add(0, dateList[0].minusMonths(1))
                pagerAdapter.notifyDataSetChanged()
                selectCurrentTab()
                updateTabs(it)
            }
        }
    }

    private fun addNextMonthTab() {
        tabAdded = true
        val currentDate = dateList[2]
        context?.let { context ->
            viewModel.getMonthlySpending(context, cardId, currentDate.monthToString(), currentDate.yearToString()) {
                dateList.removeAt(0)
                dateList.add(2, dateList[1].plusMonths(1))
                pagerAdapter.notifyDataSetChanged()
                selectCurrentTab()
                updateTabs(it)
            }
        }
    }

    private fun selectCurrentTab() = viewPager.post {
        viewPager.currentItem = 1
        tabAdded = false
    }

    private fun enableTab(index: Int) {
        (tabLayout.getChildAt(0) as ViewGroup).getChildAt(index).isClickable = true
    }

    private fun disableTab(index: Int) {
        if (dateList[index] == LocalDate.MAX) return
        (tabLayout.getChildAt(0) as ViewGroup).getChildAt(index).isClickable = false
        dateList[index] = LocalDate.MAX
        pagerAdapter.notifyDataSetChanged()
    }

    private fun updateTabs(monthlySpending: MonthlySpending?) = monthlySpending?.let {
        if (it.prevSpendingExists) enableTab(0) else disableTab(0)
        if (it.nextSpendingExists) enableTab(2) else disableTab(2)
    }

    override fun viewLoaded() {
        viewModel.viewLoaded()
        pagerAdapter = CardTransactionsChartPagerAdapter(childFragmentManager, cardId, dateList)
        viewPager.adapter = pagerAdapter
        pagerAdapter.delegate = this
        tabLayout.setupWithViewPager(viewPager)
        disableTab(0)
        disableTab(2)
        viewPager.setCurrentItem(1, false)
        tabAdded = false
        val currentDate = dateList[1]
        context?.let { context ->
            viewModel.getMonthlySpending(context, cardId, currentDate.monthToString(), currentDate.yearToString()) {
                dateList[0] = dateList[1].minusMonths(1)
                updateTabs(it)
                pagerAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCategorySelected(mcc: MCC, startDate: LocalDate, endDate: LocalDate) {
        delegate?.onCategorySelected(mcc, startDate, endDate)
    }

    override fun onStatementTapped(month: Int, year: Int) {
        showLoading()
        viewModel.getMonthlyStatement(month, year) {
            hideLoading()
            delegate?.showMonthlyStatement(it)
        }
    }

    companion object {
        fun newInstance(cardId: String) = CardMonthlyStatsFragmentThemeTwo().apply {
            arguments = Bundle().apply { putString(CARD_ID_KEY, cardId) }
        }
    }
}
