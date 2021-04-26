package com.aptopayments.sdk.features.managecard

import androidx.annotation.VisibleForTesting
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.KycStatus
import com.aptopayments.mobile.data.config.ContextConfiguration
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseDialogFragment
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.features.addbalance.AddBalanceFlow
import com.aptopayments.sdk.features.card.account.AccountSettingsFlow
import com.aptopayments.sdk.features.card.activatephysicalcard.ActivatePhysicalCardFlow
import com.aptopayments.sdk.features.card.cardsettings.CardSettingsFlow
import com.aptopayments.sdk.features.card.cardstats.CardStatsFlow
import com.aptopayments.sdk.features.card.fundingsources.FundingSourceContract
import com.aptopayments.sdk.features.card.waitlist.WaitlistContract
import com.aptopayments.sdk.features.kyc.KycStatusFlow
import com.aptopayments.sdk.features.transactiondetails.TransactionDetailsContract
import org.koin.core.inject
import java.lang.reflect.Modifier

private const val MANAGE_CARD_TAG = "ManageCardFragment"
private const val TRANSACTION_DETAILS_TAG = "TransactionDetailsFragment"
private const val FUNDING_SOURCE_DIALOG_TAG = "FundingSourceDialogFragment"
private const val WAITLIST_TAG = "WaitlistFragment"

