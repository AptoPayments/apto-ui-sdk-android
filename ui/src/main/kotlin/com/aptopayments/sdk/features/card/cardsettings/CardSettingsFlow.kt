package com.aptopayments.sdk.features.card.cardsettings

import com.aptopayments.mobile.data.card.Disclaimer
import com.aptopayments.mobile.data.config.ContextConfiguration
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.data.voip.Action
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.extension.localized
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseDialogFragment
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.usecase.AcceptAchDisclaimerUseCase
import com.aptopayments.sdk.core.usecase.AcceptAchDisclaimerUseCase.Params
import com.aptopayments.sdk.core.usecase.DeclineAchDisclaimerUseCase
import com.aptopayments.sdk.features.card.orderphysical.OrderPhysicalFlow
import com.aptopayments.sdk.features.card.passcode.CardPasscodeFlow
import com.aptopayments.sdk.features.card.setpin.SetCardPinFlow
import com.aptopayments.sdk.features.card.statements.StatementListFlow
import com.aptopayments.sdk.features.contentpresenter.ContentPresenterContract
import com.aptopayments.sdk.features.loadfunds.AddFundsFlow
import com.aptopayments.sdk.features.directdeposit.instructions.DirectDepositInstructionsContract
import com.aptopayments.sdk.features.disclaimer.DisclaimerContract
import com.aptopayments.sdk.features.disclaimer.DisclaimerFragment
import com.aptopayments.sdk.features.loadfunds.dialog.AddFundsSelectorDialogContract
import com.aptopayments.sdk.features.voip.VoipFlow
import com.aptopayments.sdk.utils.MessageBanner
import org.koin.core.inject

private const val CARD_SETTINGS_TAG = "CardSettingsFragment"
private const val CONTENT_PRESENTER_TAG = "ContentPresenterFragment"
private const val ADD_FUNDS_SELECTOR_DIALOG_TAG = "AddFundsSelectorDialogFragment"
private const val DIRECT_DEPOSIT_INSTRUCTIONS_TAG = "DirectDepositInstructionsFragment"
private const val DISCLAIMER_TAG = "DisclaimerFragment"

