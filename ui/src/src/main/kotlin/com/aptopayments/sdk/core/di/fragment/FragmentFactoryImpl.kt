package com.aptopayments.sdk.core.di.fragment

import android.annotation.SuppressLint
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.card.KycStatus
import com.aptopayments.core.data.cardproduct.CardProduct
import com.aptopayments.core.data.config.ContextConfiguration
import com.aptopayments.core.data.config.ProjectConfiguration
import com.aptopayments.core.data.config.UITheme
import com.aptopayments.core.data.config.UITheme.THEME_1
import com.aptopayments.core.data.config.UITheme.THEME_2
import com.aptopayments.core.data.content.Content
import com.aptopayments.core.data.geo.Country
import com.aptopayments.core.data.transaction.Transaction
import com.aptopayments.core.data.user.DataPoint
import com.aptopayments.core.data.user.DataPointList
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.data.voip.Action
import com.aptopayments.core.data.workflowaction.AllowedBalanceType
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.auth.birthdateverification.BirthdateVerificationContract
import com.aptopayments.sdk.features.auth.birthdateverification.BirthdateVerificationFragmentThemeTwo
import com.aptopayments.sdk.features.auth.inputemail.InputEmailContract
import com.aptopayments.sdk.features.auth.inputemail.InputEmailFragmentThemeOne
import com.aptopayments.sdk.features.auth.inputphone.InputPhoneContract
import com.aptopayments.sdk.features.auth.inputphone.InputPhoneFragmentThemeOne
import com.aptopayments.sdk.features.auth.inputphone.InputPhoneFragmentThemeTwo
import com.aptopayments.sdk.features.auth.verification.*
import com.aptopayments.sdk.features.card.account.AccountSettingsContract
import com.aptopayments.sdk.features.card.account.AccountSettingsFragmentThemeTwo
import com.aptopayments.sdk.features.card.activatephysicalcard.activate.ActivatePhysicalCardContract
import com.aptopayments.sdk.features.card.activatephysicalcard.activate.ActivatePhysicalCardFragmentThemeTwo
import com.aptopayments.sdk.features.card.activatephysicalcard.success.ActivatePhysicalCardSuccessContract
import com.aptopayments.sdk.features.card.activatephysicalcard.success.ActivatePhysicalCardSuccessFragmentThemeTwo
import com.aptopayments.sdk.features.card.cardsettings.CardSettingsContract
import com.aptopayments.sdk.features.card.cardsettings.CardSettingsFragmentThemeTwo
import com.aptopayments.sdk.features.card.cardstats.CardMonthlyStatsContract
import com.aptopayments.sdk.features.card.cardstats.CardMonthlyStatsFragmentThemeTwo
import com.aptopayments.sdk.features.card.cardstats.chart.CardTransactionsChartContract
import com.aptopayments.sdk.features.card.cardstats.chart.CardTransactionsChartThemeTwo
import com.aptopayments.sdk.features.card.fundingsources.FundingSourceContract
import com.aptopayments.sdk.features.card.fundingsources.FundingSourceDialogFragmentThemeTwo
import com.aptopayments.sdk.features.card.notificationpreferences.NotificationPreferencesContract
import com.aptopayments.sdk.features.card.notificationpreferences.NotificationPreferencesFragmentThemeTwo
import com.aptopayments.sdk.features.card.setpin.ConfirmPinContract
import com.aptopayments.sdk.features.card.setpin.ConfirmPinFragmentThemeTwo
import com.aptopayments.sdk.features.card.setpin.SetPinContract
import com.aptopayments.sdk.features.card.setpin.SetPinFragmentThemeTwo
import com.aptopayments.sdk.features.card.statements.StatementListContract
import com.aptopayments.sdk.features.card.statements.StatementListFragment
import com.aptopayments.sdk.features.card.transactionlist.TransactionListConfig
import com.aptopayments.sdk.features.card.transactionlist.TransactionListContract
import com.aptopayments.sdk.features.card.transactionlist.TransactionListFragmentThemeTwo
import com.aptopayments.sdk.features.card.waitlist.WaitlistContract
import com.aptopayments.sdk.features.card.waitlist.WaitlistFragmentThemeTwo
import com.aptopayments.sdk.features.contentpresenter.ContentPresenterContract
import com.aptopayments.sdk.features.contentpresenter.ContentPresenterFragmentThemeTwo
import com.aptopayments.sdk.features.disclaimer.DisclaimerContract
import com.aptopayments.sdk.features.disclaimer.DisclaimerFragmentThemeOne
import com.aptopayments.sdk.features.disclaimer.DisclaimerFragmentThemeTwo
import com.aptopayments.sdk.features.issuecard.IssueCardContract
import com.aptopayments.sdk.features.issuecard.IssueCardErrorContract
import com.aptopayments.sdk.features.issuecard.IssueCardErrorFragmentThemeTwo
import com.aptopayments.sdk.features.issuecard.IssueCardFragmentThemeTwo
import com.aptopayments.sdk.features.kyc.KycStatusContract
import com.aptopayments.sdk.features.kyc.KycStatusFragmentThemeTwo
import com.aptopayments.sdk.features.maintenance.MaintenanceContract
import com.aptopayments.sdk.features.maintenance.MaintenanceFragmentThemeTwo
import com.aptopayments.sdk.features.managecard.ManageCardContract
import com.aptopayments.sdk.features.managecard.ManageCardFragmentThemeTwo
import com.aptopayments.sdk.features.nonetwork.NoNetworkContract
import com.aptopayments.sdk.features.nonetwork.NoNetworkFragmentThemeTwo
import com.aptopayments.sdk.features.oauth.OAuthConfig
import com.aptopayments.sdk.features.oauth.connect.OAuthConnectContract
import com.aptopayments.sdk.features.oauth.connect.OAuthConnectFragmentThemeTwo
import com.aptopayments.sdk.features.oauth.verify.OAuthVerifyContract
import com.aptopayments.sdk.features.oauth.verify.OAuthVerifyFragmentThemeTwo
import com.aptopayments.sdk.features.passcode.ChangePasscodeFragment
import com.aptopayments.sdk.features.passcode.CreatePasscodeFragment
import com.aptopayments.sdk.features.passcode.PasscodeContract
import com.aptopayments.sdk.features.selectcountry.CountrySelectorContract
import com.aptopayments.sdk.features.selectcountry.CountrySelectorFragmentThemeTwo
import com.aptopayments.sdk.features.transactiondetails.TransactionDetailsContract
import com.aptopayments.sdk.features.transactiondetails.TransactionDetailsFragmentThemeTwo
import com.aptopayments.sdk.features.voip.VoipContract
import com.aptopayments.sdk.features.voip.VoipFragmentThemeTwo
import com.aptopayments.sdk.ui.fragments.pdf.PdfRendererContract
import com.aptopayments.sdk.ui.fragments.pdf.PdfRendererFragment
import com.aptopayments.sdk.ui.fragments.webbrowser.WebBrowserContract
import com.aptopayments.sdk.ui.fragments.webbrowser.WebBrowserFragment
import org.threeten.bp.LocalDate
import java.io.File

