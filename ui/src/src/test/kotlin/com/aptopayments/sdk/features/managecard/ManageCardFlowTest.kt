package com.aptopayments.sdk.features.managecard

import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.card.KycStatus
import com.aptopayments.core.data.cardproduct.CardProduct
import com.aptopayments.core.data.config.ContextConfiguration
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.config.UITheme
import com.aptopayments.core.data.content.Content
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.features.card.cardsettings.CardSettingsContract
import com.aptopayments.sdk.features.card.fundingsources.FundingSourceContract
import com.aptopayments.sdk.features.card.waitlist.WaitlistContract
import com.aptopayments.sdk.features.contentpresenter.ContentPresenterContract
import com.aptopayments.sdk.features.contentpresenter.ContentPresenterFragmentDouble
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ManageCardFlowTest : AndroidTest() {

    private lateinit var sut: ManageCardFlow
    @Mock private lateinit var mockFragmentFactory: FragmentFactory
    @Mock private lateinit var mockWaitlistDelegate: WaitlistContract.Delegate
    @Mock private lateinit var mockManageCardDelegate: ManageCardContract.Delegate
    @Mock private lateinit var mockCardSettingsDelegate: CardSettingsContract.Delegate
    @Mock private lateinit var mockFundingSourcesDelegate: FundingSourceContract.Delegate
    @Mock private lateinit var mockContentPresenterDelegate: ContentPresenterContract.Delegate
    @Mock private lateinit var mockConfig: ContextConfiguration
    @Mock private lateinit var mockCardProduct: CardProduct
    @Mock private lateinit var mockContent: Content
    @Mock private lateinit var mockAptoPlatform: AptoPlatform
    private val cardId = "TEST_CARD_ID"

    @Before
    override fun setUp() {
        super.setUp()
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
        sut = ManageCardFlow(cardId = cardId, contextConfiguration = mockConfig, onClose = {})
        sut.fragmentFactory = mockFragmentFactory
        sut.aptoPlatformProtocol = mockAptoPlatform
    }

    @Test
    fun `should start the KYC flow on init if KYC is not passed`() {
        // Given
        val card = TestDataProvider.provideCard(kycStatus = KycStatus.REJECTED)
        Mockito.`when`(sut.aptoPlatformProtocol.fetchFinancialAccount(anyString(), anyBoolean(), anyBoolean(), TestDataProvider.anyObject())).thenAnswer { invocation ->
            (invocation.arguments[3] as (Either<Failure, Card>) -> Unit).invoke(Either.Right(card))
        }
        val sutSpy = Mockito.spy(sut)
        Mockito.doNothing().`when`(sutSpy).initKycFlow(TestDataProvider.anyObject(), TestDataProvider.anyObject())

        // When
        sutSpy.init {}

        // Then
        verify(sutSpy).initKycFlow(TestDataProvider.anyObject(), TestDataProvider.anyObject())
    }

    @Test
    fun `should use the factory to instantiate WaitListFragmentInterface as first fragment if KYC is passed and card is waitlisted`() {
        // Given
        val card = TestDataProvider.provideCard(accountID = cardId, kycStatus = KycStatus.PASSED, isWaitlisted = true)
        Mockito.`when`(sut.aptoPlatformProtocol.fetchFinancialAccount(anyString(), anyBoolean(), anyBoolean(), TestDataProvider.anyObject())).thenAnswer { invocation ->
            (invocation.arguments[3] as (Either<Failure, Card>) -> Unit).invoke(Either.Right(card))
        }

        Mockito.`when`(sut.aptoPlatformProtocol.fetchCardProduct(anyString(), anyBoolean(), TestDataProvider.anyObject())).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, CardProduct>) -> Unit).invoke(Either.Right(mockCardProduct))
        }

        val tag = "WaitlistFragment"
        val fragmentDouble = WaitlistFragmentDouble(mockWaitlistDelegate).apply { this.TAG = tag }
        given {
            mockFragmentFactory.waitlistFragment(uiTheme = UITheme.THEME_1, tag = tag, cardId = cardId, cardProduct = mockCardProduct)
        }.willReturn(fragmentDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).waitlistFragment(uiTheme = UITheme.THEME_1, tag = tag, cardId = cardId, cardProduct = mockCardProduct)
    }

    @Test
    fun `should use the factory to instantiate ManageCardFragmentInterface as first fragment if KYC is passed and card is not waitlisted`() {
        // Given
        val card = TestDataProvider.provideCard(accountID = cardId, kycStatus = KycStatus.PASSED, isWaitlisted = false)
        Mockito.`when`(sut.aptoPlatformProtocol.fetchFinancialAccount(anyString(), anyBoolean(), anyBoolean(), TestDataProvider.anyObject())).thenAnswer { invocation ->
            (invocation.arguments[3] as (Either<Failure, Card>) -> Unit).invoke(Either.Right(card))
        }
        val tag = "ManageCardFragment"
        val fragmentDouble = ManageCardFragmentDouble(mockManageCardDelegate).apply { this.TAG = tag }
        given {
            mockFragmentFactory.manageCardFragment(uiTheme = UITheme.THEME_1, tag = tag, cardId = cardId)
        }.willReturn(fragmentDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).manageCardFragment(uiTheme = UITheme.THEME_1, tag = tag, cardId = cardId)
    }

    @Test
    fun `should use the factory to instantiate ManageCardFragmentInterface when showManageCardFragment is called`() {
        // Given
        val card = TestDataProvider.provideCard(accountID = cardId)
        Mockito.`when`(sut.aptoPlatformProtocol.fetchFinancialAccount(anyString(), anyBoolean(), anyBoolean(), TestDataProvider.anyObject())).thenAnswer { invocation ->
            (invocation.arguments[3] as (Either<Failure, Card>) -> Unit).invoke(Either.Right(card))
        }
        val tag = "ManageCardFragment"
        val fragmentDouble = ManageCardFragmentDouble(mockManageCardDelegate).apply { this.TAG = tag }
        given {
            mockFragmentFactory.manageCardFragment(uiTheme = UITheme.THEME_1, tag = tag, cardId = cardId)
        }.willReturn(fragmentDouble)

        // When
        sut.showManageCardFragment()

        // Then
        verify(mockFragmentFactory).manageCardFragment(uiTheme = UITheme.THEME_1, tag = tag, cardId = cardId)
    }

    @Test
    fun `should start the Add Balance flow when onAddFundingSource is called`() {
        // Given
        val balanceId = "TEST_BALANCE_ID"
        val card = TestDataProvider.provideCard()
        Mockito.`when`(sut.aptoPlatformProtocol.fetchFinancialAccount(anyString(), anyBoolean(), anyBoolean(), TestDataProvider.anyObject())).thenAnswer { invocation ->
            (invocation.arguments[3] as (Either<Failure, Card>) -> Unit).invoke(Either.Right(card))
        }
        val sutSpy = Mockito.spy(sut)
        Mockito.doNothing().`when`(sutSpy).initAddBalanceFlow(card, balanceId)

        // When
        sutSpy.onAddFundingSource(balanceId)

        // Then
        verify(sutSpy).initAddBalanceFlow(card, balanceId)
    }

    @Test
    fun `should use the factory to instantiate CardSettingsFragmentInterface when onCardSettingsTapped is called`() {
        // Given
        val card = TestDataProvider.provideCard(accountID = cardId, kycStatus = KycStatus.PASSED, isWaitlisted = false)
        val cardDetailsShown = false
        Mockito.`when`(sut.aptoPlatformProtocol.fetchCardProduct(anyString(), anyBoolean(), TestDataProvider.anyObject())).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, CardProduct>) -> Unit).invoke(Either.Right(mockCardProduct))
        }

        val tag = "CardSettingsFragment"
        val fragmentDouble = CardSettingsFragmentDouble(mockCardSettingsDelegate).apply { this.TAG = tag }
        given {
            mockFragmentFactory.cardSettingsFragment(uiTheme = UITheme.THEME_1, tag = tag, card = card, cardDetailsShown = cardDetailsShown, cardProduct = mockCardProduct, projectConfiguration = mockConfig.projectConfiguration)
        }.willReturn(fragmentDouble)

        // When
        sut.onCardSettingsTapped(card, cardDetailsShown)

        // Then
        verify(mockFragmentFactory).cardSettingsFragment(uiTheme = UITheme.THEME_1, tag = tag, card = card, cardDetailsShown = cardDetailsShown, cardProduct = mockCardProduct, projectConfiguration = mockConfig.projectConfiguration)
    }

    @Test
    fun `should use the factory to instantiate FundingSourcesDialogInterface when onFundingSourceTapped is called`() {
        // Given
        val balanceId = "TEST_BALANCE_ID"
        val tag = "FundingSourceDialogFragment"
        val fragmentDouble = FundingSourcesDialogFragmentDouble(mockFundingSourcesDelegate).apply { this.TAG = tag }
        given {
            mockFragmentFactory.fundingSourceFragment(uiTheme = UITheme.THEME_1, tag = tag, cardID = cardId, selectedBalanceID = balanceId)
        }.willReturn(fragmentDouble)

        // When
        sut.onFundingSourceTapped(balanceId)

        // Then
        verify(mockFragmentFactory).fundingSourceFragment(uiTheme = UITheme.THEME_1, tag = tag, cardID = cardId, selectedBalanceID = balanceId)
    }

    @Test
    fun `should use the factory to instantiate ContentPresenterInterface when showContentPresenter is called`() {
        // Given
        val tag = "ContentPresenterFragment"
        val title = "TEST_TITLE"
        val fragmentDouble = ContentPresenterFragmentDouble(mockContentPresenterDelegate).apply { this.TAG = tag }
        given {
            mockFragmentFactory.contentPresenterFragment(uiTheme = UITheme.THEME_1, tag = tag, content = mockContent, title = title)
        }.willReturn(fragmentDouble)

        // When
        sut.showContentPresenter(mockContent, title)

        // Then
        verify(mockFragmentFactory).contentPresenterFragment(uiTheme = UITheme.THEME_1, tag = tag, content = mockContent, title = title)
    }

    @Test
    fun `flow is set as manage card fragment delegate when restoring state`() {
        // Given
        val tag = "ManageCardFragment"
        val fragmentDouble = ManageCardFragmentDouble(mockManageCardDelegate).apply { this.TAG = tag }
        sut.setStartElement(fragmentDouble)

        // When
        sut.restoreState()

        // Then
        assertNotNull(sut.manageCardFragment)
        assertNotNull(sut.manageCardFragment!!.delegate)
        assertEquals(sut.manageCardFragment!!.delegate, sut)
    }
}