internal class CardSettingsFlow(
    val cardId: String,
    val onClose: () -> Unit,
    private val contextConfiguration: ContextConfiguration,
    private val onCardStateChanged: () -> Unit,
    private val onTransactionsChanged: () -> Unit,
) : Flow(),
    CardSettingsContract.Delegate,
    ContentPresenterContract.Delegate,
    AddFundsSelectorDialogContract.Delegate,
    DirectDepositInstructionsContract.Delegate,
    DisclaimerContract.Delegate {

    private val aptoPlatformProtocol: AptoPlatformProtocol by inject()

    private val acceptAchDisclaimerUseCase: AcceptAchDisclaimerUseCase by inject()
    private val declineAchDisclaimerUseCase: DeclineAchDisclaimerUseCase by inject()

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        showLoading()
        aptoPlatformProtocol.fetchCard(cardId = cardId, forceRefresh = false) { result ->
            result.either(
                { onInitComplete(Either.Left(it)) },
                { card ->
                    card.cardProductID?.let { cardProductId ->
                        aptoPlatformProtocol.fetchCardProduct(cardProductId, forceRefresh = false) {
                            hideLoading()
                            it.either(::handleFailure) { cardProduct ->
                                val fragment = fragmentFactory.cardSettingsFragment(
                                    card = card,
                                    cardProduct = cardProduct,
                                    projectConfiguration = contextConfiguration.projectConfiguration,
                                    tag = CARD_SETTINGS_TAG
                                )
                                fragment.delegate = this
                                setStartElement(fragment as BaseFragment)
                                onInitComplete(Unit.right())
                            }
                        }
                    }
                }
            )
        }
    }

    override fun restoreState() {
        (fragmentWithTag(CARD_SETTINGS_TAG) as? CardSettingsContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(CONTENT_PRESENTER_TAG) as? ContentPresenterContract.View)?.let {
            it.delegate = this
        }
        (fragmentDialogWithTag(ADD_FUNDS_SELECTOR_DIALOG_TAG) as? AddFundsSelectorDialogContract.View)?.let {
            it.delegate = this
        }
        (fragmentDialogWithTag(DIRECT_DEPOSIT_INSTRUCTIONS_TAG) as? DirectDepositInstructionsContract.View)?.let {
            it.delegate = this
        }
        (fragmentDialogWithTag(DISCLAIMER_TAG) as? DisclaimerContract.View)?.let {
            it.delegate = this
        }
    }

    override fun onBackFromCardSettings() = onClose()

    override fun showContentPresenter(content: Content, title: String) {
        val fragment = fragmentFactory.contentPresenterFragment(content, title, CONTENT_PRESENTER_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun transactionsChanged() {
        onTransactionsChanged.invoke()
    }

    override fun onCloseTapped() = popFragment()

    override fun onCardStateChanged() {
        onCardStateChanged.invoke()
    }

    override fun onSetPin() {
        val flow = SetCardPinFlow(
            cardId = cardId,
            onBack = { popFlow(animated = true) },
            onFinish = {
                popFlow(animated = true)
                rootActivity()?.let {
                    notify(
                        title = "manage_card.confirm_pin.pin_updated.title".localized(),
                        message = "manage_card.confirm_pin.pin_updated.message".localized(),
                        messageType = MessageBanner.MessageType.HEADS_UP
                    )
                }
            }
        )
        flow.init { initResult ->
            initResult.either(::handleFailure) { push(flow = flow) }
        }
    }

    override fun showVoip(action: Action) {
        val flow = VoipFlow(
            cardId = cardId, action = action, onBack = { popFlow(animated = true) },
            onFinish = { popFlow(animated = true) }
        )
        flow.init { initResult ->
            initResult.either(::handleFailure) { push(flow = flow) }
        }
    }

    override fun showStatement() {
        val flow = StatementListFlow(
            onBack = { popAnimatedFlow() },
            onFinish = { popAnimatedFlow() }
        )
        flow.init { initResult -> initResult.either(::handleFailure) { push(flow) } }
    }

    override fun onAddFunds() {
        startAddFundsFlow()
    }

    private fun startAddFundsFlow() {
        val flow = AddFundsFlow(
            cardId = cardId,
            onClose = { popFlow(true) }
        )
        flow.init { initResult -> initResult.either(::handleFailure) { push(flow) } }
    }

    override fun onSetCardPasscode() {
        val flow = CardPasscodeFlow(cardId = cardId) { popAnimatedFlow() }
        flow.init { initResult -> initResult.either(::handleFailure) { push(flow) } }
    }

    override fun showAddFundsSelector() {
        val fragment = fragmentFactory.addFundsSelectorDialogFragment(ADD_FUNDS_SELECTOR_DIALOG_TAG)
        fragment.delegate = this
        push(fragment as BaseDialogFragment)
    }

    override fun showAddFundsDisclaimer(disclaimer: Disclaimer?) {
        val fragment = fragmentFactory.disclaimerFragment(
            disclaimer?.content!!,
            DisclaimerFragment.Configuration(
                screenTitle = "load_funds_direct_deposit_disclaimer_title",
                screenAcceptAgreement = "load_funds_direct_deposit_disclaimer_accept_action_button",
                screenRejectAgreement = "load_funds_direct_deposit_disclaimer_cancel_action_button",
                alertTitle = "load_funds_direct_deposit_disclaimer_alert_title",
                alertText = "load_funds_direct_deposit_disclaimer_alert_message"
            ),
            DISCLAIMER_TAG
        )
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun showOrderPhysicalCard() {
        val flow = OrderPhysicalFlow(cardId = cardId) { popAnimatedFlow() }
        flow.init { initResult -> initResult.either(::handleFailure) { push(flow) } }
    }

    override fun onDisclaimerAccepted() {
        showLoading()
        acceptAchDisclaimerUseCase.invoke(Params(cardId)) { result ->
            result.either(
                { handleFailure(it) },
                {
                    aptoPlatformProtocol.fetchCard(cardId, true) { result ->
                        hideLoading()
                        popFragment()
                        result.runIfRight { showAddFundsSelector() }
                    }
                }
            )
        }
    }

    override fun onDisclaimerDeclined() {
        showLoading()
        declineAchDisclaimerUseCase.invoke(DeclineAchDisclaimerUseCase.Params(cardId)) {
            hideLoading()
            popFragment()
            startAddFundsFlow()
        }
    }

    override fun addFundsSelectorCardClicked() {
        popDialogFragmentWithTag(ADD_FUNDS_SELECTOR_DIALOG_TAG)
        startAddFundsFlow()
    }

    override fun addFundsSelectorAchClicked() {
        popDialogFragmentWithTag(ADD_FUNDS_SELECTOR_DIALOG_TAG)

        val fragment = fragmentFactory.directDepositInstructionsFragment(cardId, DIRECT_DEPOSIT_INSTRUCTIONS_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun onBackFromDirectDepositInstructions() {
        popFragment()
    }

    private fun popAnimatedFlow() {
        popFlow(true)
    }
}
