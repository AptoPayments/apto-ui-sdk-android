package com.aptopayments.sdk.core.di.fragment

import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.KycStatus
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.config.ContextConfiguration
import com.aptopayments.mobile.data.config.ProjectConfiguration
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.data.geo.Country
import com.aptopayments.mobile.data.payment.Payment
import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.mobile.data.user.*
import com.aptopayments.mobile.data.voip.Action
import com.aptopayments.mobile.data.workflowaction.AllowedBalanceType
import com.aptopayments.mobile.data.workflowaction.WorkflowActionConfigurationIssueCard
import com.aptopayments.sdk.features.auth.birthdateverification.BirthdateVerificationContract
import com.aptopayments.sdk.features.auth.inputemail.InputEmailContract
import com.aptopayments.sdk.features.auth.inputphone.InputPhoneContract
import com.aptopayments.sdk.features.auth.verification.EmailVerificationContract
import com.aptopayments.sdk.features.auth.verification.PhoneVerificationContract
import com.aptopayments.sdk.features.card.account.AccountSettingsContract
import com.aptopayments.sdk.features.card.activatephysicalcard.activate.ActivatePhysicalCardContract
import com.aptopayments.sdk.features.card.activatephysicalcard.success.ActivatePhysicalCardSuccessContract
import com.aptopayments.sdk.features.card.cardsettings.CardSettingsContract
import com.aptopayments.sdk.features.card.cardstats.CardMonthlyStatsContract
import com.aptopayments.sdk.features.card.cardstats.chart.CardTransactionsChartContract
import com.aptopayments.sdk.features.card.fundingsources.FundingSourceContract
import com.aptopayments.sdk.features.card.notificationpreferences.NotificationPreferencesContract
import com.aptopayments.sdk.features.card.orderphysical.initial.OrderPhysicalCardContract
import com.aptopayments.sdk.features.card.orderphysical.success.OrderPhysicalCardSuccessContract
import com.aptopayments.sdk.features.card.passcode.start.CardPasscodeStartFragment
import com.aptopayments.sdk.features.card.setpin.ConfirmCardPinContract
import com.aptopayments.sdk.features.card.setpin.SetCardPinContract
import com.aptopayments.sdk.features.card.statements.StatementListContract
import com.aptopayments.sdk.features.card.transactionlist.TransactionListConfig
import com.aptopayments.sdk.features.card.transactionlist.TransactionListContract
import com.aptopayments.sdk.features.card.waitlist.WaitlistContract
import com.aptopayments.sdk.features.contentpresenter.ContentPresenterContract
import com.aptopayments.sdk.features.directdeposit.details.AchAccountDetailsDialogContract
import com.aptopayments.sdk.features.disclaimer.DisclaimerContract
import com.aptopayments.sdk.features.inputdata.address.CollectUserAddressContract
import com.aptopayments.sdk.features.inputdata.birthdate.CollectUserBirthdateContract
import com.aptopayments.sdk.features.inputdata.email.CollectUserEmailContract
import com.aptopayments.sdk.features.inputdata.id.CollectUserIdContract
import com.aptopayments.sdk.features.inputdata.name.CollectUserNameSurnameContract
import com.aptopayments.sdk.features.inputdata.phone.CollectUserPhoneContract
import com.aptopayments.sdk.features.issuecard.IssueCardContract
import com.aptopayments.sdk.features.kyc.KycStatusContract
import com.aptopayments.sdk.features.loadfunds.add.AddFundsContract
import com.aptopayments.sdk.features.directdeposit.instructions.DirectDepositInstructionsContract
import com.aptopayments.sdk.features.disclaimer.DisclaimerFragment
import com.aptopayments.sdk.features.loadfunds.dialog.AddFundsSelectorDialogContract
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.AddCardPaymentSourceContract
import com.aptopayments.sdk.features.loadfunds.paymentsources.onboarding.AddCardOnboardingContract
import com.aptopayments.sdk.features.loadfunds.paymentsources.list.PaymentSourcesListContract
import com.aptopayments.sdk.features.loadfunds.result.AddFundsResultFragment
import com.aptopayments.sdk.features.maintenance.MaintenanceContract
import com.aptopayments.sdk.features.managecard.ManageCardContract
import com.aptopayments.sdk.features.nonetwork.NoNetworkContract
import com.aptopayments.sdk.features.oauth.OAuthConfig
import com.aptopayments.sdk.features.oauth.connect.OAuthConnectContract
import com.aptopayments.sdk.features.oauth.verify.OAuthVerifyContract
import com.aptopayments.sdk.features.passcode.PasscodeContract
import com.aptopayments.sdk.features.selectcountry.CountrySelectorContract
import com.aptopayments.sdk.features.transactiondetails.TransactionDetailsContract
import com.aptopayments.sdk.features.voip.VoipContract
import com.aptopayments.sdk.features.card.statements.detail.StatementDetailContract
import com.aptopayments.sdk.ui.fragments.webbrowser.WebBrowserContract
import org.threeten.bp.LocalDate

internal interface FragmentFactory {

