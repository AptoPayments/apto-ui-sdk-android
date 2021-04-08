package com.aptopayments.sdk.features.directdeposit.details

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseDialogFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.directdeposit.details.AchAccountDetailsViewModel.Action
import kotlinx.android.synthetic.main.fragment_ach_accout_details_dialog.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import com.aptopayments.sdk.utils.extensions.copyToClipboard
import com.aptopayments.sdk.utils.extensions.setValue

private const val CARD_ID_KEY = "CARD_ID_KEY"

internal class AchAccountDetailsDialogFragment :
    BaseDialogFragment(), AchAccountDetailsDialogContract.View {
    private lateinit var cardId: String
    override var delegate: AchAccountDetailsDialogContract.Delegate? = null

    private val viewModel: AchAccountDetailsViewModel by viewModel { parametersOf(cardId) }

    override fun layoutId() = R.layout.fragment_ach_accout_details_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cardId = requireArguments()[CARD_ID_KEY] as String
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        positionDialog()
    }

    override fun setUpUI() {
        setCopyLongClickListeners()
        with(themeManager()) {
            customizeHighlightTitleLabel(account_details_title)
            customizeMainItem(account_details_account_number_title)
            customizeMainItem(account_details_routing_number_title)
            customizeMainItemRight(account_details_account_number_details)
            customizeMainItemRight(account_details_routing_details)
            view?.let { customizeRoundedBackground(it) }
        }
    }

    private fun setCopyLongClickListeners() {
        account_details_account_container.setOnLongClickListener {
            viewModel.accountLongClick()
            true
        }
        account_details_routing_container.setOnLongClickListener {
            viewModel.routingLongClick()
            true
        }
    }

    @SuppressLint("SetTextI18n")
    override fun setUpViewModel() {
        observeNotNullable(viewModel.failure) { handleFailure(it) }
        observeNotNullable(viewModel.actions) {
            when (it) {
                is Action.CopyValueToClipboard -> copyValue(label = it.label, value = it.value)
            }
        }
        observeNotNullable(viewModel.achAccountDetails) {
            account_details_account_number_details.text = it.accountNumber
            account_details_routing_details.text = it.routingNumber
        }
    }

    private fun copyValue(label: String, value: String) {
        requireContext().copyToClipboard(label = label, value = value)
        Toast.makeText(
            requireContext(),
            "load_funds_ach_account_details_copied_to_clipboard".localized(),
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        fun newInstance(cardId: String, tag: String) = AchAccountDetailsDialogFragment().apply {
            arguments = Bundle().apply {
                putSerializable(CARD_ID_KEY, cardId)
            }
            TAG = tag
        }
    }
}
