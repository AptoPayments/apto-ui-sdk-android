package com.aptopayments.sdk.features.card.cardsettings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.data.card.*
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.config.ProjectConfiguration
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.AndroidTest
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
internal class CardSettingsViewModelTest : AndroidTest() {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private val cardDetailsRepo = mock<LocalCardDetailsRepository>()
    private val cardFeatures = mock<Features>()
    private val card = mock<Card> {
        on { accountID } doReturn CARD_ID
    }
    private val projectConfiguration = mock<ProjectConfiguration>()
    private val cardProduct = mock<CardProduct>()
    private val analytics = mock<AnalyticsServiceContract>()
    private val aptoPlatform = mock<AptoPlatform>()
    private val cardOptionsMock = mock<CardOptions>()
    private val aptoUiSdkProtocol = mock<AptoUiSdkProtocol> {
        on { cardOptions } doReturn cardOptionsMock
    }

    private lateinit var sut: CardSettingsViewModel

    @Before
    override fun setUp() {
        super.setUp()
        startKoin {
            modules(
                listOf(
                    applicationModule,
                    module {
                        factory(override = true) { cardDetailsRepo }
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
    fun `when getPin is disabled then option is not shown`() {
        configureGetPin(FeatureStatus.DISABLED)
        configureCardFeatures()

        sut = createSut()

        assertFalse(sut.cardUiState.getOrAwaitValue().showGetPin)
    }

    @Test
    fun `when getPin is enabled then option is shown`() {
        configureGetPin(FeatureStatus.ENABLED)
        configureCardFeatures()

        sut = createSut()

        assertTrue(sut.cardUiState.getOrAwaitValue().showGetPin)
    }

    @Test
    fun `when setPin is disabled then option is not shown`() {
        configureSetPin(FeatureStatus.DISABLED)
        configureCardFeatures()

        sut = createSut()

        assertFalse(sut.cardUiState.getOrAwaitValue().showSetPin)
    }

    @Test
    fun `when setPin is enabled then option is shown`() {
        configureSetPin(FeatureStatus.ENABLED)
        configureCardFeatures()

        sut = createSut()

        assertTrue(sut.cardUiState.getOrAwaitValue().showSetPin)
    }

    @Test
    fun `when ivr is disabled then option is not shown`() {
        configureIvrSupport(FeatureStatus.DISABLED)
        configureCardFeatures()

        sut = createSut()

        assertFalse(sut.cardUiState.getOrAwaitValue().showSetPin)
    }

    @Test
    fun `when ivr is enabled then option is shown`() {
        configureIvrSupport(FeatureStatus.ENABLED)
        configureCardFeatures()

        sut = createSut()

        assertTrue(sut.cardUiState.getOrAwaitValue().showIvrSupport)
    }

    @Test
    fun `when funding is disabled then option is not shown`() {
        configureFunding(false)
        configureCardFeatures()

        sut = createSut()

        assertFalse(sut.cardUiState.getOrAwaitValue().showAddFunds)
    }

    @Test
    fun `when funding is enabled then option is shown`() {
        configureFunding(true)
        configureCardFeatures()

        sut = createSut()

        assertTrue(sut.cardUiState.getOrAwaitValue().showAddFunds)
    }

    @Test
    fun `when passcode is disabled then option is not shown`() {
        configurePasscode(false)
        configureCardFeatures()

        sut = createSut()

        assertFalse(sut.cardUiState.getOrAwaitValue().showPasscode)
    }

    @Test
    fun `when passcode is enabled then option is shown`() {
        configurePasscode(true)
        configureCardFeatures()

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
    fun `when onCustomerSupport and isChatbotActive then action is chatbot`() {
        whenever(projectConfiguration.isChatbotActive).thenReturn(true)
        whenever(card.accountID).thenReturn("1")
        whenever(card.cardHolder).thenReturn("Jhon Snow")
        sut = createSut()

        sut.onCustomerSupport()
        val action = sut.action.getOrAwaitValue()

        assertTrue(action is Action.StartChatbot)
    }

    @Test
    fun `when onCustomerSupport and !isChatbotActive then action is CustomerSupportEmail`() {
        whenever(projectConfiguration.isChatbotActive).thenReturn(false)
        sut = createSut()

        sut.onCustomerSupport()
        val action = sut.action.getOrAwaitValue()

        assertTrue(action is Action.CustomerSupportEmail)
    }

    private fun createSut() =
        CardSettingsViewModel(card, cardProduct, projectConfiguration, analytics, aptoPlatform, aptoUiSdkProtocol)

    private fun configureGetPin(statusResult: FeatureStatus) {
        val feature = mock<GetPin>() {
            on { status } doReturn statusResult
        }
        whenever(cardFeatures.getPin).thenReturn(feature)
    }

    private fun configureSetPin(statusResult: FeatureStatus) {
        val feature = mock<SetPin>() {
            on { status } doReturn statusResult
        }
        whenever(cardFeatures.setPin).thenReturn(feature)
    }

    private fun configureIvrSupport(statusResult: FeatureStatus) {
        val feature = mock<Ivr>() {
            on { status } doReturn statusResult
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
        val feature = mock<CardPasscodeFeature>() {
            on { isEnabled } doReturn enabled
        }
        whenever(cardFeatures.passcode).thenReturn(feature)
    }

    private fun configureCardFeatures() {
        whenever(card.features).thenReturn(cardFeatures)
    }
}
