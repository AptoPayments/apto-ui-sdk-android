package com.aptopayments.sdk.features.card.cardsettings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.data.card.*
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CardSettingsViewModelTest : AndroidTest() {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private val cardFeatures = mock<Features>()
    private val card = mock<Card>()
    private val cardProduct = mock<CardProduct>()
    private val analytics = mock<AnalyticsServiceContract>()
    private val aptoPlatform = mock<AptoPlatform>()
    private val cardOptionsMock = mock<CardOptions>()
    private val aptoUiSdkProtocol = mock<AptoUiSdkProtocol> {
        on { cardOptions } doReturn cardOptionsMock
    }

    private lateinit var sut: CardSettingsViewModel

    @Test
    fun `when cardProduct empty sections are hidden`() {
        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        assertFalse(sut.showLegalSection)
        assertFalse(sut.showFaq)
        assertFalse(sut.showCardholderAgreement)
        assertFalse(sut.showTermsAndConditions)
        assertFalse(sut.showPrivacyPolicy)
    }

    @Test
    fun `when faq is set then showFaq is true`() {
        whenever(cardProduct.faq).thenReturn(mock())

        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        assertTrue(sut.showFaq)
    }

    @Test
    fun `when CHA is set then legal and CHA are shown`() {
        whenever(cardProduct.cardholderAgreement).thenReturn(mock())

        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        assertTrue(sut.showLegalSection)
        assertTrue(sut.showCardholderAgreement)
    }

    @Test
    fun `when Terms are set then legal and terms are shown`() {
        whenever(cardProduct.termsAndConditions).thenReturn(mock())

        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        assertTrue(sut.showLegalSection)
        assertTrue(sut.showTermsAndConditions)
    }

    @Test
    fun `when privacy policy is set then legal and privacy section is shown`() {
        whenever(cardProduct.privacyPolicy).thenReturn(mock())

        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        assertTrue(sut.showLegalSection)
        assertTrue(sut.showPrivacyPolicy)
    }

    @Test
    fun `when getPin is disabled then option is not shown`() {
        configureGetPin(FeatureStatus.DISABLED)
        configureCardFeatures()

        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        assertFalse(sut.showGetPin.getOrAwaitValue())
    }

    @Test
    fun `when getPin is enabled then option is shown`() {
        configureGetPin(FeatureStatus.ENABLED)
        configureCardFeatures()

        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        assertTrue(sut.showGetPin.getOrAwaitValue())
    }

    @Test
    fun `when setPin is disabled then option is not shown`() {
        configureSetPin(FeatureStatus.DISABLED)
        configureCardFeatures()

        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        assertFalse(sut.showSetPin.getOrAwaitValue())
    }

    @Test
    fun `when setPin is enabled then option is shown`() {
        configureSetPin(FeatureStatus.ENABLED)
        configureCardFeatures()

        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        assertTrue(sut.showSetPin.getOrAwaitValue())
    }

    @Test
    fun `when ivr is disabled then option is not shown`() {
        configureIvrSupport(FeatureStatus.DISABLED)
        configureCardFeatures()

        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        assertFalse(sut.showSetPin.getOrAwaitValue())
    }

    @Test
    fun `when ivr is enabled then option is shown`() {
        configureIvrSupport(FeatureStatus.ENABLED)
        configureCardFeatures()

        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        assertTrue(sut.showIvrSupport.getOrAwaitValue())
    }

    @Test
    fun `when funding is disabled then option is not shown`() {
        configureFunding(false)
        configureCardFeatures()

        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        assertFalse(sut.showAddFunds.getOrAwaitValue())
    }

    @Test
    fun `when funding is enabled then option is shown`() {
        configureFunding(true)
        configureCardFeatures()

        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        assertTrue(sut.showAddFunds.getOrAwaitValue())
    }

    @Test
    fun `when cardState is active then cardLocked is false`() {
        whenever(card.state).thenReturn(Card.CardState.ACTIVE)

        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        assertFalse(sut.cardLocked.getOrAwaitValue())
    }

    @Test
    fun `when cardState is inactive then cardLocked is true`() {
        whenever(card.state).thenReturn(Card.CardState.INACTIVE)

        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        assertTrue(sut.cardLocked.getOrAwaitValue())
    }

    @Test
    fun `when cardState is cancelled then cardLocked is true`() {
        whenever(card.state).thenReturn(Card.CardState.CANCELLED)

        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        assertTrue(sut.cardLocked.getOrAwaitValue())
    }

    @Test
    fun `when faqPressed then correct content presenter called`() {
        val element = mock<Content>()
        whenever(cardProduct.faq).thenReturn(element)
        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        sut.onFaqPressed()
        val content = sut.showContentPresenter.getOrAwaitValue()

        assertEquals(element, content.first)
        assertEquals("card_settings_legal_faq_title", content.second)
    }

    @Test
    fun `when cardHolderAgreementPressed then correct content presenter called`() {
        val element = mock<Content>()
        whenever(cardProduct.cardholderAgreement).thenReturn(element)
        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        sut.onCardholderAgreementPressed()
        val content = sut.showContentPresenter.getOrAwaitValue()

        assertEquals(element, content.first)
        assertEquals("card_settings_legal_cardholder_agreement_title", content.second)
    }

    @Test
    fun `when onPrivacyPolicyPressed then correct content presenter called`() {
        val element = mock<Content>()
        whenever(cardProduct.privacyPolicy).thenReturn(element)
        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        sut.onPrivacyPolicyPressed()
        val content = sut.showContentPresenter.getOrAwaitValue()

        assertEquals(element, content.first)
        assertEquals("card_settings_legal_privacy_policy_title", content.second)
    }

    @Test
    fun `when onTermsPressed then correct content presenter called`() {
        val element = mock<Content>()
        whenever(cardProduct.termsAndConditions).thenReturn(element)
        sut = CardSettingsViewModel(card, cardProduct, analytics, aptoPlatform, aptoUiSdkProtocol)

        sut.onTermsPressed()
        val content = sut.showContentPresenter.getOrAwaitValue()

        assertEquals(element, content.first)
        assertEquals("card_settings_legal_terms_of_service_title", content.second)
    }

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

    private fun configureCardFeatures() {
        whenever(card.features).thenReturn(cardFeatures)
    }
}
