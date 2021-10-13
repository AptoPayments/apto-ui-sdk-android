package com.aptopayments.sdk.features.card.statements.detail

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.extensions.SnackbarMessageType
import kotlinx.android.synthetic.main.fragment_statement_details.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.KoinComponent
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalDate
import java.io.File

private const val STATEMENT_MONTH_KEY = "statement_month"

internal class StatementDetailFragment :
    BaseFragment(),
    StatementDetailContract.View,
    KoinComponent {

    override var delegate: StatementDetailContract.Delegate? = null
    private lateinit var statementMonth: StatementMonth
    private val viewModel: StatementDetailViewModel by viewModel { parametersOf(statementMonth) }

    override fun layoutId() = R.layout.fragment_statement_details

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setUpArguments() {
        statementMonth = requireArguments()[STATEMENT_MONTH_KEY] as StatementMonth
    }

    override fun setupViewModel() {
        observeNotNullable(viewModel.loading) { handleLoading(it) }
        observeNotNullable(viewModel.failure) { handleFailure(it) }
        observeNotNullable(viewModel.state) { loadAndConfigurePdf(it.file) }
        observeNotNullable(viewModel.action) {
            when (it) {
                is StatementDetailViewModel.Action.ShowDownloadingSign ->
                    notify(
                        "monthly_statements_report_downloading".localized(),
                        SnackbarMessageType.HEADS_UP
                    )
            }
        }
    }

    private fun loadAndConfigurePdf(file: File?) {
        file?.let {
            pdfView.fromFile(file).show()
        }
    }

    override fun setupUI() {
        setUpToolbar()
    }

    private fun tintMenuItem() {
        tb_llsdk_toolbar?.menu?.let {
            themeManager().customizeMenuImage(it.findItem(R.id.menu_save_pdf))
        }
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save_pdf -> {
                onSaveFileToPhone()
                true
            }
            else -> false
        }
    }

    private fun onSaveFileToPhone() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) { granted ->
                if (granted) {
                    viewModel.downloadToPhone()
                } else {
                    notify("monthly_statements_report_permission_error".localized(), SnackbarMessageType.ERROR)
                }
            }
        } else {
            viewModel.downloadToPhone()
        }
    }

    private fun setUpToolbar() {
        tb_llsdk_toolbar?.apply {
            inflateMenu(R.menu.menu_statement_download)
            setOnMenuItemClickListener { onMenuItemSelected(it) }
            tintMenuItem()
        }
        tb_llsdk_toolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .backButtonMode(BackButtonMode.Back(UIConfig.textTopBarSecondaryColor))
                .title(getDownloadTitle(statementMonth))
                .setSecondaryColors()
                .build()
        )
    }

    private fun getDownloadTitle(month: StatementMonth): String {
        val date = LocalDate.of(month.year, month.month, 1)
        return "monthly_statements_report_title".localized()
            .replace("<<MONTH>>", date.monthLocalized())
            .replace("<<YEAR>>", date.year.toString())
    }

    override fun onBackPressed() {
        delegate?.onPdfBackPressed()
    }

    companion object {
        fun newInstance(statementMonth: StatementMonth) = StatementDetailFragment().apply {
            arguments = Bundle().apply {
                putSerializable(STATEMENT_MONTH_KEY, statementMonth)
            }
        }
    }
}
