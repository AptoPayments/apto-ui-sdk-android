package com.aptopayments.sdk.features.card.statements

import android.view.Menu
import android.view.MenuInflater
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.statements.StatementMonth
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.extension.visibleIf
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.core.usecase.DownloadStatementUseCase
import com.aptopayments.sdk.data.StatementFile
import com.aptopayments.sdk.utils.ItemDecoratorFirstLast
import kotlinx.android.synthetic.main.fragment_statement_list.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class StatementListFragment : BaseFragment(), StatementListContract.View {

    override var delegate: StatementListContract.Delegate? = null
    private val viewModel: StatementListViewModel by viewModel()

    private val statementAdapter = StatementListAdapter(object : StatementListAdapter.Delegate {
        override fun onMonthTapped(month: StatementMonth) {
            showLoading()
            viewModel.onMonthTapped(month)
        }
    })

    private fun handleFileDownloaded(file: StatementFile) {
        hideLoading()
        delegate?.onStatementDownloaded(file)
    }

    override fun layoutId(): Int = R.layout.fragment_statement_list

    override fun setupViewModel() {
        viewModel.fetchStatementList()
        observe(viewModel.statementList, ::updateAdapter)
        observeNotNullable(viewModel.file) { file -> handleFileDownloaded(file) }
        observeNotNullable(viewModel.statementListEmpty) { configureEmptyStateVisibility(it) }
        failure(viewModel.failure) { handleFailure(it) }
    }

    override fun viewLoaded() {
        super.viewLoaded()
        viewModel.viewLoaded()
    }

    override fun handleFailure(failure: Failure?) {
        hideLoading()
        when (failure) {
            is DownloadStatementUseCase.StatementExpiredFailure -> notify(failure.errorMessage())
            is DownloadStatementUseCase.StatementDownloadFailure -> notify(failure.errorMessage())
            else -> super.handleFailure(failure)
        }
    }

    private fun updateAdapter(list: List<StatementListItem>?) {
        list?.let { statementAdapter.setData(it) }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setupToolbar() {
        val toolbar = tb_llsdk_toolbar as Toolbar
        toolbar.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        tb_llsdk_toolbar.setTitleTextColor(UIConfig.textTopBarSecondaryColor)

        val title = "monthly_statements.list.title".localized()
        val backButtonMode = BaseActivity.BackButtonMode.Back(null, UIConfig.textTopBarSecondaryColor)
        delegate?.configureToolbar(toolbar, title, backButtonMode)
    }

    override fun onBackPressed() {
        delegate?.onBackPressed()
    }

    private fun setUpTheme() {
        themeManager().customizeEmptyCase(statements_list_empty_message)
    }

    private fun configureEmptyStateVisibility(it: Boolean) {
        hideLoading()
        statements_list_empty_message.visibleIf(it)
    }

    companion object {
        fun newInstance() = StatementListFragment()
    }
}
