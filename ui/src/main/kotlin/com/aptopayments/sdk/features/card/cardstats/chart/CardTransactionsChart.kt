package com.aptopayments.sdk.features.card.cardstats.chart

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.aptopayments.mobile.data.card.Money
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.stats.CategorySpending
import com.aptopayments.mobile.data.transaction.MCC
import com.aptopayments.mobile.data.transaction.MCC.Icon
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.iconResource
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.extensions.setColorFilterCompat
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.android.synthetic.main.fragment_transactions_chart.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalDate
import java.util.Locale

private const val CARD_ID = "card_id_bundle"
private const val DATE = "date_bundle"

internal class CardTransactionsChart :
    BaseFragment(),
    CardTransactionsChartContract.View,
    OnChartValueSelectedListener,
    CategoryListAdapter.Delegate {

    private lateinit var pieChart: AptoPieChart
    private lateinit var cardID: String
    private lateinit var date: LocalDate
    private val viewModel: CardTransactionsChartViewModel by viewModel { parametersOf(cardID, date) }
    private lateinit var categoryListAdapter: CategoryListAdapter
    private var dataSet: PieDataSet = PieDataSet(null, "")
    private var totalSpent = 0.0
    private var currency: String? = null
    private var groupedCategories = ArrayList<CategorySpending>()
    override var delegate: CardTransactionsChartContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_transactions_chart

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setUpArguments() {
        super.setUpArguments()
        cardID = requireArguments().getString(CARD_ID)!!
        date = requireArguments().getSerializable(DATE) as LocalDate
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(failure) { handleFailure(it) }
            observeNotNullable(categorySpending) {
                updateChartData(it)
                showViews()
            }
            observeNotNullable(hasMonthlyStatement) {
                monthly_statement_link?.invisibleIf(!it)
                monthly_statement_top_separator?.invisibleIf(!it)
            }
        }
    }

    override fun setupUI() {
        setupTheme()
        setupChart()
        setupCategoryListRecyclerView()
        setClickListeners()
    }

    private fun setClickListeners() {
        monthly_statement_link.setOnClickListenerSafe { delegate?.onStatementTapped(date.monthValue, date.year) }
    }

    private fun showViews() {
        chart?.show()
        ll_chart_text?.show()
        transaction_list_top_separator?.show()
        tv_list_title?.show()
        transaction_list_bottom_separator?.show()
        rv_categories?.show()
        monthly_statement_link_container?.show()
        transaction_list_top_separator?.show()
    }

    private fun setupTheme() {
        with(themeManager()) {
            customizeTimestamp(tv_center_text_title)
            customizeMainItem(tv_center_text_amount)
            customizeStarredSectionTitle(tv_list_title, UIConfig.textSecondaryColor)
            customizeEmptyCase(tv_no_transactions)
            customizeSectionTitle(tv_center_text_difference)
            customizeFormTextLink(monthly_statement_link)
        }
        tv_center_text_difference.setTextColor(UIConfig.textMessageColor)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pieChart = view.findViewById(R.id.chart)
        super.onViewCreated(view, savedInstanceState)
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
        pieChart.setHoleColor(UIConfig.uiBackgroundSecondaryColor)
        pieChart.invalidate()
    }

    private fun updateChartData(spendingList: List<CategorySpending>) {
        tv_no_transactions.visibleIf(spendingList.isNullOrEmpty())
        drawPieChartEntries(calculatePieChartEntries(spendingList))
        currency = calculateSpendingListCurrency(spendingList)
        categoryListAdapter.categorySpendingList = spendingList.map { CategoryListItem(it, false) }
        tv_center_text_amount.text = Money(amount = totalSpent, currency = currency).toString()
    }

    private fun calculateSpendingListCurrency(spendingList: List<CategorySpending>): String? {
        return if (spendingList.isEmpty()) {
            "stats.monthly_spending.graph.default_currency".localized()
        } else {
            spendingList.first().spending?.currency
        }
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
        return if (spendingList.isEmpty()) {
            ArrayList()
        } else {
            val entries = generateEntriesAndUpdateTotalSpending(spendingList)
            groupSmallEntries(entries)
        }
    }

    private fun generateEntriesAndUpdateTotalSpending(spendingList: List<CategorySpending>): ArrayList<PieEntry> {
        val entries = ArrayList<PieEntry>()
        totalSpent = 0.0
        spendingList.forEachIndexed { index, categorySpending ->
            categorySpending.spending?.currency?.let { currency = it }
            categorySpending.spending?.amount?.toFloat()?.let {
                val mcc = MCC(categorySpending.categoryId)
                val icon = context?.let { context -> ContextCompat.getDrawable(context, mcc.iconResource) }
                icon?.setColorFilterCompat(UIConfig.iconSecondaryColor, PorterDuff.Mode.SRC_ATOP)
                entries.add(index, PieEntry(it, icon, PieChartElement(categorySpending, mcc)))
                totalSpent += it
            }
        }
        return entries
    }

    private fun groupSmallEntries(entries: ArrayList<PieEntry>): ArrayList<PieEntry> {
        // Values below 7% of the total will be grouped
        val maxSpendingForSmallCategory = totalSpent * 0.07
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
        entries.add(
            PieEntry(
                totalSpendingOfSmallCategories.toFloat(),
                emptyDrawable,
                PieChartElement(CategorySpending("", spending))
            )
        )
        return entries
    }

    override fun setupListeners() {
        super.setupListeners()
        pieChart.setOnChartValueSelectedListener(this)
    }

    override fun onNothingSelected() {
        tv_center_text_title.localizedText = "stats.monthly_spending.graph.title"
        tv_center_text_difference.remove()
        tv_center_text_amount.text = Money(amount = totalSpent, currency = currency).toString()
        for (i in 0 until dataSet.colors.size) resetPieChartEntryColor(i)
    }

    @SuppressLint("SetTextI18n")
    override fun onValueSelected(entry: Entry?, highlight: Highlight?) {
        if (entry == null || highlight == null || entry.data == null) return
        for (i in 0 until dataSet.colors.size) {
            if (i == highlight.x.toInt()) dataSet.colors[i] = UIConfig.uiPrimaryColor
            else resetPieChartEntryColor(i)
        }
        val pieChartElement = entry.data as PieChartElement
        if (pieChartElement.mcc == null) notifyAdapterCategorySpendingSelected(groupedCategories)
        else notifyAdapterCategorySpendingSelected(arrayListOf(pieChartElement.categorySpending))
        entry.icon.setColorFilterCompat(UIConfig.uiTertiaryColor, PorterDuff.Mode.SRC_ATOP)
        tv_center_text_title.text = pieChartElement.mcc?.toLocalizedString() ?: getString(R.string.ellipsis)
        tv_center_text_amount.text = pieChartElement.categorySpending.spending.toString()
        tv_center_text_difference.visibleIf(pieChartElement.categorySpending.difference != null)
        pieChartElement.categorySpending.difference?.let {
            val difference = it.toFloat()
            if (difference != 0F && context != null) {
                tv_center_text_difference.setTextColor(UIConfig.textMessageColor)
                if (difference > 0F) {
                    tv_center_text_difference.text = "+$it%"
                    tv_center_text_difference.background.setColorFilterCompat(
                        UIConfig.statsDifferenceIncreaseBackgroundColor,
                        PorterDuff.Mode.SRC_ATOP
                    )
                } else {
                    tv_center_text_difference.text = "$it%"
                    tv_center_text_difference.background.setColorFilterCompat(
                        UIConfig.statsDifferenceDecreaseBackgroundColor,
                        PorterDuff.Mode.SRC_ATOP
                    )
                }
            }
        }
    }

    private fun notifyAdapterCategorySpendingSelected(categorySpendingList: ArrayList<CategorySpending>) {
        categoryListAdapter.clearSelectedCategory()
        categorySpendingList.forEach { categoryListAdapter.selectCategorySpending(it) }
        categoryListAdapter.notifyDataSetChanged()
    }

    private fun resetPieChartEntryColor(index: Int) {
        dataSet.colors[index] = UIConfig.uiTertiaryColor
        dataSet.values[index].icon?.setColorFilterCompat(UIConfig.iconSecondaryColor, PorterDuff.Mode.SRC_ATOP)
    }

    private fun setupCategoryListRecyclerView() {
        context?.let { context ->
            categoryListAdapter =
                CategoryListAdapter()
            categoryListAdapter.delegate = this
            rv_categories.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = categoryListAdapter
            }
        }
    }

    override fun onCategoryTapped(categorySpending: CategorySpending) {
        val mcc = MCC(name = null, icon = Icon.valueOf(categorySpending.categoryId.toUpperCase(Locale.US)))
        delegate?.onCategorySelected(mcc, startOfMonth, endOfMonth)
    }

    private val startOfMonth: LocalDate
        get() = date.withDayOfMonth(1)
    private val endOfMonth: LocalDate
        get() = startOfMonth.plusMonths(1).withDayOfMonth(1).minusDays(1)

    companion object {
        fun newInstance(cardID: String, date: LocalDate) = CardTransactionsChart().apply {
            this.arguments = Bundle().apply {
                putSerializable(CARD_ID, cardID)
                putSerializable(DATE, date)
            }
        }
    }
}
