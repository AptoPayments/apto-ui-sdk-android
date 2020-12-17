package com.aptopayments.sdk.core.di.fragment

import android.annotation.SuppressLint
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.KycStatus
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.config.ContextConfiguration
import com.aptopayments.mobile.data.config.ProjectConfiguration
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.data.geo.Country
import com.aptopayments.mobile.data.payment.Payment
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.mobile.data.user.*
import com.aptopayments.mobile.data.voip.Action
import com.aptopayments.mobile.data.workflowaction.AllowedBalanceType
import com.aptopayments.mobile.data.workflowaction.WorkflowActionConfigurationIssueCard
import com.aptopayments.sdk.features.auth.birthdateverification.BirthdateVerificationFragment
import com.aptopayments.sdk.features.auth.inputemail.InputEmailFragment
import com.aptopayments.sdk.features.auth.inputphone.InputPhoneFragment
import com.aptopayments.sdk.features.auth.verification.EmailVerificationFragment
import com.aptopayments.sdk.features.auth.verification.PhoneVerificationFragment
import com.aptopayments.sdk.features.card.account.AccountSettingsFragment
import com.aptopayments.sdk.features.card.activatephysicalcard.activate.ActivatePhysicalCardFragment
import com.aptopayments.sdk.features.card.activatephysicalcard.success.ActivatePhysicalCardSuccessFragment
import com.aptopayments.sdk.features.card.cardsettings.CardSettingsFragment
import com.aptopayments.sdk.features.card.cardstats.CardMonthlyStatsFragment
import com.aptopayments.sdk.features.card.cardstats.chart.CardTransactionsChart
import com.aptopayments.sdk.features.card.fundingsources.FundingSourceDialogFragment
import com.aptopayments.sdk.features.card.notificationpreferences.NotificationPreferencesFragment
import com.aptopayments.sdk.features.card.passcode.passcode.ConfirmCardPasscodeFragment
import com.aptopayments.sdk.features.card.passcode.passcode.SetCardPasscodeFragment
import com.aptopayments.sdk.features.card.passcode.start.CardPasscodeStartFragment
import com.aptopayments.sdk.features.card.setpin.ConfirmCardPinFragment
import com.aptopayments.sdk.features.card.setpin.SetCardPinFragment
import com.aptopayments.sdk.features.card.statements.StatementListFragment
import com.aptopayments.sdk.features.card.transactionlist.TransactionListConfig
import com.aptopayments.sdk.features.card.transactionlist.TransactionListFragment
import com.aptopayments.sdk.features.card.waitlist.WaitlistFragment
import com.aptopayments.sdk.features.contentpresenter.ContentPresenterFragment
import com.aptopayments.sdk.features.disclaimer.DisclaimerFragment
import com.aptopayments.sdk.features.inputdata.address.CollectUserAddressFragment
import com.aptopayments.sdk.features.inputdata.birthdate.CollectUserBirthdateFragment
import com.aptopayments.sdk.features.inputdata.email.CollectUserEmailFragment
import com.aptopayments.sdk.features.inputdata.id.CollectUserIdFragment
import com.aptopayments.sdk.features.inputdata.name.CollectUserNameSurnameFragment
import com.aptopayments.sdk.features.inputdata.phone.CollectUserPhoneFragment
import com.aptopayments.sdk.features.issuecard.IssueCardFragment
import com.aptopayments.sdk.features.kyc.KycStatusFragment
import com.aptopayments.sdk.features.loadfunds.add.AddFundsFragment
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.AddCardDetailsFragment
import com.aptopayments.sdk.features.loadfunds.paymentsources.list.PaymentSourcesListFragment
import com.aptopayments.sdk.features.loadfunds.paymentsources.onboarding.AddCardOnboardingFragment
import com.aptopayments.sdk.features.loadfunds.result.AddFundsResultFragment
import com.aptopayments.sdk.features.maintenance.MaintenanceFragment
import com.aptopayments.sdk.features.managecard.ManageCardFragment
import com.aptopayments.sdk.features.nonetwork.NoNetworkFragment
import com.aptopayments.sdk.features.oauth.OAuthConfig
import com.aptopayments.sdk.features.oauth.connect.OAuthConnectFragment
import com.aptopayments.sdk.features.oauth.verify.OAuthVerifyFragment
import com.aptopayments.sdk.features.passcode.ChangePasscodeFragment
import com.aptopayments.sdk.features.passcode.CreatePasscodeFragment
import com.aptopayments.sdk.features.selectcountry.CountrySelectorFragment
import com.aptopayments.sdk.features.transactiondetails.TransactionDetailsFragment
import com.aptopayments.sdk.features.voip.VoipFragment
import com.aptopayments.sdk.ui.fragments.pdf.PdfRendererFragment
import com.aptopayments.sdk.ui.fragments.webbrowser.WebBrowserFragment
import org.threeten.bp.LocalDate
import java.io.File

@SuppressLint("VisibleForTests")
internal class FragmentFactoryImpl : FragmentFactory {

    override fun countrySelectorFragment(allowedCountries: List<Country>, tag: String) =
        CountrySelectorFragment.newInstance(allowedCountries).apply { this.TAG = tag }

    override fun inputPhoneFragment(allowedCountries: List<Country>, tag: String) =
        InputPhoneFragment.newInstance(allowedCountries).apply { this.TAG = tag }

    override fun inputEmailFragment(tag: String) = InputEmailFragment.newInstance().apply { this.TAG = tag }

    override fun birthdateVerificationFragment(verification: Verification, tag: String) =
        BirthdateVerificationFragment.newInstance(verification, tag)