@SuppressLint("VisibleForTests")
internal class FragmentFactoryImpl constructor() : FragmentFactory {

    override fun countrySelectorFragment(
            uiTheme: UITheme,
            allowedCountries: List<Country>,
            tag: String
    ): CountrySelectorContract.View {
        return when (uiTheme) {
            THEME_2 -> {
                CountrySelectorFragmentThemeTwo.newInstance(allowedCountries).apply { this.TAG = tag }
            }
            else -> CountrySelectorFragmentThemeTwo()
        }
    }

    override fun inputPhoneFragment(
            uiTheme: UITheme,
            allowedCountries: List<Country>,
            tag: String
    ): InputPhoneContract.View {
        return when (uiTheme) {
            THEME_2 -> {
                InputPhoneFragmentThemeTwo.newInstance(allowedCountries).apply {
                    this.TAG = tag
                }
            }
            THEME_1 -> {
                InputPhoneFragmentThemeOne.newInstance(allowedCountries).apply {
                    this.TAG = tag
                }
            }
        }
    }

    override fun inputEmailFragment(uiTheme: UITheme, tag: String): InputEmailContract.View {
        return when (uiTheme) {
            THEME_1 -> {
                InputEmailFragmentThemeOne.newInstance().apply {
                    this.TAG = tag
                }
            }
            else -> InputEmailFragmentThemeOne.newInstance().apply {
                this.TAG = tag
            }
        }
    }

