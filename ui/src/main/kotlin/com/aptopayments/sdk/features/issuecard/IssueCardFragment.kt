package com.aptopayments.sdk.features.issuecard

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.workflowaction.WorkflowActionConfigurationIssueCard
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentIssueCardBinding
import com.aptopayments.sdk.utils.extensions.parseHtmlLinks
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val CARD_APPLICATION_ID = "application_id"
private const val ACTION_CONFIGURATION = "ACTION_CONFIGURATION"

internal class IssueCardFragment : BaseFragment(), IssueCardContract.View {

    private val viewModel: IssueCardViewModel by viewModel { parametersOf(cardApplicationId, actionConfiguration) }
    override var delegate: IssueCardContract.Delegate? = null
    private lateinit var cardApplicationId: String
    private var actionConfiguration: WorkflowActionConfigurationIssueCard? = null
    private lateinit var binding: FragmentIssueCardBinding

    override fun layoutId() = R.layout.fragment_issue_card

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, layoutId(), container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        hideKeyboard()
        return binding.root
    }

    override fun setUpArguments() {
        cardApplicationId = requireArguments()[CARD_APPLICATION_ID] as String
        actionConfiguration = requireArguments()[ACTION_CONFIGURATION] as WorkflowActionConfigurationIssueCard?
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(card, ::onCardIssuedSucceeded)
            failure(failure) { handleFailure(it) }
            observeNotNullable(loading) { handleLoading(it) }
            observeNotNullable(secondaryCta) { setSecondaryCta(it) }
        }
    }

    private fun setSecondaryCta(it: String) {
        val value = it.localized()
        if (value.isNotEmpty()) {
            themeManager().customizeHtml(binding.tvSecondaryCta, value.parseHtmlLinks())
        } else {
            binding.tvSecondaryCta.text = ""
        }
    }

    private fun onCardIssuedSucceeded(card: Card?) {
        card?.let { delegate?.onCardIssuedSucceeded(it) }
    }

    override fun setupUI() {
        with(themeManager()) {
            customizeLargeTitleLabel(binding.tvIssueCardErrorTitle)
            customizeRegularTextLabel(binding.tvDescription)
            customizeSubmitButton(binding.tvPrimaryCta)
        }
        binding.tvSecondaryCta.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun issueCard() {
        showLoading()
        viewModel.retryIssueCard()
    }

    companion object {
        fun newInstance(cardApplicationId: String, actionConfiguration: WorkflowActionConfigurationIssueCard?) =
            IssueCardFragment().apply {
                arguments = Bundle().apply {
                    putString(CARD_APPLICATION_ID, cardApplicationId)
                    putSerializable(ACTION_CONFIGURATION, actionConfiguration)
                }
            }
    }
}
