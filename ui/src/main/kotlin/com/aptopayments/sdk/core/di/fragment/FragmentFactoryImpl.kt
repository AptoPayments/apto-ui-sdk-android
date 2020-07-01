package com.aptopayments.sdk.core.di.fragment

import android.annotation.SuppressLint
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.KycStatus
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.config.ContextConfiguration
import com.aptopayments.mobile.data.config.ProjectConfiguration
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.data.geo.Country
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.mobile.data.user.*
import com.aptopayments.mobile.data.voip.Action
import com.aptopayments.mobile.data.workflowaction.AllowedBalanceType
import com.aptopayments.mobile.data.workflowaction.WorkflowActionConfigurationIssueCard
import com.aptopayments.sdk.features.auth.birthdateverification.BirthdateVerificationFragmentThemeTwo
import com.aptopayments.sdk.features.auth.inputemail.InputEmailFragmentThemeTwo
import com.aptopayments.sdk.features.auth.inputphone.InputPhoneFragmentThemeTwo
import com.aptopayments.sdk.features.auth.verification.EmailVerificationFragmentThemeTwo
import com.aptopayments.sdk.features.auth.verification.PhoneVerificationFragmentThemeTwo
import com.aptopayments.sdk.features.card.account.AccountSettingsFragmentThemeTwo
import com.aptopayments.sdk.features.card.activatephysicalcard.activate.ActivatePhysicalCardFragmentThemeTwo
import com.aptopayments.sdk.features.card.activatephysicalcard.success.ActivatePhysicalCardSuccessFragmentThemeTwo
import com.aptopayments.sdk.features.card.cardsettings.CardSettingsFragmentThemeTwo
import com.aptopayments.sdk.features.card.cardstats.CardMonthlyStatsFragmentThemeTwo
import com.aptopayments.sdk.features.card.cardstats.chart.CardTransactionsChartThemeTwo
import com.aptopayments.sdk.features.card.fundingsources.FundingSourceDialogFragmentThemeTwo
import com.aptopayments.sdk.features.card.notificationpreferences.NotificationPreferencesFragmentThemeTwo
import com.aptopayments.sdk.features.card.setpin.ConfirmPinFragmentThemeTwo
import com.aptopayments.sdk.features.card.setpin.SetPinFragmentThemeTwo
import com.aptopayments.sdk.features.card.statements.StatementListFragment
import com.aptopayments.sdk.features.card.transactionlist.TransactionListConfig
import com.aptopayments.sdk.features.card.transactionlist.TransactionListFragmentThemeTwo
import com.aptopayments.sdk.features.card.waitlist.WaitlistFragmentThemeTwo
import com.aptopayments.sdk.features.contentpresenter.ContentPresenterFragmentThemeTwo
import com.aptopayments.sdk.features.disclaimer.DisclaimerFragmentThemeTwo
import com.aptopayments.sdk.features.inputdata.address.CollectUserAddressFragment
import com.aptopayments.sdk.features.inputdata.birthdate.CollectUserBirthdateFragment
import com.aptopayments.sdk.features.inputdata.email.CollectUserEmailFragment
import com.aptopayments.sdk.features.inputdata.id.CollectUserIdFragment
import com.aptopayments.sdk.features.inputdata.name.CollectUserNameSurnameFragment
import com.aptopayments.sdk.features.inputdata.phone.CollectUserPhoneFragment
import com.aptopayments.sdk.features.issuecard.IssueCardFragmentThemeTwo
import com.aptopayments.sdk.features.kyc.KycStatusFragmentThemeTwo
import com.aptopayments.sdk.features.maintenance.MaintenanceFragmentThemeTwo
import com.aptopayments.sdk.features.managecard.ManageCardFragmentThemeTwo
import com.aptopayments.sdk.features.nonetwork.NoNetworkFragmentThemeTwo
import com.aptopayments.sdk.features.oauth.OAuthConfig
import com.aptopayments.sdk.features.oauth.connect.OAuthConnectFragmentThemeTwo
import com.aptopayments.sdk.features.oauth.verify.OAuthVerifyFragmentThemeTwo
import com.aptopayments.sdk.features.passcode.ChangePasscodeFragment
import com.aptopayments.sdk.features.passcode.CreatePasscodeFragment
import com.aptopayments.sdk.features.selectcountry.CountrySelectorFragmentThemeTwo
import com.aptopayments.sdk.features.transactiondetails.TransactionDetailsFragmentThemeTwo
import com.aptopayments.sdk.features.voip.VoipFragmentThemeTwo
import com.aptopayments.sdk.ui.fragments.pdf.PdfRendererFragment
import com.aptopayments.sdk.ui.fragments.webbrowser.WebBrowserFragment
import org.threeten.bp.LocalDate
import java.io.File

