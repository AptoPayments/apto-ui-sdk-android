package com.aptopayments.sdk.features.card.statements

import androidx.recyclerview.widget.LinearLayoutManager
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.card.statements.StatementListViewModel.Action
import com.aptopayments.sdk.utils.ItemDecoratorFirstLast
import kotlinx.android.synthetic.main.fragment_statement_list.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class StatementListFragment : BaseFragment(), StatementListContract.View {

    override var delegate: StatementListContract.Delegate? = null
    private val viewModel: StatementListViewModel by viewModel()

    private val statementAdapter = StatementListAdapter(object : StatementListAdapter.Delegate {
        override fun onMonthTapped(month: StatementMonth) {
            viewModel.onMonthTapped(month)
        }
    })

    override fun layoutId(): Int = R.layout.fragment_statement_list

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setupViewModel() {
        observeNotNullable(viewModel.loading) { handleLoading(it) }
        observeNotNullable(viewModel.failure) { handleFailure(it) }
        observeNotNullable(viewModel.state) { handleStateChange(it) }
        observeNotNullable(viewModel.action) { action ->
            when (action) {
                is Action.OpenMonth -> delegate?.onStatementPressed(action.statementMonth)
            }
        }
    }

    private fun handleStateChange(state: StatementListViewModel.State) {
        statements_list_empty_message.visibleIf(state.showEmpty)
        statements_recycler_view.goneIf(state.showEmpty)
        statementAdapter.setData(state.list)
    }

    override fun setupUI() {
        setupToolbar()
        setUpTheme()
        setupRecycler()
    }

    private fun setupRecycler() {
        val linearLayoutManager = LinearLayoutManager(context)
        val offset = resources.getDimensionPixelSize(R.dimen._16sdp)

        statements_recycler_view.apply {
            layoutManager = linearLayoutManager
            adapter = statementAdapter
            addItemDecoration(ItemDecoratorFirstLast(offset))
        }
    }

    private fun setupToolbar() {
        val title = "monthly_statements.list.title".localized()
        val backButtonMode = BackButtonMode.Back(UIConfig.textTopBarSecondaryColor)
        tb_llsdk_toolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .backButtonMode(backButtonMode)
                .title(title)
                .setSecondaryColors()
                .build()
        )
    }

    override fun onBackPressed() {
        delegate?.onBackPressed()
    }

    private fun setUpTheme() {
        themeManager().customizeEmptyCase(statements_list_empty_message)
    }

    companion object {
        fun newInstance() = StatementListFragment()
    }
}