    override fun oauthConnectFragment(config: OAuthConfig, tag: String) =
        OAuthConnectFragment.newInstance(config).apply { this.TAG = tag }

    override fun oauthVerifyFragment(
        datapoints: DataPointList,
        allowedBalanceType: AllowedBalanceType,
        tokenId: String,
        tag: String
    ) = OAuthVerifyFragment.newInstance(datapoints, allowedBalanceType, tokenId).apply { this.TAG = tag }

    override fun phoneVerificationFragment(verification: Verification, tag: String) =
        PhoneVerificationFragment.newInstance(verification).apply { this.TAG = tag }

    override fun emailVerificationFragment(verification: Verification, tag: String) =
        EmailVerificationFragment.newInstance(verification).apply { this.TAG = tag }

    override fun kycStatusFragment(kycStatus: KycStatus, cardID: String, tag: String) =
        KycStatusFragment.newInstance(kycStatus, cardID).apply { this.TAG = tag }

    override fun noNetworkFragment(tag: String) = NoNetworkFragment.newInstance().apply { this.TAG = tag }

    override fun maintenanceFragment(tag: String) = MaintenanceFragment.newInstance().apply { this.TAG = tag }

    override fun disclaimerFragment(content: Content, tag: String) =
        DisclaimerFragment.newInstance(content).apply { this.TAG = tag }

    override fun contentPresenterFragment(content: Content, title: String, tag: String) =
        ContentPresenterFragment.newInstance(content, title).apply { this.TAG = tag }

    override fun manageCardFragment(cardId: String, tag: String) =
        ManageCardFragment.newInstance(cardId).apply { this.TAG = tag }

    override fun fundingSourceFragment(cardID: String, selectedBalanceID: String?, tag: String) =
        FundingSourceDialogFragment.newInstance(cardID, selectedBalanceID).apply { this.TAG = tag }

    override fun accountSettingsFragment(contextConfiguration: ContextConfiguration, tag: String) =
        AccountSettingsFragment.newInstance(contextConfiguration).apply { this.TAG = tag }

    override fun activatePhysicalCardFragment(card: Card, tag: String) =
        ActivatePhysicalCardFragment.newInstance(card).apply { this.TAG = tag }

    override fun activatePhysicalCardSuccessFragment(card: Card, tag: String) =
        ActivatePhysicalCardSuccessFragment.newInstance(card).apply { this.TAG = tag }

    override fun cardSettingsFragment(
        card: Card,
        cardProduct: CardProduct,
        projectConfiguration: ProjectConfiguration,
        tag: String
    ) = CardSettingsFragment.newInstance(card, cardProduct, projectConfiguration).apply { this.TAG = tag }

    override fun transactionDetailsFragment(transaction: Transaction, tag: String) =
        TransactionDetailsFragment.newInstance(transaction).apply { this.TAG = tag }

    override fun cardMonthlyStatsFragment(cardId: String, tag: String) =
        CardMonthlyStatsFragment.newInstance(cardId).apply { this.TAG = tag }

    override fun cardTransactionsChartFragment(cardId: String, date: LocalDate, tag: String) =
        CardTransactionsChart.newInstance(cardId, date).apply { this.TAG = tag }

    override fun webBrowserFragment(url: String, tag: String) =
        WebBrowserFragment.newInstance(url).apply { this.TAG = tag }

    override fun notificationPreferencesFragment(cardId: String, tag: String) =
        NotificationPreferencesFragment.newInstance(cardId).apply { this.TAG = tag }

    override fun issueCardFragment(
        cardApplicationId: String,
        actionConfiguration: WorkflowActionConfigurationIssueCard?,
        tag: String
    ) = IssueCardFragment.newInstance(cardApplicationId, actionConfiguration).apply { this.TAG = tag }

    override fun transactionListFragment(cardId: String, config: TransactionListConfig, tag: String) =
        TransactionListFragment.newInstance(cardId, config).apply { this.TAG = tag }

    override fun waitlistFragment(cardId: String, cardProduct: CardProduct, tag: String) =
        WaitlistFragment.newInstance(cardProduct, cardId).apply { this.TAG = tag }

    override fun setPinFragment(tag: String) = SetCardPinFragment().apply { this.TAG = tag }

    override fun confirmPinFragment(cardId: String, pin: String, tag: String) =
        ConfirmCardPinFragment.newInstance(cardId, pin).apply { this.TAG = tag }

    override fun setPasscodeFragment(tag: String) = SetCardPasscodeFragment().apply { this.TAG = tag }

    override fun confirmPasscodeFragment(cardId: String, pin: String, verificationId: String?, tag: String) =
        ConfirmCardPasscodeFragment.newInstance(cardId = cardId, pin = pin, verificationId = verificationId).apply { this.TAG = tag }

    override fun getVoipFragment(cardId: String, action: Action, tag: String) =
        VoipFragment.newInstance(cardId, action).apply { this.TAG = tag }

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

    override fun addCardDetailsFragment(cardId: String, tag: String) = AddCardDetailsFragment.newInstance(cardId, tag)
    override fun addCardOnboardingFragment(cardId: String, tag: String) =
        AddCardOnboardingFragment.newInstance(cardId = cardId, tag = tag)

    override fun paymentSourcesList(tag: String) = PaymentSourcesListFragment.newInstance(tag)
    override fun addFundsFragment(cardId: String, tag: String) =
        AddFundsFragment.newInstance(cardId = cardId, tag = tag)

    override fun addFundsResultFragment(cardId: String, payment: Payment, tag: String) =
        AddFundsResultFragment.newInstance(cardId, payment, tag)

    override fun cardPasscodeStartFragment(cardId: String, tag: String) =
        CardPasscodeStartFragment.newInstance(cardId, tag)
}
