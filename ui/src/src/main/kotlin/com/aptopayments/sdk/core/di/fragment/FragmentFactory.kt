package com.aptopayments.sdk.core.di.fragment

import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.card.KycStatus
import com.aptopayments.core.data.cardproduct.CardProduct
import com.aptopayments.core.data.config.ContextConfiguration
import com.aptopayments.core.data.config.ProjectConfiguration
import com.aptopayments.core.data.content.Content
import com.aptopayments.core.data.geo.Country
import com.aptopayments.core.data.transaction.Transaction
import com.aptopayments.core.data.user.*
import com.aptopayments.core.data.voip.Action
import com.aptopayments.core.data.workflowaction.AllowedBalanceType
import com.aptopayments.core.data.workflowaction.WorkflowActionConfigurationIssueCard
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
import com.aptopayments.sdk.features.card.setpin.ConfirmPinContract
import com.aptopayments.sdk.features.card.setpin.SetPinContract
import com.aptopayments.sdk.features.card.statements.StatementListContract
import com.aptopayments.sdk.features.card.transactionlist.TransactionListConfig
import com.aptopayments.sdk.features.card.transactionlist.TransactionListContract
import com.aptopayments.sdk.features.card.waitlist.WaitlistContract
import com.aptopayments.sdk.features.contentpresenter.ContentPresenterContract
import com.aptopayments.sdk.features.disclaimer.DisclaimerContract
import com.aptopayments.sdk.features.inputdata.address.CollectUserAddressContract
import com.aptopayments.sdk.features.inputdata.birthdate.CollectUserBirthdateContract
import com.aptopayments.sdk.features.inputdata.email.CollectUserEmailContract
import com.aptopayments.sdk.features.inputdata.id.CollectUserIdContract
import com.aptopayments.sdk.features.inputdata.name.CollectUserNameSurnameContract
import com.aptopayments.sdk.features.inputdata.phone.CollectUserPhoneContract
import com.aptopayments.sdk.features.issuecard.IssueCardContract
import com.aptopayments.sdk.features.kyc.KycStatusContract
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
import com.aptopayments.sdk.ui.fragments.pdf.PdfRendererContract
import com.aptopayments.sdk.ui.fragments.webbrowser.WebBrowserContract
import org.threeten.bp.LocalDate
import java.io.File

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
    fun disclaimerFragment(content: Content, tag: String): DisclaimerContract.View
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
    fun accountSettingsFragment(contextConfiguration: ContextConfiguration, tag: String): AccountSettingsContract.View
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
    fun setPinFragment(tag: String): SetPinContract.View
    fun confirmPinFragment(pin: String, tag: String): ConfirmPinContract.View
    fun getVoipFragment(cardId: String, action: Action, tag: String): VoipContract.View
    fun statementListFragment(tag: String): StatementListContract.View
    fun pdfRendererFragment(title: String, file: File, tag: String): PdfRendererContract.View
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
}
