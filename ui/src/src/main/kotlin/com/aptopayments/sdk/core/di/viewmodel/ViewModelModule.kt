package com.aptopayments.sdk.core.di.viewmodel

import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.cardproduct.CardProduct
import com.aptopayments.core.data.transaction.Transaction
import com.aptopayments.core.data.workflowaction.WorkflowActionConfigurationIssueCard
import com.aptopayments.core.data.user.*
import com.aptopayments.core.repository.transaction.FetchTransactionsTaskQueue
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
import com.aptopayments.sdk.features.card.setpin.ConfirmPinViewModel
import com.aptopayments.sdk.features.card.setpin.SetPinViewModel
import com.aptopayments.sdk.features.card.statements.StatementListViewModel
import com.aptopayments.sdk.features.card.transactionlist.TransactionListViewModel
import com.aptopayments.sdk.features.card.waitlist.WaitlistViewModel
import com.aptopayments.sdk.features.disclaimer.DisclaimerViewModel
import com.aptopayments.sdk.features.inputdata.address.CollectUserAddressViewModel
import com.aptopayments.sdk.features.inputdata.birthdate.CollectUserBirthdateViewModel
import com.aptopayments.sdk.features.inputdata.email.CollectUserEmailViewModel
import com.aptopayments.sdk.features.inputdata.id.CollectUserIdViewModel
import com.aptopayments.sdk.features.inputdata.name.CollectUserNameViewModel
import com.aptopayments.sdk.features.inputdata.phone.CollectUserPhoneViewModel
import com.aptopayments.sdk.features.issuecard.IssueCardViewModel
import com.aptopayments.sdk.features.kyc.KycStatusViewModel
import com.aptopayments.sdk.features.maintenance.MaintenanceViewModel
import com.aptopayments.sdk.features.managecard.ManageCardViewModel
import com.aptopayments.sdk.features.nonetwork.NoNetworkViewModel
import com.aptopayments.sdk.features.oauth.connect.OAuthConnectViewModel
import com.aptopayments.sdk.features.oauth.verify.OAuthVerifyViewModel
import com.aptopayments.sdk.features.passcode.ChangePasscodeViewModel
import com.aptopayments.sdk.features.passcode.CreatePasscodeViewModel
import com.aptopayments.sdk.features.transactiondetails.TransactionDetailsViewModel
import com.aptopayments.sdk.features.voip.VoipViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.threeten.bp.LocalDate

val viewModelModule = module {
    viewModel { InputPhoneViewModel(get(), get()) }
    viewModel { InputEmailViewModel(get()) }
    viewModel { OAuthConnectViewModel(get()) }
    viewModel { OAuthVerifyViewModel(get()) }
    viewModel { VerificationViewModel(get()) }
    viewModel { (verification: Verification) -> BirthdateVerificationViewModel(verification, get(), get()) }
    viewModel { FundingSourcesViewModel(get()) }
    viewModel { KycStatusViewModel(get()) }
    single { FetchTransactionsTaskQueue(get()) }
    viewModel { (cardId: String) -> ManageCardViewModel(cardId, get(), get(), get()) }
    viewModel { ActivatePhysicalCardViewModel(get()) }
    viewModel { ActivatePhysicalCardSuccessViewModel(get()) }
    viewModel { (card: Card, cardProduct: CardProduct) ->
        CardSettingsViewModel(card, cardProduct, get(), get(), get())
    }
    viewModel { (transaction: Transaction) -> TransactionDetailsViewModel(transaction, get()) }
    viewModel { (cardId: String) -> CardMonthlyStatsViewModel(cardId, get(), get(), get()) }
    viewModel { (cardId: String, date: LocalDate) -> CardTransactionsChartViewModel(cardId, date, get()) }
    viewModel { NoNetworkViewModel(get()) }
    viewModel { MaintenanceViewModel(get()) }
    viewModel { AccountSettingsViewModel(get(), get(), get()) }
    viewModel { NotificationPreferencesViewModel() }
    viewModel { DisclaimerViewModel(get()) }
    viewModel { (cardApplicationId: String, actionConfiguration : WorkflowActionConfigurationIssueCard?) ->
        IssueCardViewModel(cardApplicationId, actionConfiguration, get(), get())
    }
    viewModel { TransactionListViewModel(get()) }
    viewModel { WaitlistViewModel(get()) }
    viewModel { SetPinViewModel(get()) }
    viewModel { ConfirmPinViewModel(get()) }
    viewModel { VoipViewModel(get(), get()) }
    viewModel { StatementListViewModel(get()) }
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
}
