package com.aptopayments.sdk.features.card.cardsettings

import com.aptopayments.mobile.data.PhoneNumber
import com.aptopayments.mobile.data.card.*
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.applicationModule
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.card.cardsettings.CardSettingsViewModel.Action
import com.aptopayments.sdk.repository.IAPHelper
import com.aptopayments.sdk.repository.CardActionRepository
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val CARD_ID = "CARD_ID"

@Suppress("UNCHECKED_CAST")
@ExtendWith(InstantExecutorExtension::class)
internal class CardSettingsViewModelTest : UnitTest() {

    private val cardDetailsRepo = mock<CardActionRepository>()
    private val cardFeatures = mock<Features>()
    private val card = mock<Card> {
        on { accountID } doReturn CARD_ID
        on { features } doReturn cardFeatures
    }
    private val cardProduct = mock<CardProduct>()
    private val analytics = mock<AnalyticsServiceContract>()
    private val aptoPlatform = mock<AptoPlatform>()
    private val cardOptionsMock = mock<CardOptions>()
    private val aptoUiSdkProtocol = mock<AptoUiSdkProtocol> {
        on { cardOptions } doReturn cardOptionsMock
    }
    private val cardActionRepo: CardActionRepository = mock()
    private val telephonyEnabledChecker: TelephonyEnabledChecker = mock()
    private val phoneNumber = PhoneNumber("1", "111111111")
    private val iapHelper: IAPHelper = mock()

    private lateinit var sut: CardSettingsViewModel

    @BeforeEach
    fun setUp() {
        startKoin {
            modules(
                listOf(
                    applicationModule,
                    module {
                        factory(override = true) { cardDetailsRepo }
                        factory(override = true) { telephonyEnabledChecker }
                        factory(override = true) { iapHelper }
                    }
                )
            )
        }
    }

    @Test
    fun `when cardProduct empty sections are hidden`() {
        sut = createSut()

        val state = sut.state.getOrAwaitValue()

        assertFalse(state.showLegalSection)
        assertFalse(state.showFaq)
        assertFalse(state.showCardholderAgreement)
        assertFalse(state.showTermsAndConditions)
        assertFalse(state.showPrivacyPolicy)
        assertFalse(state.showExchangeRates)
        assertFalse(state.showAddToGooglePay)
    }

    @Test
    fun `when faq is set then showFaq is true`() {
        whenever(cardProduct.faq).thenReturn(mock())

        sut = createSut()
        val state = sut.state.getOrAwaitValue()

        assertTrue(state.showFaq)
    }

    @Test
    fun `when CHA is set then legal and CHA are shown`() {
        whenever(cardProduct.cardholderAgreement).thenReturn(mock())

        sut = createSut()
        val state = sut.state.getOrAwaitValue()

        assertTrue(state.showLegalSection)
        assertTrue(state.showCardholderAgreement)
    }

    @Test
    fun `when Terms are set then legal and terms are shown`() {
        whenever(cardProduct.termsAndConditions).thenReturn(mock())

        sut = createSut()
        val state = sut.state.getOrAwaitValue()

        assertTrue(state.showLegalSection)
        assertTrue(state.showTermsAndConditions)
    }

    @Test
    fun `when privacy policy is set then legal and privacy section is shown`() {
        whenever(cardProduct.privacyPolicy).thenReturn(mock())

        sut = createSut()
        val state = sut.state.getOrAwaitValue()

        assertTrue(state.showLegalSection)
        assertTrue(state.showPrivacyPolicy)
    }

    @Test
    fun `given exchange_rates content is present when sut created then legal and exchange rates section is shown`() {
        whenever(cardProduct.exchangeRates).thenReturn(mock())

        sut = createSut()
        val state = sut.state.getOrAwaitValue()

        assertTrue(state.showLegalSection)
        assertTrue(state.showExchangeRates)
    }

    @Test
    fun `when getPin is disabled then option is not shown`() {
        configureGetPin(FeatureStatus.DISABLED, null)

        sut = createSut()
        val state = sut.state.getOrAwaitValue()

        assertFalse(state.showGetPin)
    }

