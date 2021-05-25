package com.aptopayments.sdk.features.managecard

import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.KycStatus
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.config.ContextConfiguration
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.features.card.fundingsources.FundingSourceDialogFragment
import com.aptopayments.sdk.features.card.waitlist.WaitlistFragment
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Suppress("UNCHECKED_CAST")
class ManageCardFlowTest : UnitTest() {

    private lateinit var sut: ManageCardFlow
    private val mockFragmentFactory: FragmentFactory = mock()
    private val mockConfig: ContextConfiguration = mock()
    private val mockCardProduct: CardProduct = mock()
    private val mockAptoPlatform: AptoPlatform = mock()
    private val cardId = "TEST_CARD_ID"

    @Before
    fun setUp() {
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
        startKoin {
            modules(
                module {
                    single { mockFragmentFactory }
                    single<AptoPlatformProtocol> { mockAptoPlatform }
                }
            )
        }
        sut = ManageCardFlow(cardId = cardId, contextConfiguration = mockConfig, onClose = {})
    }

    @Test
    fun `should start the KYC flow on init if KYC is not passed`() {
        // Given
        val card = TestDataProvider.provideCard(kycStatus = KycStatus.REJECTED)
        whenever(
            mockAptoPlatform.fetchCard(
                anyString(),
                anyBoolean(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(Either.Right(card))
        }
        val sutSpy = spy(sut)
        doNothing().whenever(sutSpy).initKycFlow(TestDataProvider.anyObject(), TestDataProvider.anyObject())

        // When
        sutSpy.init {}

        // Then
        verify(sutSpy).initKycFlow(TestDataProvider.anyObject(), TestDataProvider.anyObject())
    }

    @Test
    fun `should use the factory to instantiate WaitListFragmentInterface as first fragment if KYC is passed and card is waitlisted`() {
        // Given
        val card = TestDataProvider.provideCard(accountID = cardId, kycStatus = KycStatus.PASSED, isWaitlisted = true)
        whenever(
            mockAptoPlatform.fetchCard(
                anyString(),
                anyBoolean(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(Either.Right(card))
        }

        whenever(
            mockAptoPlatform.fetchCardProduct(
                anyString(),
                anyBoolean(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, CardProduct>) -> Unit).invoke(Either.Right(mockCardProduct))
        }

        val tag = "WaitlistFragment"
        val fragmentDouble = mock<WaitlistFragment> { on { TAG } doReturn tag }
        given {
            mockFragmentFactory.waitlistFragment(cardId, mockCardProduct, tag)
        }.willReturn(fragmentDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).waitlistFragment(cardId, mockCardProduct, tag)
    }

    @Test
    fun `should use the factory to instantiate ManageCardFragmentInterface as first fragment if KYC is passed and card is not waitlisted`() {
        // Given
        val card = TestDataProvider.provideCard(accountID = cardId, kycStatus = KycStatus.PASSED, isWaitlisted = false)
        whenever(
            mockAptoPlatform.fetchCard(
                anyString(),
                anyBoolean(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(Either.Right(card))
        }
        val tag = "ManageCardFragment"
        val fragmentDouble = mock<ManageCardFragment> { on { TAG } doReturn tag }
        given {
            mockFragmentFactory.manageCardFragment(tag = tag, cardId = cardId)
        }.willReturn(fragmentDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).manageCardFragment(tag = tag, cardId = cardId)
    }

    @Test
    fun `should use the factory to instantiate ManageCardFragmentInterface when showManageCardFragment is called`() {
        // Given
        val card = TestDataProvider.provideCard(accountID = cardId)
        whenever(
            mockAptoPlatform.fetchCard(
                anyString(),
                anyBoolean(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(Either.Right(card))
        }
        val tag = "ManageCardFragment"
        val fragmentDouble = mock<ManageCardFragment> { on { TAG } doReturn tag }
        given {
            mockFragmentFactory.manageCardFragment(tag = tag, cardId = cardId)
        }.willReturn(fragmentDouble)

        // When
        sut.showManageCardFragment()

        // Then
        verify(mockFragmentFactory).manageCardFragment(tag = tag, cardId = cardId)
    }

    @Test
    fun `should start the Add Balance flow when onAddFundingSource is called`() {
        // Given
        val balanceId = "TEST_BALANCE_ID"
        val card = TestDataProvider.provideCard()
        whenever(
            mockAptoPlatform.fetchCard(
                anyString(),
                anyBoolean(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(Either.Right(card))
        }
        val sutSpy = spy(sut)
        doNothing().whenever(sutSpy).initAddBalanceFlow(card, balanceId)

        // When
        sutSpy.onAddFundingSource(balanceId)

        // Then
        verify(sutSpy).initAddBalanceFlow(card, balanceId)
    }

    @Test
    fun `should use the factory to instantiate FundingSourcesDialogInterface when onFundingSourceTapped is called`() {
        // Given
        val balanceId = "TEST_BALANCE_ID"
        val tag = "FundingSourceDialogFragment"
        val fragmentDouble = mock<FundingSourceDialogFragment>()
        given {
            mockFragmentFactory.fundingSourceFragment(tag = tag, cardID = cardId, selectedBalanceID = balanceId)
        }.willReturn(fragmentDouble)

        // When
        sut.onFundingSourceTapped(balanceId)

        // Then
        verify(mockFragmentFactory).fundingSourceFragment(tag = tag, cardID = cardId, selectedBalanceID = balanceId)
    }

    @Test
    fun `flow is set as manage card fragment delegate when restoring state`() {
        // Given
        val tag = "ManageCardFragment"
        val fragmentDouble = spy<ManageCardFragment>().apply { TAG = tag }
        sut.setStartElement(fragmentDouble)

        // When
        sut.restoreState()

        // Then
        assertNotNull(sut.manageCardFragment)
        assertNotNull(sut.manageCardFragment!!.delegate)
        assertEquals(sut.manageCardFragment!!.delegate, sut)
    }
}
