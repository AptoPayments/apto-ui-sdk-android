package com.aptopayments.sdk.core.di.viewmodel

import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.payment.Payment
import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.mobile.data.transfermoney.CardHolderData
import com.aptopayments.mobile.data.transfermoney.P2pTransferResponse
import com.aptopayments.mobile.data.user.*
import com.aptopayments.mobile.data.workflowaction.AllowedBalanceType
import com.aptopayments.mobile.data.workflowaction.WorkflowActionConfigurationIssueCard
import com.aptopayments.sdk.features.auth.birthdateverification.BirthdateVerificationViewModel
import com.aptopayments.sdk.features.auth.inputemail.InputEmailViewModel
import com.aptopayments.sdk.features.auth.inputphone.InputPhoneViewModel
import com.aptopayments.sdk.features.auth.verification.VerificationViewModel
import com.aptopayments.sdk.features.card.account.AccountSettingsViewModel
import com.aptopayments.sdk.features.card.activatephysicalcard.activate.ActivatePhysicalCardViewModel
import com.aptopayments.sdk.features.card.activatephysicalcard.success.ActivatePhysicalCardSuccessViewModel
import com.aptopayments.sdk.features.card.cardsettings.CardSettingsViewModel
import com.aptopayments.sdk.features.card.cardstats.CardMonthlyStatsViewModel
import com.aptopayments.sdk.features.card.cardstats.chart.CardTransactionsChartViewModel
import com.aptopayments.sdk.features.card.fundingsources.FundingSourcesViewModel
import com.aptopayments.sdk.features.card.notificationpreferences.NotificationPreferencesViewModel
import com.aptopayments.sdk.features.card.orderphysical.initial.OrderPhysicalCardViewModel
import com.aptopayments.sdk.features.card.orderphysical.success.OrderPhysicalCardSuccessViewModel
import com.aptopayments.sdk.features.card.passcode.passcode.ConfirmCardPasscodeViewModel
import com.aptopayments.sdk.features.card.passcode.passcode.SetCardPasscodeViewModel
import com.aptopayments.sdk.features.card.passcode.start.CardPasscodeStartViewModel
import com.aptopayments.sdk.features.card.statements.StatementListViewModel
import com.aptopayments.sdk.features.card.statements.detail.StatementDetailViewModel
import com.aptopayments.sdk.features.card.transactionlist.TransactionListConfig
import com.aptopayments.sdk.features.card.transactionlist.TransactionListViewModel
import com.aptopayments.sdk.features.card.waitlist.WaitlistViewModel
import com.aptopayments.sdk.features.directdeposit.details.AchAccountDetailsViewModel
import com.aptopayments.sdk.features.disclaimer.DisclaimerViewModel
import com.aptopayments.sdk.features.inputdata.address.CollectUserAddressViewModel
import com.aptopayments.sdk.features.inputdata.birthdate.CollectUserBirthdateViewModel
import com.aptopayments.sdk.features.inputdata.email.CollectUserEmailViewModel
import com.aptopayments.sdk.features.inputdata.id.CollectUserIdViewModel
import com.aptopayments.sdk.features.inputdata.name.CollectUserNameViewModel
import com.aptopayments.sdk.features.inputdata.phone.CollectUserPhoneViewModel
import com.aptopayments.sdk.features.issuecard.IssueCardViewModel
import com.aptopayments.sdk.features.kyc.KycStatusViewModel
import com.aptopayments.sdk.features.loadfunds.add.AddFundsViewModel
import com.aptopayments.sdk.features.directdeposit.instructions.DirectDepositInstructionsViewModel
import com.aptopayments.sdk.features.loadfunds.dialog.AddFundsSelectorDialogViewModel
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.AddCardPaymentSourceViewModel
import com.aptopayments.sdk.features.loadfunds.paymentsources.list.PaymentSourcesListViewModel
import com.aptopayments.sdk.features.loadfunds.paymentsources.onboarding.AddCardOnboardingViewModel
import com.aptopayments.sdk.features.loadfunds.result.AddFundsResultViewModel
import com.aptopayments.sdk.features.maintenance.MaintenanceViewModel
import com.aptopayments.sdk.features.managecard.FetchTransactionsTaskQueue
import com.aptopayments.sdk.features.managecard.ManageCardViewModel
import com.aptopayments.sdk.features.nonetwork.NoNetworkViewModel
import com.aptopayments.sdk.features.oauth.connect.OAuthConnectViewModel
import com.aptopayments.sdk.features.oauth.verify.OAuthVerifyViewModel
import com.aptopayments.sdk.features.p2p.funds.SendFundsViewModel
import com.aptopayments.sdk.features.p2p.recipient.DEBOUNCE_TIME
import com.aptopayments.sdk.features.p2p.recipient.P2pRecipientViewModel
import com.aptopayments.sdk.features.p2p.result.P2pResultViewModel
import com.aptopayments.sdk.features.passcode.ChangePasscodeViewModel
import com.aptopayments.sdk.features.passcode.CreatePasscodeViewModel
import com.aptopayments.sdk.features.transactiondetails.TransactionDetailsViewModel
import com.aptopayments.sdk.features.voip.VoipViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.threeten.bp.LocalDate

