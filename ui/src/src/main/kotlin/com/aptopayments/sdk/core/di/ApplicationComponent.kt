
package com.aptopayments.sdk.core.di

import android.app.Application
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.di.NetworkComponent
import com.aptopayments.sdk.core.di.viewmodel.ViewModelModule
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.addbalance.AddBalanceFlow
import com.aptopayments.sdk.features.auth.AuthFlow
import com.aptopayments.sdk.features.auth.inputemail.InputEmailFragmentThemeOne
import com.aptopayments.sdk.features.auth.inputphone.InputPhoneFragmentThemeOne
import com.aptopayments.sdk.features.auth.inputphone.InputPhoneFragmentThemeTwo
import com.aptopayments.sdk.features.card.CardActivity
import com.aptopayments.sdk.features.card.CardFlow
import com.aptopayments.sdk.features.card.account.AccountSettingsFlow
import com.aptopayments.sdk.features.card.activatephysicalcard.ActivatePhysicalCardFlow
import com.aptopayments.sdk.features.card.cardsettings.CardSettingsFragmentThemeTwo
import com.aptopayments.sdk.features.card.cardstats.CardStatsFlow
import com.aptopayments.sdk.features.card.cardstats.CardTransactionsChartPagerAdapter
import com.aptopayments.sdk.features.card.fundingsources.FundingSourceDialogFragmentThemeTwo
import com.aptopayments.sdk.features.card.setpin.SetPinFlow
import com.aptopayments.sdk.features.card.transactionlist.TransactionListFlow
import com.aptopayments.sdk.features.disclaimer.DisclaimerFlow
import com.aptopayments.sdk.features.issuecard.IssueCardFlow
import com.aptopayments.sdk.features.kyc.KycStatusFlow
import com.aptopayments.sdk.features.maintenance.MaintenanceFragmentThemeTwo
import com.aptopayments.sdk.features.managecard.ManageCardFlow
import com.aptopayments.sdk.features.managecard.ManageCardFragmentThemeTwo
import com.aptopayments.sdk.features.newcard.NewCardFlow
import com.aptopayments.sdk.features.nonetwork.NoNetworkFragmentThemeTwo
import com.aptopayments.sdk.features.oauth.OAuthFlow
import com.aptopayments.sdk.features.oauth.OAuthVerifyFlow
import com.aptopayments.sdk.features.selectbalancestore.SelectBalanceStoreFlow
import com.aptopayments.sdk.features.selectcountry.CardProductSelectorFlow
import com.aptopayments.sdk.features.voip.VoipFlow
import com.aptopayments.sdk.features.voip.VoipFragmentThemeTwo
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, ViewModelModule::class, NetworkComponent::class])
@VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
internal interface ApplicationComponent {

    fun inject(application: Application)

    fun inject(cardActivity: CardActivity)

    fun inject(inputPhoneFragment: InputPhoneFragmentThemeTwo)

    fun inject(inputPhoneFragment: InputPhoneFragmentThemeOne)

    fun inject(inputEmailFragment: InputEmailFragmentThemeOne)

    fun inject(baseFragment: BaseFragment)

    fun inject(baseActivity: BaseActivity)

    fun inject(baseDialogFragment: FundingSourceDialogFragmentThemeTwo)

    fun inject(manageCardFragment: ManageCardFragmentThemeTwo)

    fun inject(noNetworkFragment: NoNetworkFragmentThemeTwo)

    fun inject(maintenanceFragment: MaintenanceFragmentThemeTwo)

    fun inject(cardFlow: CardFlow)

    fun inject(cardProductSelectorFlow: CardProductSelectorFlow)

    fun inject(authFlow: AuthFlow)

    fun inject(newCardFlow: NewCardFlow)

    fun inject(selectBalanceStoreFlow: SelectBalanceStoreFlow)

    fun inject(oauthFlow: OAuthFlow)

    fun inject(oauthVerifyFlow: OAuthVerifyFlow)

    fun inject(addBalanceFlow: AddBalanceFlow)

    fun inject(disclaimerFlow: DisclaimerFlow)

    fun inject(manageCardFlow: ManageCardFlow)

    fun inject(kycStatusFlow: KycStatusFlow)

    fun inject(activatePhysicalCardFlow: ActivatePhysicalCardFlow)

    fun inject(cardStatsFlow: CardStatsFlow)

    fun inject(accountSettingsFlow: AccountSettingsFlow)

    fun inject(issueCardFlow: IssueCardFlow)

    fun inject(cardTransactionsChartPagerAdapter: CardTransactionsChartPagerAdapter)

    fun inject(transactionListFlow: TransactionListFlow)

    fun inject(setPinFlow: SetPinFlow)

    fun inject(voipFlow: VoipFlow)

    fun inject(voipFragmentThemeTwo: VoipFragmentThemeTwo)

    fun inject(cardSettingsFragmentThemeTwo: CardSettingsFragmentThemeTwo)
}
