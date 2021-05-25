package com.aptopayments.sdk.features.card.cardsettings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.data.PhoneNumber
import com.aptopayments.mobile.data.card.*
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.applicationModule
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.card.cardsettings.CardSettingsViewModel.Action
import com.aptopayments.sdk.repository.LocalCardDetailsRepository
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val CARD_ID = "CARD_ID"

@Suppress("UNCHECKED_CAST")
internal class CardSettingsViewModelTest : UnitTest() {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private val cardDetailsRepo = mock<LocalCardDetailsRepository>()
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
    private val telephonyEnabledChecker: TelephonyEnabledChecker = mock()
    private val phoneNumber = PhoneNumber("1", "111111111")

    private lateinit var sut: CardSettingsViewModel

    @Before
    fun setUp() {
        startKoin {
            modules(
                listOf(
                    applicationModule,
                    module {
                        factory(override = true) { cardDetailsRepo }
                        factory(override = true) { telephonyEnabledChecker }
                    }
                )
            )
        }
    }

    @Test
    fun `when cardProduct empty sections are hidden`() {
        sut = createSut()

        assertFalse(sut.showLegalSection)
        assertFalse(sut.showFaq)
        assertFalse(sut.showCardholderAgreement)
        assertFalse(sut.showTermsAndConditions)
        assertFalse(sut.showPrivacyPolicy)
        assertFalse(sut.showExchangeRates)
    }

    @Test
    fun `when faq is set then showFaq is true`() {
        whenever(cardProduct.faq).thenReturn(mock())

        sut = createSut()

        assertTrue(sut.showFaq)
    }

    @Test
    fun `when CHA is set then legal and CHA are shown`() {
        whenever(cardProduct.cardholderAgreement).thenReturn(mock())

        sut = createSut()

        assertTrue(sut.showLegalSection)
        assertTrue(sut.showCardholderAgreement)
    }

    @Test
    fun `when Terms are set then legal and terms are shown`() {
        whenever(cardProduct.termsAndConditions).thenReturn(mock())

        sut = createSut()

        assertTrue(sut.showLegalSection)
        assertTrue(sut.showTermsAndConditions)
    }

    @Test
    fun `when privacy policy is set then legal and privacy section is shown`() {
        whenever(cardProduct.privacyPolicy).thenReturn(mock())

        sut = createSut()

        assertTrue(sut.showLegalSection)
        assertTrue(sut.showPrivacyPolicy)
    }

    @Test
    fun `given exchange_rates content is present when sut created then legal and exchange rates section is shown`() {
        whenever(cardProduct.exchangeRates).thenReturn(mock())

        sut = createSut()

        assertTrue(sut.showLegalSection)
        assertTrue(sut.showExchangeRates)
    }

    @Test
    fun `when getPin is disabled then option is not shown`() {
        configureGetPin(FeatureStatus.DISABLED, null)

        sut = createSut()

        assertFalse(sut.cardUiState.getOrAwaitValue().showGetPin)
    }

    @Test
    fun `when getPin is enabled then option is shown`() {
        configureGetPin(FeatureStatus.ENABLED, FeatureType.Voip())

        sut = createSut()

        assertTrue(sut.cardUiState.getOrAwaitValue().showGetPin)
    }

    @Test
    fun `when setPin is disabled then option is not shown`() {
        configureSetPin(FeatureStatus.DISABLED)

        sut = createSut()

        assertFalse(sut.cardUiState.getOrAwaitValue().showSetPin)
    }

    @Test
    fun `when setPin is enabled then option is shown`() {
        configureSetPin(FeatureStatus.ENABLED)

        sut = createSut()

        assertTrue(sut.cardUiState.getOrAwaitValue().showSetPin)
    }

    @Test
    fun `when ivr is disabled then option is not shown`() {
        configureIvrSupport(FeatureStatus.DISABLED, null)

        sut = createSut()

        assertFalse(sut.cardUiState.getOrAwaitValue().showSetPin)
    }

    @Test
    fun `given ivr enabled & no phone number then option is not shown`() {
        configureIvrSupport(FeatureStatus.ENABLED, null)

        sut = createSut()

        assertFalse(sut.cardUiState.getOrAwaitValue().showIvrSupport)
    }

