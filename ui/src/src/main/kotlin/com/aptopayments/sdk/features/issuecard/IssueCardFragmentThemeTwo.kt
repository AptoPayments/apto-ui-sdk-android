package com.aptopayments.sdk.features.issuecard

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.platform.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier

private const val CARD_APPLICATION_ID = "application_id"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class IssueCardFragmentThemeTwo : BaseFragment(), IssueCardContract.View {

    private val viewModel: IssueCardViewModel by viewModel()
    override var delegate: IssueCardContract.Delegate? = null
    private lateinit var cardApplicationId: String

    override fun layoutId() = R.layout.fragment_issue_card_theme_two

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
        cardApplicationId = arguments!![CARD_APPLICATION_ID] as String
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(card, ::onCardIssuedSucceeded)
            observe(issueCardErrorCode, ::onIssueCardFailed)
            failure(failure) { handleFailure(it) }
        }
    }

    private fun onCardIssuedSucceeded(card: Card?) {
        card?.let {
            hideLoading()
            delegate?.onCardIssuedSucceeded(it)
        }
    }

    private fun onIssueCardFailed(issueCardErrorCode: Int?) {
        hideLoading()
        delegate?.onCardIssuedFailed(issueCardErrorCode)
    }

    override fun setupUI() {}

    override fun viewLoaded() {
        viewModel.viewLoaded()
        issueCard()
    }

    override fun issueCard() {
        showLoading()
        viewModel.issueCard(cardApplicationId)
    }

    companion object {
        fun newInstance(cardApplicationId: String) = IssueCardFragmentThemeTwo().apply {
            arguments = Bundle().apply { putString(CARD_APPLICATION_ID, cardApplicationId) }
        }
    }
}