    @Test
    fun `when getPin is enabled then option is shown`() {
        configureGetPin(FeatureStatus.ENABLED, FeatureType.Voip())

        sut = createSut()

        assertTrue(sut.state.getOrAwaitValue().showGetPin)
    }

    @Test
    fun `when setPin is disabled then option is not shown`() {
        configureSetPin(FeatureStatus.DISABLED)

        sut = createSut()

        assertFalse(sut.state.getOrAwaitValue().showSetPin)
    }

    @Test
    fun `when setPin is enabled then option is shown`() {
        configureSetPin(FeatureStatus.ENABLED)

        sut = createSut()

        assertTrue(sut.state.getOrAwaitValue().showSetPin)
    }

    @Test
    fun `when ivr is disabled then option is not shown`() {
        configureIvrSupport(FeatureStatus.DISABLED, null)

        sut = createSut()

        assertFalse(sut.state.getOrAwaitValue().showSetPin)
    }

    @Test
    fun `given ivr enabled & no phone number then option is not shown`() {
        configureIvrSupport(FeatureStatus.ENABLED, null)

        sut = createSut()

        assertFalse(sut.state.getOrAwaitValue().showIvrSupport)
    }

    @Test
    fun `given ivr enabled & phone number then option is not shown`() {
        configureIvrSupport(FeatureStatus.ENABLED, phoneNumber)

        sut = createSut()

        assertTrue(sut.state.getOrAwaitValue().showIvrSupport)
    }

    @Test
    fun `when funding is disabled then option is not shown`() {
        configureFunding(false)

        sut = createSut()

        assertFalse(sut.state.getOrAwaitValue().showAddFunds)
    }

    @Test
    fun `when funding is enabled then option is shown`() {
        configureFunding(true)

        sut = createSut()

        assertTrue(sut.state.getOrAwaitValue().showAddFunds)
    }

    @Test
    fun `when passcode is disabled then option is not shown`() {
        configurePasscode(false)

        sut = createSut()

        assertFalse(sut.state.getOrAwaitValue().showPasscode)
    }

    @Test
    fun `when passcode is enabled then option is shown`() {
        configurePasscode(true)

        sut = createSut()

        assertTrue(sut.state.getOrAwaitValue().showPasscode)
    }

    @Test
    fun `when cardState is active then cardLocked is false`() {
        whenever(card.state).thenReturn(Card.CardState.ACTIVE)

        sut = createSut()

        assertFalse(sut.state.getOrAwaitValue().cardLocked)
    }

    @Test
    fun `when cardState is inactive then cardLocked is true`() {
        whenever(card.state).thenReturn(Card.CardState.INACTIVE)

        sut = createSut()

        assertTrue(sut.state.getOrAwaitValue().cardLocked)
    }

    @Test
    fun `when cardState is cancelled then cardLocked is true`() {
        whenever(card.state).thenReturn(Card.CardState.CANCELLED)

        sut = createSut()

        assertTrue(sut.state.getOrAwaitValue().cardLocked)
    }

    @Test
    fun `given transferMoney is disabled then option is not shown`() {
        configureTransferMoney(enabled = false)

        sut = createSut()

        assertFalse(sut.state.getOrAwaitValue().showTransferMoney)
    }

    @Test
    fun `given transferMoney is enabled option is not shown`() {
        configureTransferMoney(enabled = true)

        sut = createSut()

        assertTrue(sut.state.getOrAwaitValue().showTransferMoney)
    }

    @Test
    fun `given showMonthlyStatementOption is true when sut is created then showMonthlyStatement state is true`() {
        whenever(cardOptionsMock.showMonthlyStatementOption()).thenReturn(true)

        sut = createSut()

        assertTrue(sut.state.getOrAwaitValue().showMonthlyStatement)
    }

    @Test
    fun `given showMonthlyStatementOption is false when sut is created then showMonthlyStatement state is false`() {
        whenever(cardOptionsMock.showMonthlyStatementOption()).thenReturn(false)

        sut = createSut()

        assertFalse(sut.state.getOrAwaitValue().showMonthlyStatement)
    }