    @Test
    fun `given ivr enabled & phone number then option is not shown`() {
        configureIvrSupport(FeatureStatus.ENABLED, phoneNumber)

        sut = createSut()

        assertTrue(sut.cardUiState.getOrAwaitValue().showIvrSupport)
    }

    @Test
    fun `when funding is disabled then option is not shown`() {
        configureFunding(false)

        sut = createSut()

        assertFalse(sut.cardUiState.getOrAwaitValue().showAddFunds)
    }

    @Test
    fun `when funding is enabled then option is shown`() {
        configureFunding(true)

        sut = createSut()

        assertTrue(sut.cardUiState.getOrAwaitValue().showAddFunds)
    }

    @Test
    fun `when passcode is disabled then option is not shown`() {
        configurePasscode(false)

        sut = createSut()

        assertFalse(sut.cardUiState.getOrAwaitValue().showPasscode)
    }

    @Test
    fun `when passcode is enabled then option is shown`() {
        configurePasscode(true)

        sut = createSut()

        assertTrue(sut.cardUiState.getOrAwaitValue().showPasscode)
    }

    @Test
    fun `when cardState is active then cardLocked is false`() {
        whenever(card.state).thenReturn(Card.CardState.ACTIVE)

        sut = createSut()

        assertFalse(sut.cardUiState.getOrAwaitValue().cardLocked)
    }

    @Test
    fun `when cardState is inactive then cardLocked is true`() {
        whenever(card.state).thenReturn(Card.CardState.INACTIVE)

        sut = createSut()

        assertTrue(sut.cardUiState.getOrAwaitValue().cardLocked)
    }

    @Test
    fun `when cardState is cancelled then cardLocked is true`() {
        whenever(card.state).thenReturn(Card.CardState.CANCELLED)

        sut = createSut()

        assertTrue(sut.cardUiState.getOrAwaitValue().cardLocked)
    }

    @Test
    fun `given showMonthlyStatementOption is true when sut is created then showMonthlyStatement state is true`() {
        whenever(cardOptionsMock.showMonthlyStatementOption()).thenReturn(true)

        sut = createSut()

        assertTrue(sut.cardUiState.getOrAwaitValue().showMonthlyStatement)
    }

    @Test
    fun `given showMonthlyStatementOption is false when sut is created then showMonthlyStatement state is false`() {
        whenever(cardOptionsMock.showMonthlyStatementOption()).thenReturn(false)

        sut = createSut()

        assertFalse(sut.cardUiState.getOrAwaitValue().showMonthlyStatement)
    }

    @Test
    fun `when orderedStatus is AVAILABLE then showOrderPhysical is true`() {
        whenever(card.orderedStatus).thenReturn(Card.OrderedStatus.AVAILABLE)

        sut = createSut()

        assertTrue(sut.cardUiState.getOrAwaitValue().showOrderPhysical)
    }

    @Test
    fun `when orderedStatus is ORDERED then showOrderPhysical is true`() {
        whenever(card.orderedStatus).thenReturn(Card.OrderedStatus.ORDERED)

        sut = createSut()

        assertFalse(sut.cardUiState.getOrAwaitValue().showOrderPhysical)
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
        CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

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
        val feature = mock<GetPin>() {
            on { status } doReturn statusResult
            featureType?.let { on { type } doReturn featureType }
        }
        whenever(cardFeatures.getPin).thenReturn(feature)
    }

    private fun configureSetPin(statusResult: FeatureStatus) {
        val feature = mock<SetPin>() {
            on { status } doReturn statusResult
        }
        whenever(cardFeatures.setPin).thenReturn(feature)
    }

    private fun configureIvrSupport(statusResult: FeatureStatus, phone: PhoneNumber?) {
        val feature = mock<Ivr>() {
            on { status } doReturn statusResult
            on { ivrPhone } doReturn phone
        }
        whenever(cardFeatures.ivrSupport).thenReturn(feature)
    }

    private fun configureFunding(statusResult: Boolean) {
        val feature = mock<FundingFeature>() {
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
}
