package com.aptopayments.sdk.features.card.cardstats

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.viewpager2.widget.ViewPager2
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.transaction.MCC
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.extension.invisibleIf
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.extension.yearToString
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import kotlinx.android.synthetic.main.fragment_card_monthly_stats.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalDate
import java.lang.reflect.Modifier

private const val CARD_ID_KEY = "CARD_ID"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class CardMonthlyStatsFragmentThemeTwo : BaseFragment(), CardMonthlyStatsContract.View,
    CardMonthlyStatsAdapter.Delegate {

    private lateinit var cardId: String
    private val viewModel: CardMonthlyStatsViewModel by viewModel { parametersOf(cardId) }
    private lateinit var adapter: CardMonthlyStatsAdapter

    override var delegate: CardMonthlyStatsContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_card_monthly_stats

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setUpArguments() {
        cardId = arguments!![CARD_ID_KEY] as String
    }

    override fun handleFailure(failure: Failure?) {
        hideLoading()
        when (failure) {
            is Failure.FeatureFailure -> notify(failure.errorMessage())
            else -> super.handleFailure(failure)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun setupUI() {
        with(themeManager()) {
            customizeTimestamp(title_previous_month)
            customizeTimestamp(title_next_month)
            customizeSectionTitle(title_current_month)
        }
        setupToolBar()
    }

    private fun setupToolBar() {
        tb_llsdk_toolbar.setTitleTextColor(UIConfig.textTopBarSecondaryColor)
        tb_llsdk_toolbar.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        delegate?.configureToolbar(
            toolbar = tb_llsdk_toolbar,
            title = "stats.monthly_spending.title".localized()
                .replace("<<YEAR>>", LocalDate.now().yearToString()),
            backButtonMode = BaseActivity.BackButtonMode.Back(null, UIConfig.textTopBarSecondaryColor)
        )
    }

    override fun setupViewModel() {
        viewModel.apply { failure(failure) { handleFailure(it) } }
        observeNotNullable(viewModel.previousMonthName) { configureMonthTitle(title_previous_month, it) }
        observeNotNullable(viewModel.currentMonthName) { configureMonthTitle(title_current_month, it) }
        observeNotNullable(viewModel.nextMonthName) { configureMonthTitle(title_next_month, it) }
        observeNotNullable(viewModel.addSpending) { adapter.addPage(it) }
        observeNotNullable(viewModel.loading) { handleLoading(it) }
    }

    override fun onBackPressed() {
        delegate?.onBackFromCardMonthlyStats()
    }

    override fun setupListeners() {
        title_previous_month.setOnClickListener { viewPager.setCurrentItem(viewPager.currentItem + 1, true) }
        title_next_month.setOnClickListener { viewPager.setCurrentItem(viewPager.currentItem - 1, true) }
    }

    override fun viewLoaded() {
        viewModel.viewLoaded()
        val now = LocalDate.now()
        val currentMonth = LocalDate.of(now.year, now.month, 1)
        adapter = CardMonthlyStatsAdapter(currentMonth, cardId, this)
        adapter.delegate = this
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.onMonthSelected(adapter.months[position])
            }
        })
    }

    private fun configureMonthTitle(textView: TextView, text: String) {
        textView.text = text
        textView.invisibleIf(text.isEmpty())
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