    @Test
    fun `when orderedStatus is AVAILABLE then showOrderPhysical is true`() {
        whenever(card.orderedStatus).thenReturn(Card.OrderedStatus.AVAILABLE)

        sut = createSut()

        assertTrue(sut.state.getOrAwaitValue().showOrderPhysical)
    }

    @Test
    fun `when orderedStatus is ORDERED then showOrderPhysical is true`() {
        whenever(card.orderedStatus).thenReturn(Card.OrderedStatus.ORDERED)

        sut = createSut()

        assertFalse(sut.state.getOrAwaitValue().showOrderPhysical)
    }

    @Test
    fun `given feature on and satisfyHardware when create viewModel then showAddToGooglePay is true`() {
        givenInAppProvisioningFeature(true)
        whenever(iapHelper.satisfyHardwareRequisites()).thenReturn(true)

        sut = createSut()

        assertTrue(sut.state.getOrAwaitValue().showAddToGooglePay)
    }

    @Test
    fun `given feature off and satisfyHardware when create viewModel then showAddToGooglePay is true`() {
        givenInAppProvisioningFeature(true)
        whenever(iapHelper.satisfyHardwareRequisites()).thenReturn(false)

        sut = createSut()

        assertFalse(sut.state.getOrAwaitValue().showAddToGooglePay)
    }

    @Test
    fun `given feature on and satisfyHardware off when create viewModel then showAddToGooglePay is true`() {
        givenInAppProvisioningFeature(false)
        whenever(iapHelper.satisfyHardwareRequisites()).thenReturn(true)

        sut = createSut()

        assertFalse(sut.state.getOrAwaitValue().showAddToGooglePay)
    }

    @Test
    fun `when faqPressed then correct content presenter called`() {
        val element = mock<Content>()
        whenever(cardProduct.faq).thenReturn(element)
        sut = createSut()

        sut.onFaqPressed()
        val content = sut.action.getOrAwaitValue() as Action.ContentPresenter

        assertEquals(element, content.content)
        assertEquals("card_settings_legal_faq_title", content.title)
    }

    @Test
    fun `when cardHolderAgreementPressed then correct content presenter called`() {
        val element = mock<Content>()
        whenever(cardProduct.cardholderAgreement).thenReturn(element)
        sut = createSut()

        sut.onCardholderAgreementPressed()
        val content = sut.action.getOrAwaitValue() as Action.ContentPresenter

        assertEquals(element, content.content)
        assertEquals("card_settings_legal_cardholder_agreement_title", content.title)
    }

    @Test
    fun `when onPrivacyPolicyPressed then correct content presenter called`() {
        val element = mock<Content>()
        whenever(cardProduct.privacyPolicy).thenReturn(element)
        sut = createSut()

        sut.onPrivacyPolicyPressed()
        val content = sut.action.getOrAwaitValue() as Action.ContentPresenter

        assertEquals(element, content.content)
        assertEquals("card_settings_legal_privacy_policy_title", content.title)
    }

    @Test
    fun `when onTermsPressed then correct content presenter called`() {
        val element = mock<Content>()
        whenever(cardProduct.termsAndConditions).thenReturn(element)
        sut = createSut()

        sut.onTermsPressed()
        val content = sut.action.getOrAwaitValue() as Action.ContentPresenter

        assertEquals(element, content.content)
        assertEquals("card_settings_legal_terms_of_service_title", content.title)
    }

    @Test
    fun `when setPasscodePressed and card Enabled then SetCardPasscode action is fired`() {
        whenever(card.state).thenReturn(Card.CardState.ACTIVE)
        sut = createSut()

        sut.setPasscodePressed()

        assertTrue(sut.action.getOrAwaitValue() is Action.SetCardPasscode)
    }

    @Test
    fun `when setPasscodePressed and card Inactive then SetCardPasscodeErrorDisabled action is fired`() {
        whenever(card.state).thenReturn(Card.CardState.INACTIVE)
        sut = createSut()

        sut.setPasscodePressed()

        assertTrue(sut.action.getOrAwaitValue() is Action.SetCardPasscodeErrorDisabled)
    }

