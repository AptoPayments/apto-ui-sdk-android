package com.aptopayments.sdk.core.di.viewmodel

import com.aptopayments.core.data.transaction.Transaction
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
import com.aptopayments.sdk.features.card.fundingsources.FundingSourcesViewModel
import com.aptopayments.sdk.features.card.notificationpreferences.NotificationPreferencesViewModel
import com.aptopayments.sdk.features.card.setpin.ConfirmPinViewModel
import com.aptopayments.sdk.features.card.setpin.SetPinViewModel
import com.aptopayments.sdk.features.card.statements.StatementListViewModel
import com.aptopayments.sdk.features.card.transactionlist.TransactionListViewModel
import com.aptopayments.sdk.features.card.waitlist.WaitlistViewModel
import com.aptopayments.sdk.features.disclaimer.DisclaimerViewModel
import com.aptopayments.sdk.features.issuecard.IssueCardViewModel
import com.aptopayments.sdk.features.kyc.KycStatusViewModel
import com.aptopayments.sdk.features.maintenance.MaintenanceViewModel
import com.aptopayments.sdk.features.managecard.ManageCardViewModel
import com.aptopayments.sdk.features.nonetwork.NoNetworkViewModel
import com.aptopayments.sdk.features.oauth.connect.OAuthConnectViewModel
import com.aptopayments.sdk.features.oauth.verify.OAuthVerifyViewModel
import com.aptopayments.sdk.features.transactiondetails.TransactionDetailsViewModel
import com.aptopayments.sdk.features.voip.VoipViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { InputPhoneViewModel(analyticsManager = get()) }
    viewModel { InputEmailViewModel(analyticsManager = get()) }
    viewModel { OAuthConnectViewModel(analyticsManager = get()) }
    viewModel { OAuthVerifyViewModel(analyticsManager = get()) }
    viewModel { VerificationViewModel(analyticsManager = get()) }
    viewModel { BirthdateVerificationViewModel(get(), get()) }
    viewModel { FundingSourcesViewModel(analyticsManager = get()) }
    viewModel { KycStatusViewModel(analyticsManager = get()) }
    single { FetchTransactionsTaskQueue(aptoPlatformProtocol = get()) }
    viewModel { ManageCardViewModel(getTransactionsQueue = get(), analyticsManager = get()) }
    viewModel { ActivatePhysicalCardViewModel(analyticsManager = get()) }
    viewModel { ActivatePhysicalCardSuccessViewModel(analyticsManager = get()) }
    viewModel { CardSettingsViewModel(analyticsManager = get()) }
    viewModel { (transaction: Transaction) -> TransactionDetailsViewModel(transaction, get()) }
    viewModel { CardMonthlyStatsViewModel(analyticsManager = get()) }
    viewModel { NoNetworkViewModel(analyticsManager = get()) }
    viewModel { MaintenanceViewModel(get()) }
    viewModel { AccountSettingsViewModel(get(), get()) }
    viewModel { NotificationPreferencesViewModel() }
    viewModel { DisclaimerViewModel(analyticsManager = get()) }
    viewModel { IssueCardViewModel(analyticsManager = get()) }
    viewModel { TransactionListViewModel(analyticsManager = get()) }
    viewModel { WaitlistViewModel(analyticsManager = get()) }
    viewModel { SetPinViewModel(analyticsManager = get()) }
    viewModel { ConfirmPinViewModel(analyticsManager = get()) }
    viewModel { VoipViewModel(analyticsManager = get(), voipHandler = get()) }
    viewModel { StatementListViewModel(get()) }
}
