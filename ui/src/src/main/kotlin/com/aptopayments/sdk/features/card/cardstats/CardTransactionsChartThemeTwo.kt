package com.aptopayments.sdk.features.card.cardstats

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.aptopayments.core.data.card.Money
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.stats.CategorySpending
import com.aptopayments.core.data.transaction.MCC
import com.aptopayments.core.data.transaction.MCC.Icon
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.data.transaction.iconResource
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.android.synthetic.main.fragment_transactions_chart_theme_two.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.threeten.bp.LocalDate
import java.lang.reflect.Modifier

private var dataLoaded = false

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class CardTransactionsChartThemeTwo : BaseFragment(), CardTransactionsChartContract.View,
        OnChartValueSelectedListener, CategoryListAdapter.Delegate {

    private val viewModel: CardMonthlyStatsViewModel by sharedViewModel()
    private lateinit var pieChart: AptoPieChart
    private lateinit var cardID: String
    private lateinit var date: LocalDate
    private lateinit var categoryListAdapter: CategoryListAdapter
    private var dataSet: PieDataSet = PieDataSet(null, "")
    private var totalSpent = 0.0
    private var currency: String? = null
    private var groupedCategories = ArrayList<CategorySpending>()
    override var delegate: CardTransactionsChartContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_transactions_chart_theme_two

    override fun setupViewModel() {
        viewModel.apply {
            failure(failure) { handleFailure(it) }
            observe(monthlySpendingMap) { updateChartData(it) }
        }
    }

    override fun setupUI() {
        setupTheme()
        setupTexts()
        setupChart()
        setupCategoryListRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        if (date == LocalDate.MAX) return
        totalSpent = 0.0
        if (!dataLoaded) showLoading()
        context?.let {
            viewModel.getMonthlySpending(it, cardID, date.monthToString(), date.yearToString()) {
                if (!dataLoaded) hideLoading()
                dataLoaded = true
                showViews()
            }
        }
    }

    private fun showViews() {
        chart?.show()
        ll_chart_text?.show()
        transaction_list_top_separator?.show()
        tv_list_title?.show()
        transaction_list_bottom_separator?.show()
        rv_categories?.show()
    }

    private fun setupTheme() {
        with(themeManager()) {
            customizeTimestamp(tv_center_text_title)
            customizeMainItem(tv_center_text_amount)
            customizeStarredSectionTitle(tv_list_title, UIConfig.textSecondaryColor)
            customizeEmptyCase(tv_no_transactions)
            customizeSectionTitle(tv_center_text_difference)
        }
        tv_center_text_difference.setTextColor(UIConfig.textMessageColor)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pieChart = view.findViewById(R.id.chart)
        super.onViewCreated(view, savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    private fun setupTexts() {
        context?.let {
            tv_center_text_title.text = "stats.monthly_spending.graph.title".localized(it)
            tv_no_transactions.text = "stats_monthly_spending_list_empty_case".localized(it)
            tv_list_title.text = "stats.monthly_spending.list.title".localized(it)
        }
    }

    private fun setupChart() {
        val initialList = ArrayList<PieEntry>(1)
        initialList.add(0, PieEntry(100f))
        dataSet = PieDataSet(initialList, "")
        dataSet.setDrawValues(false)
        dataSet.setDrawIcons(true)
        dataSet.resetColors()
        dataSet.addColor(UIConfig.uiTertiaryColor)

        pieChart.data = PieData(dataSet)
        pieChart.description = null
        pieChart.legend.isEnabled = false
        pieChart.isRotationEnabled = false
        pieChart.transparentCircleRadius = 0f
        pieChart.holeRadius = 60f
        pieChart.invalidate()
    }

    private fun updateChartData(monthlySpendingMap: HashMap<Pair<String, String>, List<CategorySpending>>?) {
        monthlySpendingMap?.get(Pair(date.monthToString(), date.yearToString()))?.let { spendingList ->
            currency = if (spendingList.isEmpty()) {
                tv_no_transactions.show()
                drawPieChartEntries(ArrayList())
                context?.let { "stats.monthly_spending.graph.default_currency".localized(it) }
            } else {
                tv_no_transactions.remove()
                drawPieChartEntries(calculatePieChartEntries(spendingList))
                spendingList.first().spending?.currency
            }
            val categorySpendingArrayList = ArrayList<CategoryListItem>()
            spendingList.forEach { categorySpending ->
                categorySpendingArrayList.add(CategoryListItem(categorySpending, false))
            }
            categoryListAdapter.categorySpendingList = categorySpendingArrayList
            tv_center_text_amount.text = Money(amount = totalSpent, currency = currency).toString()
        } ?: tv_no_transactions.show()
    }

    private fun drawPieChartEntries(entries: ArrayList<PieEntry>) {
        if (entries.isEmpty()) entries.add(PieEntry(100F)) // Show one category for empty case
        dataSet.values = entries
        dataSet.resetColors()
        repeat(entries.size) {
            dataSet.addColor(UIConfig.uiTertiaryColor)
        }
        pieChart.data = PieData(dataSet)
        pieChart.invalidate()
    }

    private fun calculatePieChartEntries(spendingList: List<CategorySpending>): ArrayList<PieEntry> {
        val entries = generateEntriesAndUpdateTotalSpending(spendingList)
        return groupSmallEntries(entries)
    }

    private fun generateEntriesAndUpdateTotalSpending(spendingList: List<CategorySpending>): ArrayList<PieEntry> {
        val entries = ArrayList<PieEntry>()
        spendingList.forEachIndexed { index, categorySpending ->
            categorySpending.spending?.currency?.let { currency = it }
            categorySpending.spending?.amount?.toFloat()?.let {
                val mcc = MCC(name = categorySpending.categoryId, icon = Icon.valueOf(categorySpending.categoryId.toUpperCase()))
                val icon = context?.let { context -> ContextCompat.getDrawable(context, mcc.iconResource) }
                icon?.setColorFilter(UIConfig.iconSecondaryColor, PorterDuff.Mode.SRC_ATOP)
                entries.add(index, PieEntry(it, icon, PieChartElement(categorySpending, mcc)))
                totalSpent += it
            }
        }
        return entries
    }

    private fun groupSmallEntries(entries: ArrayList<PieEntry>): ArrayList<PieEntry> {
        // Values below 7% of the total will be grouped
        val maxSpendingForSmallCategory= totalSpent * 0.07
        var totalSpendingOfSmallCategories = 0.0
        val iterator = entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value < maxSpendingForSmallCategory) {
                groupedCategories.add((entry.data as PieChartElement).categorySpending)
                iterator.remove()
                totalSpendingOfSmallCategories += entry.value
            }
        }

        // Entries must have a drawable so we create an invisible one for this case
        val emptyDrawable = ColorDrawable(Color.TRANSPARENT)
        val spending = Money(amount = totalSpendingOfSmallCategories, currency = currency)
        entries.add(PieEntry(totalSpendingOfSmallCategories.toFloat(), emptyDrawable, PieChartElement(CategorySpending("", spending))))
        return entries
    }

    override fun setupListeners() {
        super.setupListeners()
        pieChart.setOnChartValueSelectedListener(this)
    }

    override fun onNothingSelected() {
        tv_center_text_title.text = context?.let { "stats.monthly_spending.graph.title".localized(it) }
        tv_center_text_difference.remove()
        tv_center_text_amount.text = Money(amount = totalSpent, currency = currency).toString()
        for (i in 0 until dataSet.colors.size) resetPieChartEntryColor(i)
    }

    @SuppressLint("SetTextI18n")
    override fun onValueSelected(entry: Entry?, highlight: Highlight?) {
        if(entry == null || highlight == null || entry.data == null) return
        for (i in 0 until dataSet.colors.size) {
            if(i == highlight.x.toInt()) dataSet.colors[i] = UIConfig.uiPrimaryColor
            else resetPieChartEntryColor(i)
        }
        val pieChartElement = entry.data as PieChartElement
        if (pieChartElement.mcc == null) notifyAdapterCategorySpendingSelected(groupedCategories)
        else notifyAdapterCategorySpendingSelected(arrayListOf(pieChartElement.categorySpending))
        entry.icon.setColorFilter(UIConfig.uiTertiaryColor, PorterDuff.Mode.SRC_ATOP)
        tv_center_text_title.text = context?.let { pieChartElement.mcc?.toString(it) ?: getString(R.string.ellipsis) }
        tv_center_text_amount.text = pieChartElement.categorySpending.spending.toString()
        pieChartElement.categorySpending.difference?.let {
            tv_center_text_difference.show()
            val difference = it.toFloat()
            if (difference == 0F || context == null) return
            if (difference > 0F) {
                tv_center_text_difference.text = "+$it%"
                tv_center_text_difference.setTextColor(UIConfig.textMessageColor)
                tv_center_text_difference.background.setColorFilter(UIConfig.statsDifferenceIncreaseBackgroundColor, PorterDuff.Mode.SRC_ATOP)
            } else {
                tv_center_text_difference.text = "$it%"
                tv_center_text_difference.setTextColor(UIConfig.textMessageColor)
                tv_center_text_difference.background.setColorFilter(UIConfig.statsDifferenceDecreaseBackgroundColor, PorterDuff.Mode.SRC_ATOP)
            }
        } ?: tv_center_text_difference.remove()
    }

    private fun notifyAdapterCategorySpendingSelected(categorySpendingList: ArrayList<CategorySpending>) {
        categoryListAdapter.clearSelectedCategory()
        categorySpendingList.forEach { categoryListAdapter.selectCategorySpending(it) }
        categoryListAdapter.notifyDataSetChanged()
    }

    private fun resetPieChartEntryColor(index: Int) {
        dataSet.colors[index] = UIConfig.uiTertiaryColor
        dataSet.values[index].icon?.setColorFilter(UIConfig.iconSecondaryColor, PorterDuff.Mode.SRC_ATOP)
    }

    private fun setupCategoryListRecyclerView() {
        context?.let { context ->
            categoryListAdapter = CategoryListAdapter()
            categoryListAdapter.delegate = this
            rv_categories.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = categoryListAdapter
            }
        }
    }

    override fun onCategoryTapped(categorySpending: CategorySpending) {
        val mcc = MCC(name = null, icon = Icon.valueOf(categorySpending.categoryId.toUpperCase()))
        delegate?.onCategorySelected(mcc, startOfMonth, endOfMonth)
    }

    private val startOfMonth: LocalDate
        get() = date.withDayOfMonth(1)
    private val endOfMonth: LocalDate
        get() = startOfMonth.plusMonths(1).withDayOfMonth(1).minusDays(1)

    companion object {
        fun newInstance(cardID: String, date: LocalDate) = CardTransactionsChartThemeTwo().apply {
            this.cardID = cardID
            this.date = date
        }
    }
}