    @Test
    fun `when lockCard then CardStateChanged if apiCall was successful`() {
        sut = createSut()
        whenever(aptoPlatform.lockCard(eq(CARD_ID), TestDataProvider.anyObject())).thenAnswer { invocation ->
            (invocation.arguments[1] as (Either<Failure, Card>) -> Unit).invoke(
                TestDataProvider.provideCard(CARD_ID).right()
            )
        }

        sut.lockCard()

        assertTrue(sut.action.getOrAwaitValue() is Action.CardStateChanged)
    }

    @Test
    fun `when unlockCard then CardStateChanged if apiCall was successful`() {
        sut = createSut()
        whenever(aptoPlatform.unlockCard(eq(CARD_ID), TestDataProvider.anyObject())).thenAnswer { invocation ->
            (invocation.arguments[1] as (Either<Failure, Card>) -> Unit).invoke(
                TestDataProvider.provideCard(CARD_ID).right()
            )
        }

        sut.unlockCard()

        assertTrue(sut.action.getOrAwaitValue() is Action.CardStateChanged)
    }

    @Test
    fun `when onCustomerSupport then action is CustomerSupportEmail`() {
        sut = createSut()

        sut.onCustomerSupport()
        val action = sut.action.getOrAwaitValue()

        assertTrue(action is Action.CustomerSupportEmail)
    }

