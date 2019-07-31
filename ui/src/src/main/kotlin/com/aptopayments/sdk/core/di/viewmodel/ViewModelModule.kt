package com.aptopayments.sdk.core.di.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal abstract class ViewModelModule {
    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(InputPhoneViewModel::class)
    abstract fun bindsInputPhoneViewModel(inputPhoneViewModel: InputPhoneViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InputEmailViewModel::class)
    abstract fun bindsInputEmailViewModel(inputEmailViewModel: InputEmailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OAuthConnectViewModel::class)
    abstract fun bindsOAuthConnectViewModel(oauthConnectViewModel: OAuthConnectViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OAuthVerifyViewModel::class)
    abstract fun bindsOAuthVerifyViewModel(oauthVerifyViewModel: OAuthVerifyViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VerificationViewModel::class)
    abstract fun bindsVerificationViewModel(verificationViewModel: VerificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BirthdateVerificationViewModel::class)
    abstract fun bindsBirthdateVerificationViewModel(birthdateVerificationViewModel: BirthdateVerificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FundingSourcesViewModel::class)
    abstract fun bindsFundingSourceViewModel(fundingSourcesViewModel: FundingSourcesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(KycStatusViewModel::class)
    abstract fun bindsKycStatusViewModel(kycStatusViewModel: KycStatusViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ManageCardViewModel::class)
    abstract fun manageCardViewModel(manageCardViewModel: ManageCardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ActivatePhysicalCardViewModel::class)
    abstract fun activatePhysicalCardViewModel(activatePhysicalCardViewModel: ActivatePhysicalCardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ActivatePhysicalCardSuccessViewModel::class)
    abstract fun activatePhysicalCardSuccessViewModel(activatePhysicalCardSuccessViewModel: ActivatePhysicalCardSuccessViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CardSettingsViewModel::class)
    abstract fun cardSettingsViewModel(cardSettingsViewModel: CardSettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TransactionDetailsViewModel::class)
    abstract fun transactionDetailsViewModel(transactionDetailsViewModel: TransactionDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CardMonthlyStatsViewModel::class)
    abstract fun cardMonthlyStatsViewModel(cardMonthlyStatsViewModel: CardMonthlyStatsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NoNetworkViewModel::class)
    abstract fun noNetworkViewModel(noNetworkViewModel: NoNetworkViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MaintenanceViewModel::class)
    abstract fun maintenanceViewModel(maintenanceViewModel: MaintenanceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AccountSettingsViewModel::class)
    abstract fun accountSettingsViewModel(accountSettingsViewModel: AccountSettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationPreferencesViewModel::class)
    abstract fun notificationPreferencesViewModel(notificationPreferencesViewModel: NotificationPreferencesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DisclaimerViewModel::class)
    abstract fun disclaimerViewModel(disclaimerViewModel: DisclaimerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(IssueCardViewModel::class)
    abstract fun issueCardViewModel(issueCardViewModel: IssueCardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TransactionListViewModel::class)
    abstract fun transactionListViewModel(transactionListViewModel: TransactionListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WaitlistViewModel::class)
    abstract fun waitlistViewModel(waitlistViewModel: WaitlistViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SetPinViewModel::class)
    abstract fun setPinViewModel(setPinViewModel: SetPinViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConfirmPinViewModel::class)
    abstract fun confirmPinViewModel(confirmPinViewModel: ConfirmPinViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VoipViewModel::class)
    abstract fun voipViewModel(voipViewModel: VoipViewModel): ViewModel
}