    override fun birthdateVerificationFragment(
            uiTheme: UITheme,
            primaryCredential: DataPoint,
            tag: String
    ): BirthdateVerificationContract.View {
        return when (uiTheme) {
            THEME_2 -> {
                BirthdateVerificationFragmentThemeTwo.newInstance(primaryCredential).apply {
                    this.TAG = tag
                }
            }
            else -> BirthdateVerificationFragmentThemeTwo()
        }
    }

    override fun oauthConnectFragment(
            uiTheme: UITheme,
            config: OAuthConfig,
            tag: String
    ): OAuthConnectContract.View {
        return when (uiTheme) {
            THEME_2 -> {
                return OAuthConnectFragmentThemeTwo.newInstance(config).apply {
                    this.TAG = tag
                }
            }
            else -> OAuthConnectFragmentThemeTwo()
        }
    }

    override fun oauthVerifyFragment(
            uiTheme: UITheme,
            datapoints: DataPointList,
            allowedBalanceType: AllowedBalanceType,
            tokenId: String,
            tag: String
    ): OAuthVerifyContract.View {
        return when (uiTheme) {
            THEME_2 -> {
                OAuthVerifyFragmentThemeTwo.newInstance(datapoints, allowedBalanceType, tokenId).apply {
                    this.TAG = tag
                }
            }
            else -> OAuthVerifyFragmentThemeTwo()
        }
    }

    override fun phoneVerificationFragment(
            uiTheme: UITheme,
            verification: Verification,
            tag: String
    ): PhoneVerificationContract.View {
        return when (uiTheme) {
            THEME_2 -> {
                PhoneVerificationFragmentThemeTwo.newInstance(verification).apply { this.TAG = tag }
            }
            THEME_1 -> {
                PhoneVerificationFragmentThemeOne.newInstance(verification).apply { this.TAG = tag }
            }
        }
    }

    override fun emailVerificationFragment(
            uiTheme: UITheme,
            verification: Verification,
            tag: String
    ): EmailVerificationContract.View {
        return when (uiTheme) {
            THEME_2 -> {
                EmailVerificationFragmentThemeOne.newInstance(verification).apply { this.TAG = tag }
            }
            THEME_1 -> {
                EmailVerificationFragmentThemeOne.newInstance(verification).apply { this.TAG = tag }
            }
        }
    }

    override fun kycStatusFragment(
            uiTheme: UITheme,
            kycStatus: KycStatus,
            cardID: String,
            tag: String
    ): KycStatusContract.View {
        return when (uiTheme) {
            THEME_2 -> KycStatusFragmentThemeTwo.newInstance(kycStatus, cardID).apply {
                this.TAG = tag
            }
            else -> KycStatusFragmentThemeTwo.newInstance(kycStatus, cardID)
        }
    }

    override fun noNetworkFragment(
            uiTheme: UITheme,
            tag: String
    ): NoNetworkContract.View {
        return when (uiTheme) {
            THEME_2 -> NoNetworkFragmentThemeTwo.newInstance().apply { this.TAG = tag }
            else -> NoNetworkFragmentThemeTwo()
        }
    }

    override fun maintenanceFragment(
            uiTheme: UITheme,
            tag: String
    ): MaintenanceContract.View {
        return when (uiTheme) {
            THEME_2 -> MaintenanceFragmentThemeTwo.newInstance().apply { this.TAG = tag }
            else -> MaintenanceFragmentThemeTwo.newInstance()
        }
    }

    override fun disclaimerFragment(
            uiTheme: UITheme,
            content: Content,
            tag: String
    ): DisclaimerContract.View {
        return when(uiTheme) {
            THEME_2 -> {
                DisclaimerFragmentThemeTwo.newInstance(content).apply {
                    this.TAG = tag
                }
            }
            THEME_1 -> {
                DisclaimerFragmentThemeOne.newInstance(content).apply {
                    this.TAG = tag
                }
            }
        }
    }