    private fun createSut() =
        CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol, cardActionRepo)

    @Test
    fun `given correct sim when click on IVR then call action`() {
        configureIvrSupport(FeatureStatus.ENABLED, phoneNumber)
        whenever(telephonyEnabledChecker.isEnabled()).thenReturn(true)
        sut = createSut()

        sut.onIvrSupportClicked()
        val action = sut.action.getOrAwaitValue()

        assertTrue(action is Action.CallIvr)
        assertEquals(phoneNumber, action.phoneNumber)
    }

    @Test
    fun `given no sim when click on IVR then Show no sim error`() {
        configureIvrSupport(FeatureStatus.ENABLED, phoneNumber)
        whenever(telephonyEnabledChecker.isEnabled()).thenReturn(false)
        sut = createSut()

        sut.onIvrSupportClicked()
        val action = sut.action.getOrAwaitValue()

        assertTrue(action is Action.ShowNoSimInsertedError)
    }

    @Test
    fun `given GetPinFeatureType-Voip when getPin is clicked then CallVoIpListenPin`() {
        configureGetPin(FeatureStatus.ENABLED, FeatureType.Voip())
        sut = createSut()

        sut.getPinPressed()
        val action = sut.action.getOrAwaitValue()

        assertTrue(action is Action.CallVoIpListenPin)
    }

    @Test
    fun `given GetPinFeatureType-IVR and sim correct when getPin is clicked then CallIvr`() {
        configureGetPin(FeatureStatus.ENABLED, FeatureType.Ivr(phoneNumber))
        whenever(telephonyEnabledChecker.isEnabled()).thenReturn(true)

        sut = createSut()

        sut.getPinPressed()
        val action = sut.action.getOrAwaitValue()

        assertTrue(action is Action.CallIvr)
        assertEquals(phoneNumber, action.phoneNumber)
    }

    @Test
    fun `given GetPinFeatureType-IVR and no sim when getPin is clicked then ShowNoSimInsertedError`() {
        configureGetPin(FeatureStatus.ENABLED, FeatureType.Ivr(phoneNumber))
        whenever(telephonyEnabledChecker.isEnabled()).thenReturn(false)
        sut = createSut()

        sut.getPinPressed()
        val action = sut.action.getOrAwaitValue()

        assertTrue(action is Action.ShowNoSimInsertedError)
    }

    @Test
    fun `when addFundsPressed and null AchAccount Feature then AddFunds action`() {
        sut = createSut()

        sut.onAddFundsPressed()

        assertTrue(sut.action.getOrAwaitValue() is Action.AddFunds)
    }

    @Test
    fun `when addFundsPressed and AchAccount disabled then AddFunds action`() {
        configureAchAccount(enabled = false, provisioned = false)
        sut = createSut()

        sut.onAddFundsPressed()

        assertTrue(sut.action.getOrAwaitValue() is Action.AddFunds)
    }

    @Test
    fun `given ach enabled an set when addFundsPressed then ShowAddFundsSelector action`() {
        configureAchAccount(enabled = true, provisioned = true)
        sut = createSut()

        sut.onAddFundsPressed()

        assertTrue(sut.action.getOrAwaitValue() is Action.ShowAddFundsSelector)
    }

    @Test
    fun `given ach enabled but not set when addFundsPressed then ShowAddFundsSelector action`() {
        configureAchAccount(enabled = true, provisioned = false)
        sut = createSut()

        sut.onAddFundsPressed()

        assertTrue(sut.action.getOrAwaitValue() is Action.ShowAddFundsAchDisclaimer)
    }

    @Test
    fun `when orderPhysicalCard then OrderPhysicalCard action`() {
        sut = createSut()

        sut.orderPhysicalCard()

        assertTrue(sut.action.getOrAwaitValue() is Action.OrderPhysicalCard)
    }

    @Test
    fun `when transferMoneyPressed then TransferMoneyAction action`() {
        sut = createSut()

        sut.onTransferMoneyPressed()

        assertTrue(sut.action.getOrAwaitValue() is Action.TransferMoneyAction)
    }

    @Test
    fun `when setPinPressed then SetPin action`() {
        sut = createSut()

        sut.onSetPinPressed()

        assertTrue(sut.action.getOrAwaitValue() is Action.SetPin)
    }

    @Test
    fun `given exchange rates set when onExchangeRatesPressed then correct content presenter called`() {
        val element = mock<Content>()
        whenever(cardProduct.exchangeRates).thenReturn(element)
        sut = createSut()

        sut.onExchangeRatesPressed()
        val content = sut.action.getOrAwaitValue() as Action.ContentPresenter

        assertEquals(element, content.content)
        assertEquals("card_settings_legal_exchange_rates_title", content.title)
    }

    private fun configureGetPin(statusResult: FeatureStatus, featureType: FeatureType?) {
        val feature = mock<GetPin> {
            on { status } doReturn statusResult
            featureType?.let { on { type } doReturn featureType }
        }
        whenever(cardFeatures.getPin).thenReturn(feature)
    }

    private fun configureSetPin(statusResult: FeatureStatus) {
        val feature = mock<SetPin> {
            on { status } doReturn statusResult
        }
        whenever(cardFeatures.setPin).thenReturn(feature)
    }

    private fun configureIvrSupport(statusResult: FeatureStatus, phone: PhoneNumber?) {
        val feature = mock<Ivr> {
            on { status } doReturn statusResult
            on { ivrPhone } doReturn phone
        }
        whenever(cardFeatures.ivrSupport).thenReturn(feature)
    }

    private fun configureFunding(statusResult: Boolean) {
        val feature = mock<FundingFeature> {
            on { isEnabled } doReturn statusResult
        }
        whenever(cardFeatures.funding).thenReturn(feature)
    }

    private fun configurePasscode(enabled: Boolean) {
        val feature = mock<CardPasscodeFeature> {
            on { isEnabled } doReturn enabled
        }
        whenever(cardFeatures.passcode).thenReturn(feature)
    }

    private fun configureAchAccount(enabled: Boolean, provisioned: Boolean) {
        val feature = mock<AchAccountFeature> {
            on { isEnabled } doReturn enabled
            on { isAccountProvisioned } doReturn provisioned
        }
        whenever(cardFeatures.achAccount).thenReturn(feature)
    }

    private fun givenInAppProvisioningFeature(enabled: Boolean) {
        val feature = mock<GenericFeature> {
            on { isEnabled } doReturn enabled
        }
        whenever(cardFeatures.inAppProvisioning).thenReturn(feature)
    }

    private fun configureTransferMoney(enabled: Boolean) {
        val feature = mock<GenericFeature> {
            on { isEnabled } doReturn enabled
        }
        whenever(cardFeatures.transferMoneyP2p).thenReturn(feature)
    }
}
