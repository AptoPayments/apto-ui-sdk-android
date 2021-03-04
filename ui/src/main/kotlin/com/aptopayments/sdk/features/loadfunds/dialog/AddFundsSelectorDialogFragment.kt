package com.aptopayments.sdk.features.loadfunds.dialog

import android.os.Bundle
import android.view.View
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseDialogFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.aptopayments.sdk.features.loadfunds.dialog.AddFundsSelectorDialogViewModel.Actions
import com.aptopayments.sdk.utils.extensions.setValue
import kotlinx.android.synthetic.main.fragment_load_funds_selector_dialog.*

internal class AddFundsSelectorDialogFragment :
    BaseDialogFragment(), AddFundsSelectorDialogContract.View {

    override var delegate: AddFundsSelectorDialogContract.Delegate? = null
    private val viewModel: AddFundsSelectorDialogViewModel by viewModel()

    override fun layoutId() = R.layout.fragment_load_funds_selector_dialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        positionDialog()
    }

    override fun setUpUI() {
        load_funds_selector_ach.setOnClickListener { viewModel.onAchClicked() }
        load_funds_selector_card.setOnClickListener { viewModel.onCardClicked() }
        with(themeManager()) {
            customizeHighlightTitleLabel(load_funds_selector_title)
            view?.let { customizeRoundedBackground(it) }
        }
    }

    override fun setUpViewModel() {
        observeNotNullable(viewModel.action) { action ->
            when (action) {
                is Actions.CardClicked -> delegate?.addFundsSelectorCardClicked()
                is Actions.AchClicked -> delegate?.addFundsSelectorAchClicked()
            }
        }
    }

    companion object {
        fun newInstance(tag: String) = AddFundsSelectorDialogFragment().apply {
            TAG = tag
        }
    }
}