internal class ManageCardFlow(
    val cardId: String,
    val contextConfiguration: ContextConfiguration,
    var onClose: () -> Unit
) : Flow(),
    ManageCardContract.Delegate,
    FundingSourceContract.Delegate,
    TransactionDetailsContract.Delegate,
    WaitlistContract.Delegate {

    private val aptoPlatformProtocol: AptoPlatformProtocol by inject()

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    val manageCardFragment: ManageCardContract.View?
        get() = fragmentWithTag(MANAGE_CARD_TAG) as? ManageCardContract.View

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        aptoPlatformProtocol.fetchCard(cardId = cardId, forceRefresh = false) { result ->
            result.either(
                { onInitComplete(Either.Left(it)) },
                { card ->
                    when (card.kycStatus) {
                        KycStatus.PASSED -> {
                            if (card.isWaitlisted == true) {
                                card.cardProductID?.let {
                                    aptoPlatformProtocol.fetchCardProduct(it, true) { getCardProductResult ->
                                        getCardProductResult.either(
                                            { onInitComplete(Either.Left(ManageCardInitFailure())) },
                                            { cardProduct ->
                                                val fragment =
                                                    fragmentFactory.waitlistFragment(
                                                        card.accountID,
                                                        cardProduct,
                                                        WAITLIST_TAG
                                                    )
                                                fragment.delegate = this
                                                setStartElement(fragment as BaseFragment)
                                                onInitComplete(Either.Right(Unit))
                                            }
                                        )
                                    }
                                } ?: onInitComplete(Either.Left(ManageCardInitFailure()))
                            } else {
                                val fragment = fragmentFactory.manageCardFragment(card.accountID, MANAGE_CARD_TAG)
                                fragment.delegate = this
                                setStartElement(fragment as BaseFragment)
                                onInitComplete(Either.Right(Unit))
                            }
                        }
                        else -> {
                            initKycFlow(card = card) { initResult ->
                                initResult.either({ onInitComplete(Either.Left(it)) }) { flow ->
                                    setStartElement(element = flow)
                                    onInitComplete(Either.Right(Unit))
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    override fun restoreState() {
        manageCardFragment?.delegate = this
        (fragmentWithTag(TRANSACTION_DETAILS_TAG) as? TransactionDetailsContract.View)?.let {
            it.delegate = this
        }
        (fragmentDialogWithTag(FUNDING_SOURCE_DIALOG_TAG) as? FundingSourceContract.View)?.let {
            it.delegate = this
        }
        (fragmentDialogWithTag(WAITLIST_TAG) as? WaitlistContract.View)?.let {
            it.delegate = this
        }
    }

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun showManageCardFragment() {
        aptoPlatformProtocol.fetchCard(cardId = cardId, forceRefresh = false) { result ->
            result.either(::handleFailure) { card ->
                val fragment = fragmentFactory.manageCardFragment(card.accountID, MANAGE_CARD_TAG)
                fragment.delegate = this
                push(fragment as BaseFragment)
            }
        }
    }

    //
    // Manage Card
    //
    override fun onBackFromManageCard() = onClose.invoke()

    //
    // Transaction Details
    //
    override fun onTransactionTapped(transaction: Transaction) {
        val fragment = fragmentFactory.transactionDetailsFragment(transaction, TRANSACTION_DETAILS_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun onTransactionDetailsBackPressed() = popFragment()

    //
    // Funding Sources
    //
    override fun onFundingSourceTapped(selectedBalanceID: String?) = showFundingSourcesDialog(selectedBalanceID)

    override fun onAddFundingSource(selectedBalanceID: String?) {
        popDialogFragmentWithTag(FUNDING_SOURCE_DIALOG_TAG)
        aptoPlatformProtocol.fetchCard(cardId = cardId, forceRefresh = false) { result ->
            result.either(::handleFailure) { card ->
                initAddBalanceFlow(card, selectedBalanceID)
            }
        }
    }

    override fun onFundingSourceSelected(onFinish: () -> Unit) {
        popDialogFragmentWithTag(FUNDING_SOURCE_DIALOG_TAG)
        refreshCard()
        onFinish.invoke()
    }

    private fun showFundingSourcesDialog(selectedBalanceID: String?) {
        val fragment = fragmentFactory.fundingSourceFragment(cardId, selectedBalanceID, FUNDING_SOURCE_DIALOG_TAG)
        fragment.delegate = this
        push(fragment as BaseDialogFragment)
    }

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun initAddBalanceFlow(card: Card, selectedBalanceID: String?) {
        val flow = card.features?.selectBalanceStore?.allowedBalanceTypes?.let {
            AddBalanceFlow(
                allowedBalanceTypes = it,
                cardID = cardId,
                onBack = { popFlow(animated = true) },
                onFinish = {
                    refreshCard()
                    showFundingSourcesDialog(selectedBalanceID)
                }
            )
        }
        flow?.init { initResult ->
            initResult.either(::handleFailure) {
                push(flow = flow)
            }
        }
    }

    private fun refreshCard() {
        showLoading()
        aptoPlatformProtocol.fetchCard(cardId = cardId, forceRefresh = true) { result ->
            result.either(::handleFailure) {
                manageCardFragment?.refreshBalance()
                hideLoading()
                popFlow(animated = true)
            }
        }
    }

    //
    // Account Settings
    //
    override fun onAccountSettingsTapped() {
        val flow = AccountSettingsFlow(
            cardId = cardId,
            contextConfiguration = contextConfiguration,
            onClose = { popFlow(animated = true) }
        )
        flow.init { initResult ->
            initResult.either(::handleFailure) { push(flow = flow) }
        }
    }

    //
    // KYC
    //
    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun initKycFlow(card: Card, onInitComplete: (Either<Failure, Flow>) -> Unit) {
        val flow = KycStatusFlow(
            card = card,
            onClose = onClose,
            onKycPassed = { showManageCardFragment() }
        )
        flow.init { initResult ->
            initResult.either({ onInitComplete(Either.Left(it)) }) {
                onInitComplete(Either.Right(flow))
            }
        }
    }

    //
    // Activate Physical Card
    //
    override fun onActivatePhysicalCardTapped(card: Card) {
        val flow = ActivatePhysicalCardFlow(
            card = card,
            onBack = { popFlow(animated = true) },
            onFinish = { popFlow(animated = true) },
            onPhysicalCardActivated = {
                manageCardFragment?.refreshCardData()
            }
        )
        flow.init { initResult ->
            initResult.either(::handleFailure) { push(flow = flow) }
        }
    }

    //
    // Card Settings
    //
    override fun onCardSettingsTapped(card: Card) {
        val flow = CardSettingsFlow(
            cardId,
            { popFlow(animated = true) },
            contextConfiguration,
            { manageCardFragment?.refreshCardData() },
            { manageCardFragment?.refreshTransactions() },
        )
        flow.init { initResult ->
            initResult.either(::handleFailure) { push(flow = flow) }
        }
    }

    //
    // Card Stats
    //
    override fun onCardStatsTapped() {
        val flow = CardStatsFlow(
            cardId = cardId,
            onBack = { popFlow(animated = true) },
            onFinish = { popFlow(animated = true) }
        )
        flow.init { initResult ->
            initResult.either(::handleFailure) { push(flow = flow) }
        }
    }

    //
    // Waitlist
    //
    override fun onWaitlistFinished(): Unit = showManageCardFragment()
}

internal class ManageCardInitFailure : Failure.FeatureFailure()