@SuppressLint("VisibleForTests")
internal class FragmentFactoryImpl : FragmentFactory {

    override fun countrySelectorFragment(allowedCountries: List<Country>, tag: String) =
        CountrySelectorFragmentThemeTwo.newInstance(allowedCountries).apply { this.TAG = tag }

    override fun inputPhoneFragment(allowedCountries: List<Country>, tag: String) =
        InputPhoneFragmentThemeTwo.newInstance(allowedCountries).apply { this.TAG = tag }

    override fun inputEmailFragment(tag: String) = InputEmailFragmentThemeTwo.newInstance().apply { this.TAG = tag }

    override fun birthdateVerificationFragment(verification: Verification, tag: String) =
        BirthdateVerificationFragmentThemeTwo.newInstance(verification, tag)

    override fun oauthConnectFragment(config: OAuthConfig, tag: String) =
        OAuthConnectFragmentThemeTwo.newInstance(config).apply { this.TAG = tag }

    override fun oauthVerifyFragment(
        datapoints: DataPointList,
        allowedBalanceType: AllowedBalanceType,
        tokenId: String,
        tag: String
    ) = OAuthVerifyFragmentThemeTwo.newInstance(datapoints, allowedBalanceType, tokenId).apply { this.TAG = tag }

    override fun phoneVerificationFragment(verification: Verification, tag: String) =
        PhoneVerificationFragmentThemeTwo.newInstance(verification).apply { this.TAG = tag }

    override fun emailVerificationFragment(verification: Verification, tag: String) =
        EmailVerificationFragmentThemeTwo.newInstance(verification).apply { this.TAG = tag }

    override fun kycStatusFragment(kycStatus: KycStatus, cardID: String, tag: String) =
        KycStatusFragmentThemeTwo.newInstance(kycStatus, cardID).apply { this.TAG = tag }

    override fun noNetworkFragment(tag: String) = NoNetworkFragmentThemeTwo.newInstance().apply { this.TAG = tag }

    override fun maintenanceFragment(tag: String) = MaintenanceFragmentThemeTwo.newInstance().apply { this.TAG = tag }

    override fun disclaimerFragment(content: Content, tag: String) =
        DisclaimerFragmentThemeTwo.newInstance(content).apply { this.TAG = tag }

    override fun contentPresenterFragment(content: Content, title: String, tag: String) =
        ContentPresenterFragmentThemeTwo.newInstance(content, title).apply { this.TAG = tag }

    override fun manageCardFragment(cardId: String, tag: String) =
        ManageCardFragmentThemeTwo.newInstance(cardId).apply { this.TAG = tag }

    override fun fundingSourceFragment(cardID: String, selectedBalanceID: String?, tag: String) =
        FundingSourceDialogFragmentThemeTwo.newInstance(cardID, selectedBalanceID).apply { this.TAG = tag }

    override fun accountSettingsFragment(contextConfiguration: ContextConfiguration, tag: String) =
        AccountSettingsFragmentThemeTwo.newInstance(contextConfiguration).apply { this.TAG = tag }

    override fun activatePhysicalCardFragment(card: Card, tag: String) =
        ActivatePhysicalCardFragmentThemeTwo.newInstance(card).apply { this.TAG = tag }

    override fun activatePhysicalCardSuccessFragment(card: Card, tag: String) =
        ActivatePhysicalCardSuccessFragmentThemeTwo.newInstance(card).apply { this.TAG = tag }

