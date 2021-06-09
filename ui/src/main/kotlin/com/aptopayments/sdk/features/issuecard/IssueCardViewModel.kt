package com.aptopayments.sdk.features.issuecard

import androidx.lifecycle.MutableLiveData
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.workflowaction.WorkflowActionConfigurationIssueCard
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.exception.server.ErrorBalanceValidationsEmailSendsDisabled
import com.aptopayments.mobile.exception.server.ErrorBalanceValidationsInsufficientApplicationLimit
import com.aptopayments.mobile.exception.server.ErrorInsufficientFunds
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.repository.InitializationDataRepository

private const val ERROR_GENERIC = "GENERIC"
private const val ERROR_INSUFFICIENT_FUNDS = "INSUFFICIENT_FUNDS"
private const val ERROR_INSUFFICIENT_APPLICATION_LIMITS = "INSUFFICIENT_APPLICATION_LIMITS"
private const val ERROR_BALANCE_VALIDATIONS_EMAIL_SENDS = "BALANCE_VALIDATIONS_EMAIL_SENDS"

internal class IssueCardViewModel(
    private val cardApplicationId: String,
    private val actionConfiguration: WorkflowActionConfigurationIssueCard?,
    private val analyticsManager: AnalyticsServiceContract,
    private val aptoPlatform: AptoPlatformProtocol,
    private val initializationDataRepository: InitializationDataRepository
) : BaseViewModel() {

    var card: MutableLiveData<Card> = MutableLiveData()
    val image = MutableLiveData("")
    val title = MutableLiveData("")
    val description = MutableLiveData("")
    val primaryCta = MutableLiveData("")
    val secondaryCta = MutableLiveData("")
    val errorVisible = MutableLiveData(false)

    init {
        issueCard()
    }

    fun retryIssueCard() {
        issueCard()
    }

    private fun issueCard() {
        showLoading()
        errorVisible.value = false
        analyticsManager.track(Event.IssueCard)
        aptoPlatform.issueCard(
            applicationId = cardApplicationId,
            metadata = initializationDataRepository.data?.cardMetadata,
            design = initializationDataRepository.data?.design
        ) { result ->
            hideLoading()
            result.either(
                { handleIssueCardFailure(it) },
                {
                    initializationDataRepository.data?.cardMetadata = null
                    initializationDataRepository.data?.design = null
                    card.postValue(it)
                }
            )
        }
    }

    private fun handleIssueCardFailure(failure: Failure) {
        errorVisible.value = true
        if (failure is Failure.ServerError) {
            trackError(failure)
            setErrorCopiesAndImages(failure)
        } else {
            handleFailure(failure)
        }
    }

    private fun trackError(error: Failure.ServerError) {
        val event = when (error) {
            is ErrorInsufficientFunds -> Event.IssueCardInsufficientFunds
            is ErrorBalanceValidationsInsufficientApplicationLimit -> Event.IssueCardInsufficientApplicationLimit
            is ErrorBalanceValidationsEmailSendsDisabled -> Event.IssueCardEmailSendsDisabled
            else -> Event.IssueCardUnknownError
        }
        analyticsManager.track(event, error.toJSonObject())
    }

    private fun setErrorCopiesAndImages(error: Failure.ServerError) {
        image.value = actionConfiguration?.errorAsset
        when (error) {
            is ErrorInsufficientFunds -> setText(ERROR_INSUFFICIENT_FUNDS)
            is ErrorBalanceValidationsInsufficientApplicationLimit -> setText(ERROR_INSUFFICIENT_APPLICATION_LIMITS)
            is ErrorBalanceValidationsEmailSendsDisabled -> setText(ERROR_BALANCE_VALIDATIONS_EMAIL_SENDS)
            else -> setText(ERROR_GENERIC)
        }
    }

    private fun setText(key: String) {
        this.title.value = ERROR_STRINGS_MAP[key]?.get(0) ?: ""
        this.description.value = ERROR_STRINGS_MAP[key]?.get(1) ?: ""
        this.primaryCta.value = ERROR_STRINGS_MAP[key]?.get(2) ?: ""
        this.secondaryCta.value = ERROR_STRINGS_MAP[key]?.get(3) ?: ""
    }

    companion object {
        private val STRINGS_GENERIC = arrayOf(
            "issue_card_issue_card_generic_error_title",
            "issue_card_issue_card_generic_error_description",
            "issue_card_issue_card_generic_error_primary_cta",
            "issue_card_issue_card_generic_error_secondary_cta"
        )
        private val STRINGS_INSUFFICIENT_FUNDS = arrayOf(
            "issue_card_issue_card_error_insufficient_funds_title",
            "issue_card_issue_card_error_insufficient_funds_description",
            "issue_card_issue_card_error_insufficient_funds_primary_cta",
            "issue_card_issue_card_error_insufficient_funds_secondary_cta"
        )
        private val STRINGS_INSUFFICIENT_APPLICATION_LIMITS = arrayOf(
            "issue_card_issue_card_error_insufficient_application_limit_title",
            "issue_card_issue_card_error_insufficient_application_limit_description",
            "issue_card_issue_card_error_insufficient_application_limit_primary_cta",
            "issue_card_issue_card_error_insufficient_application_limit_secondary_cta"
        )
        private val STRINGS_BALANCE_VALIDATIONS_EMAIL_SENDS = arrayOf(
            "issue_card_issue_card_error_email_sends_disabled_title",
            "issue_card_issue_card_error_email_sends_disabled_description",
            "issue_card_issue_card_error_email_sends_disabled_primary_cta",
            "issue_card_issue_card_error_email_sends_disabled_secondary_cta"
        )

        private val ERROR_STRINGS_MAP = mapOf(
            ERROR_GENERIC to STRINGS_GENERIC,
            ERROR_INSUFFICIENT_FUNDS to STRINGS_INSUFFICIENT_FUNDS,
            ERROR_INSUFFICIENT_APPLICATION_LIMITS to STRINGS_INSUFFICIENT_APPLICATION_LIMITS,
            ERROR_BALANCE_VALIDATIONS_EMAIL_SENDS to STRINGS_BALANCE_VALIDATIONS_EMAIL_SENDS
        )
    }
}