val viewModelModule = module {
    viewModel { InputPhoneViewModel(get(), get(), get()) }
    viewModel { InputEmailViewModel(get()) }
    viewModel { (allowedBalanceType: AllowedBalanceType) -> OAuthConnectViewModel(allowedBalanceType, get(), get()) }
    viewModel { OAuthVerifyViewModel(get()) }
    viewModel { VerificationViewModel(get()) }
    viewModel { (verification: Verification) -> BirthdateVerificationViewModel(verification, get(), get()) }
    viewModel { FundingSourcesViewModel(get()) }
    viewModel { KycStatusViewModel(get()) }
    single { FetchTransactionsTaskQueue(get()) }
    viewModel { (cardId: String) -> ManageCardViewModel(cardId, get(), get(), get(), get()) }
    viewModel { (cardId: String) -> ActivatePhysicalCardViewModel(cardId, get(), get()) }
    viewModel { ActivatePhysicalCardSuccessViewModel(get()) }
    viewModel { (card: Card, cardProduct: CardProduct) -> CardSettingsViewModel(card, cardProduct, get(), get(), get(), get()) }
    viewModel { (transaction: Transaction) -> TransactionDetailsViewModel(transaction, get()) }
    viewModel { (cardId: String) -> CardMonthlyStatsViewModel(cardId, get(), get(), get()) }
    viewModel { (cardId: String, date: LocalDate) -> CardTransactionsChartViewModel(cardId, date, get()) }
    viewModel { NoNetworkViewModel(get()) }
    viewModel { MaintenanceViewModel(get()) }
    viewModel { AccountSettingsViewModel(get(), get(), get(), get()) }
    viewModel { NotificationPreferencesViewModel(get(), get()) }
    viewModel { DisclaimerViewModel(get()) }
    viewModel { (cardApplicationId: String, actionConfiguration: WorkflowActionConfigurationIssueCard?) ->
        IssueCardViewModel(cardApplicationId, actionConfiguration, get(), get(), get())
    }
    viewModel { (cardId: String, config: TransactionListConfig) ->
        TransactionListViewModel(
            cardId,
            config,
            get(),
            get()
        )
    }
    viewModel { WaitlistViewModel(get()) }
    viewModel { SetCardPasscodeViewModel(get()) }
    viewModel { (cardId: String, pin: String, verificationId: String?) ->
        ConfirmCardPasscodeViewModel(
            cardId = cardId,
            previousPin = pin,
            verificationId = verificationId,
            get(),
            get()
        )
    }
    viewModel { VoipViewModel(get(), get(), get()) }
    viewModel { StatementListViewModel(get(), get()) }
    viewModel { CreatePasscodeViewModel(get()) }
    viewModel { ChangePasscodeViewModel(get(), get()) }
    viewModel { (initialValue: NameDataPoint?) -> CollectUserNameViewModel(initialValue, get()) }
    viewModel { (initialValue: EmailDataPoint?) -> CollectUserEmailViewModel(initialValue, get()) }
    viewModel { (initialValue: IdDocumentDataPoint?, config: IdDataPointConfiguration) ->
        CollectUserIdViewModel(initialValue, config, get())
    }
    viewModel { (initialValue: AddressDataPoint?) -> CollectUserAddressViewModel(initialValue, get(), get(), get()) }
    viewModel { CollectUserBirthdateViewModel(get()) }
    viewModel { CollectUserPhoneViewModel(get()) }
    viewModel { (cardId: String) -> AddCardPaymentSourceViewModel(cardId, get(), get()) }
    viewModel { PaymentSourcesListViewModel(get(), get(), get()) }
    viewModel { (cardId: String) -> AddFundsViewModel(cardId, get(), get(), get()) }
    viewModel { (cardId: String, payment: Payment) -> AddFundsResultViewModel(cardId, payment, get(), get()) }
    viewModel { (cardId: String) -> AddCardOnboardingViewModel(cardId, get(), get()) }
    viewModel { (cardId: String) -> CardPasscodeStartViewModel(cardId, get(), get()) }
    viewModel { AddFundsSelectorDialogViewModel(get()) }
    viewModel { (cardId: String) -> DirectDepositInstructionsViewModel(cardId, get(), get()) }
    viewModel { (cardId: String) -> AchAccountDetailsViewModel(cardId, get(), get()) }
    viewModel { (cardId: String) -> OrderPhysicalCardViewModel(cardId, get(), get()) }
    viewModel { (cardId: String) -> OrderPhysicalCardSuccessViewModel(cardId, get(), get()) }
    viewModel { (month: StatementMonth) -> StatementDetailViewModel(month, get(), get(), get()) }
    viewModel { P2pRecipientViewModel(DEBOUNCE_TIME, get()) }
    viewModel { (cardId: String, recipient: CardHolderData) -> SendFundsViewModel(cardId, recipient, get()) }
    viewModel { (result: P2pTransferResponse) -> P2pResultViewModel(result) }
}