    override fun contentPresenterFragment(
            uiTheme: UITheme,
            content: Content,
            title: String,
            tag: String
    ): ContentPresenterContract.View {
        return when(uiTheme) {
            THEME_2 -> {
                ContentPresenterFragmentThemeTwo.newInstance(content, title).apply {
                    this.TAG = tag
                }
            }
            else -> ContentPresenterFragmentThemeTwo.newInstance(content, title)
        }
    }

    override fun manageCardFragment(
            uiTheme: UITheme,
            cardId: String,
            tag: String
    ): ManageCardContract.View {
        return when(uiTheme) {
            THEME_2 -> ManageCardFragmentThemeTwo.newInstance(cardId).apply { this.TAG = tag }
            else -> ManageCardFragmentThemeTwo.newInstance(cardId)
        }
    }

    override fun fundingSourceFragment(
            uiTheme: UITheme,
            cardID: String,
            selectedBalanceID: String?,
            tag: String
    ): FundingSourceContract.View {
        return FundingSourceDialogFragmentThemeTwo.newInstance(cardID, selectedBalanceID).apply { this.TAG = tag }
    }

    override fun accountSettingsFragment(
            uiTheme: UITheme,
            contextConfiguration: ContextConfiguration,
            tag: String
    ): AccountSettingsContract.View {
        return when(uiTheme) {
            THEME_2 -> {
                AccountSettingsFragmentThemeTwo.newInstance(contextConfiguration).apply {
                    this.TAG = tag
                }
            }
            else -> AccountSettingsFragmentThemeTwo()
        }
    }

    override fun activatePhysicalCardFragment(
            uiTheme: UITheme,
            card: Card,
            tag: String
    ): ActivatePhysicalCardContract.View {
        return when(uiTheme) {
            THEME_2 -> ActivatePhysicalCardFragmentThemeTwo.newInstance(card).apply { this.TAG = tag }
            else -> ActivatePhysicalCardFragmentThemeTwo.newInstance(card)
        }
    }

    override fun activatePhysicalCardSuccessFragment(
            uiTheme: UITheme,
            card: Card,
            tag: String
    ): ActivatePhysicalCardSuccessContract.View {
        return when(uiTheme) {
            THEME_2 -> ActivatePhysicalCardSuccessFragmentThemeTwo.newInstance(card).apply { this.TAG = tag }
            else -> ActivatePhysicalCardSuccessFragmentThemeTwo.newInstance(card)
        }
    }

    override fun cardSettingsFragment(
            uiTheme: UITheme,
            card: Card,
            cardProduct: CardProduct,
            projectConfiguration: ProjectConfiguration,
            tag: String
    ): CardSettingsContract.View {
        return when(uiTheme) {
            THEME_2 -> CardSettingsFragmentThemeTwo.newInstance(card, cardProduct, projectConfiguration).apply {
                this.TAG = tag
            }
            else -> CardSettingsFragmentThemeTwo.newInstance(card, cardProduct, projectConfiguration)
        }
    }

    override fun transactionDetailsFragment(
            uiTheme: UITheme,
            transaction: Transaction,
            tag: String
    ): TransactionDetailsContract.View {
        return when(uiTheme) {
            THEME_2 -> TransactionDetailsFragmentThemeTwo.newInstance(transaction).apply { this.TAG = tag }
            else -> TransactionDetailsFragmentThemeTwo.newInstance(transaction)
        }
    }

    override fun cardMonthlyStatsFragment(
            uiTheme: UITheme,
            cardId: String,
            tag: String
    ): CardMonthlyStatsContract.View {
        return CardMonthlyStatsFragmentThemeTwo.newInstance(cardId).apply { this.TAG = tag }
    }

    override fun cardTransactionsChartFragment(
            uiTheme: UITheme,
            cardId: String,
            date: LocalDate,
            tag: String
    ): CardTransactionsChartContract.View {
        return CardTransactionsChartThemeTwo.newInstance(cardId, date).apply { this.TAG = tag }
    }

    override fun webBrowserFragment(
            url: String,
            tag: String
    ): WebBrowserContract.View {
        return WebBrowserFragment.newInstance(url).apply { this.TAG = tag }
    }