    override fun cardSettingsFragment(
        card: Card,
        cardProduct: CardProduct,
        projectConfiguration: ProjectConfiguration,
        tag: String
    ) = CardSettingsFragmentThemeTwo.newInstance(card, cardProduct, projectConfiguration).apply { this.TAG = tag }

    override fun transactionDetailsFragment(transaction: Transaction, tag: String) =
        TransactionDetailsFragmentThemeTwo.newInstance(transaction).apply { this.TAG = tag }

    override fun cardMonthlyStatsFragment(cardId: String, tag: String) =
        CardMonthlyStatsFragmentThemeTwo.newInstance(cardId).apply { this.TAG = tag }

    override fun cardTransactionsChartFragment(cardId: String, date: LocalDate, tag: String) =
        CardTransactionsChartThemeTwo.newInstance(cardId, date).apply { this.TAG = tag }

    override fun webBrowserFragment(url: String, tag: String) =
        WebBrowserFragment.newInstance(url).apply { this.TAG = tag }

    override fun notificationPreferencesFragment(cardId: String, tag: String) =
        NotificationPreferencesFragmentThemeTwo.newInstance(cardId).apply { this.TAG = tag }

    override fun issueCardFragment(
        cardApplicationId: String,
        actionConfiguration: WorkflowActionConfigurationIssueCard?,
        tag: String
    ) = IssueCardFragmentThemeTwo.newInstance(cardApplicationId, actionConfiguration).apply { this.TAG = tag }

    override fun transactionListFragment(cardId: String, config: TransactionListConfig, tag: String) =
        TransactionListFragmentThemeTwo.newInstance(cardId, config).apply { this.TAG = tag }

    override fun waitlistFragment(cardId: String, cardProduct: CardProduct, tag: String) =
        WaitlistFragmentThemeTwo.newInstance(cardProduct, cardId).apply { this.TAG = tag }

    override fun setPinFragment(tag: String) = SetPinFragmentThemeTwo().apply { this.TAG = tag }

    override fun confirmPinFragment(pin: String, tag: String) =
        ConfirmPinFragmentThemeTwo.newInstance(pin).apply { this.TAG = tag }

    override fun getVoipFragment(cardId: String, action: Action, tag: String) =
        VoipFragmentThemeTwo.newInstance(cardId, action).apply { this.TAG = tag }

    override fun statementListFragment(tag: String) = StatementListFragment.newInstance().apply { this.TAG = tag }

    override fun pdfRendererFragment(title: String, file: File, tag: String) =
        PdfRendererFragment.newInstance(title, file).apply { this.TAG = tag }

    override fun createPasscodeFragment(tag: String) = CreatePasscodeFragment().apply { this.TAG = tag }

    override fun changePasscodeFragment(tag: String) = ChangePasscodeFragment().apply { this.TAG = tag }

    override fun collectNameFragment(initialValue: NameDataPoint?, tag: String) =
        CollectUserNameSurnameFragment.newInstance(initialValue, tag)

    override fun collectEmailFragment(initialValue: EmailDataPoint?, tag: String) =
        CollectUserEmailFragment.newInstance(initialValue, tag)

    override fun collectIdDocumentFragment(
        initialValue: IdDocumentDataPoint?,
        config: IdDataPointConfiguration,
        tag: String
    ) = CollectUserIdFragment.newInstance(initialValue, config, tag)

    override fun collectAddressFragment(
        initialValue: AddressDataPoint?,
        config: AllowedCountriesConfiguration,
        tag: String
    ) = CollectUserAddressFragment.newInstance(initialValue, config, tag)

    override fun collectBirthdateFragment(initialValue: BirthdateDataPoint?, tag: String) =
        CollectUserBirthdateFragment.newInstance(initialValue, tag)

    override fun collectPhoneFragment(
        initialValue: PhoneDataPoint?,
        config: AllowedCountriesConfiguration,
        tag: String
    ) = CollectUserPhoneFragment.newInstance(initialValue, config, tag)
}