    fun countrySelectorFragment(allowedCountries: List<Country>, tag: String): CountrySelectorContract.View
    fun inputPhoneFragment(allowedCountries: List<Country>, tag: String): InputPhoneContract.View
    fun inputEmailFragment(tag: String): InputEmailContract.View
    fun phoneVerificationFragment(verification: Verification, tag: String): PhoneVerificationContract.View
    fun emailVerificationFragment(verification: Verification, tag: String): EmailVerificationContract.View
    fun birthdateVerificationFragment(verification: Verification, tag: String): BirthdateVerificationContract.View
    fun kycStatusFragment(kycStatus: KycStatus, cardID: String, tag: String): KycStatusContract.View
    fun noNetworkFragment(tag: String): NoNetworkContract.View
    fun maintenanceFragment(tag: String): MaintenanceContract.View
    fun disclaimerFragment(
        content: Content,
        config: DisclaimerFragment.Configuration,
        tag: String
    ): DisclaimerContract.View

    fun oauthConnectFragment(config: OAuthConfig, tag: String): OAuthConnectContract.View
    fun contentPresenterFragment(content: Content, title: String, tag: String): ContentPresenterContract.View
    fun oauthVerifyFragment(
        datapoints: DataPointList,
        allowedBalanceType: AllowedBalanceType,
        tokenId: String,
        tag: String
    ): OAuthVerifyContract.View

    fun manageCardFragment(cardId: String, tag: String): ManageCardContract.View
    fun fundingSourceFragment(cardID: String, selectedBalanceID: String?, tag: String): FundingSourceContract.View
    fun accountSettingsFragment(contextConfiguration: ContextConfiguration, cardId: String, tag: String): AccountSettingsContract.View
    fun activatePhysicalCardFragment(card: Card, tag: String): ActivatePhysicalCardContract.View
    fun activatePhysicalCardSuccessFragment(card: Card, tag: String): ActivatePhysicalCardSuccessContract.View
    fun cardSettingsFragment(
        card: Card,
        cardProduct: CardProduct,
        projectConfiguration: ProjectConfiguration,
        tag: String
    ): CardSettingsContract.View

    fun transactionDetailsFragment(transaction: Transaction, tag: String): TransactionDetailsContract.View
    fun cardMonthlyStatsFragment(cardId: String, tag: String): CardMonthlyStatsContract.View
    fun cardTransactionsChartFragment(cardId: String, date: LocalDate, tag: String): CardTransactionsChartContract.View
    fun webBrowserFragment(url: String, tag: String): WebBrowserContract.View
    fun notificationPreferencesFragment(cardId: String, tag: String): NotificationPreferencesContract.View
    fun issueCardFragment(
        cardApplicationId: String,
        actionConfiguration: WorkflowActionConfigurationIssueCard?,
        tag: String
    ): IssueCardContract.View

    fun transactionListFragment(
        cardId: String,
        config: TransactionListConfig,
        tag: String
    ): TransactionListContract.View

    fun waitlistFragment(cardId: String, cardProduct: CardProduct, tag: String): WaitlistContract.View
    fun setPasscodeFragment(tag: String): SetCardPinContract.View
    fun confirmPasscodeFragment(cardId: String, pin: String, verificationId: String?, tag: String): ConfirmCardPinContract.View
    fun getVoipFragment(cardId: String, action: Action, tag: String): VoipContract.View
    fun statementListFragment(tag: String): StatementListContract.View
    fun statementDetailsFragment(statementMonth: StatementMonth, tag: String): StatementDetailContract.View
    fun createPasscodeFragment(tag: String): PasscodeContract.View
    fun changePasscodeFragment(tag: String): PasscodeContract.View
    fun collectNameFragment(initialValue: NameDataPoint?, tag: String): CollectUserNameSurnameContract.View
    fun collectEmailFragment(initialValue: EmailDataPoint?, tag: String): CollectUserEmailContract.View
    fun collectIdDocumentFragment(
        initialValue: IdDocumentDataPoint?,
        config: IdDataPointConfiguration,
        tag: String
    ): CollectUserIdContract.View

    fun collectAddressFragment(
        initialValue: AddressDataPoint?,
        config: AllowedCountriesConfiguration,
        tag: String
    ): CollectUserAddressContract.View

    fun collectBirthdateFragment(initialValue: BirthdateDataPoint?, tag: String): CollectUserBirthdateContract.View
    fun collectPhoneFragment(
        initialValue: PhoneDataPoint?,
        config: AllowedCountriesConfiguration,
        tag: String
    ): CollectUserPhoneContract.View

    fun addCardDetailsFragment(cardId: String, tag: String): AddCardPaymentSourceContract.View
    fun addCardOnboardingFragment(cardId: String, tag: String): AddCardOnboardingContract.View
    fun paymentSourcesList(tag: String): PaymentSourcesListContract.View
    fun addFundsFragment(cardId: String, tag: String): AddFundsContract.View
    fun addFundsResultFragment(cardId: String, payment: Payment, tag: String): AddFundsResultFragment
    fun cardPasscodeStartFragment(cardId: String, tag: String): CardPasscodeStartFragment
    fun addFundsSelectorDialogFragment(tag: String): AddFundsSelectorDialogContract.View
    fun directDepositInstructionsFragment(cardId: String, tag: String): DirectDepositInstructionsContract.View
    fun achAccountDetailsDialogFragment(cardId: String, tag: String): AchAccountDetailsDialogContract.View
    fun orderPhysicalCardFragment(cardId: String, tag: String): OrderPhysicalCardContract.View
    fun orderPhysicalCardSuccessFragment(cardId: String, tag: String): OrderPhysicalCardSuccessContract.View
}