    override fun notificationPreferencesFragment(
            uiTheme: UITheme,
            cardId: String,
            tag: String
    ): NotificationPreferencesContract.View {
        return when(uiTheme) {
            THEME_2 -> NotificationPreferencesFragmentThemeTwo.newInstance(cardId).apply { this.TAG = tag }
            else -> NotificationPreferencesFragmentThemeTwo.newInstance(cardId)
        }
    }

    override fun issueCardFragment(
            uiTheme: UITheme,
            tag: String,
            cardApplicationId: String
    ): IssueCardContract.View {
        return when (uiTheme) {
            THEME_2 -> IssueCardFragmentThemeTwo.newInstance(cardApplicationId).apply {
                this.TAG = tag
            }
            else -> IssueCardFragmentThemeTwo.newInstance(cardApplicationId)
        }
    }

    override fun issueCardErrorFragment(
            uiTheme: UITheme,
            tag: String,
            errorCode: Int?,
            errorAsset: String?
    ): IssueCardErrorContract.View {
        return when (uiTheme) {
            THEME_2 -> IssueCardErrorFragmentThemeTwo.newInstance(errorCode, errorAsset).apply {
                this.TAG = tag
            }
            else -> IssueCardErrorFragmentThemeTwo.newInstance(errorCode, errorAsset)
        }
    }

    override fun transactionListFragment(
            uiTheme: UITheme,
            cardId: String,
            config: TransactionListConfig,
            tag: String
    ): TransactionListContract.View {
        return when (uiTheme) {
            THEME_2 -> TransactionListFragmentThemeTwo.newInstance(cardId, config).apply {
                this.TAG = tag
            }
            THEME_1 -> TransactionListFragmentThemeTwo.newInstance(cardId, config)
        }
    }

    override fun waitlistFragment(
            uiTheme: UITheme,
            cardId: String,
            cardProduct: CardProduct,
            tag: String
    ): WaitlistContract.View {
        return when(uiTheme) {
            THEME_2 -> WaitlistFragmentThemeTwo.newInstance(cardProduct, cardId).apply { this.TAG = tag }
            else -> WaitlistFragmentThemeTwo.newInstance(cardProduct, cardId)
        }
    }

    override fun setPinFragment(
            uiTheme: UITheme,
            tag: String
    ): SetPinContract.View {
        return when(uiTheme) {
            THEME_2 -> SetPinFragmentThemeTwo().apply { this.TAG = tag }
            else -> SetPinFragmentThemeTwo()
        }
    }

    override fun confirmPinFragment(
            uiTheme: UITheme,
            pin: String,
            tag: String
    ): ConfirmPinContract.View {
        return when(uiTheme) {
            THEME_2 -> ConfirmPinFragmentThemeTwo.newInstance(pin).apply { this.TAG = tag }
            else -> ConfirmPinFragmentThemeTwo.newInstance(pin)
        }
    }

    override fun getVoipFragment(
            uiTheme: UITheme,
            cardId: String,
            action: Action,
            tag: String
    ): VoipContract.View {
        return when(uiTheme) {
            THEME_2 -> VoipFragmentThemeTwo.newInstance(cardId, action).apply { this.TAG = tag }
            else -> VoipFragmentThemeTwo.newInstance(cardId, action)
        }
    }

    override fun statementListFragment(uiTheme: UITheme, tag: String): StatementListContract.View =
        configureTheme(uiTheme, StatementListFragment.newInstance(),tag) as StatementListFragment

    override fun pdfRendererFragment(uiTheme: UITheme, title: String, file: File, tag: String): PdfRendererContract.View =
        configureTheme(uiTheme, PdfRendererFragment.newInstance(title, file),tag) as PdfRendererFragment

    override fun createPasscodeFragment(uiTheme: UITheme, tag: String): PasscodeContract.View =
        configureTheme(uiTheme, CreatePasscodeFragment(), tag) as PasscodeContract.View

    override fun changePasscodeFragment(uiTheme: UITheme, tag: String): PasscodeContract.View =
        configureTheme(uiTheme, ChangePasscodeFragment(), tag) as PasscodeContract.View

    private fun configureTheme(uiTheme: UITheme, fragment: BaseFragment, tag: String): BaseFragment {
        return when (uiTheme) {
            THEME_2 -> fragment.apply { this.TAG = tag }
            else -> fragment
        }
    }
}
