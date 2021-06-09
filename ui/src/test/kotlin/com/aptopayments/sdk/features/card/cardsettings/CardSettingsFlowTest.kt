package com.aptopayments.sdk.features.card.cardsettings

import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.config.ContextConfiguration
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.features.contentpresenter.ContentPresenterFragment
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.mockito.ArgumentMatchers

private const val cardId = "TEST_CARD_ID"

@Suppress("UNCHECKED_CAST")
class CardSettingsFlowTest : UnitTest() {

    private val mockConfig: ContextConfiguration = mock()
    private val mockFragmentFactory: FragmentFactory = mock()
    private val mockAptoPlatform: AptoPlatform = mock()
    private val mockCardProduct: CardProduct = mock()

    private lateinit var sut: CardSettingsFlow

    @BeforeEach
    fun setUp() {
        startKoin {
            modules(
                module {
                    single { mockFragmentFactory }
                    single<AptoPlatformProtocol> { mockAptoPlatform }
                }
            )
        }
        sut = CardSettingsFlow(
            cardId = cardId,
            contextConfiguration = mockConfig,
            onClose = {},
            onCardStateChanged = {},
            onTransactionsChanged = {}
        )
    }

    @Test
    fun `should use the factory to instantiate CardSettingsFragmentInterface when onCardSettingsTapped is called`() {
        // Given
        val mockCardSettingsDelegate: CardSettingsContract.Delegate = mock()
        val card = TestDataProvider.provideCard(accountID = cardId)
        configureCard(card)
        configureFetchCardProduct()

        val tag = "CardSettingsFragment"
        val fragmentDouble = mock<CardSettingsFragment> {
            on { TAG } doReturn tag
        }
        given {
            mockFragmentFactory.cardSettingsFragment(
                tag = tag,
                card = card,
                cardProduct = mockCardProduct,
                projectConfiguration = mockConfig.projectConfiguration
            )
        }.willReturn(fragmentDouble)

        // When
        sut.init {
            // do nothing
        }

        // Then
        verify(mockFragmentFactory).cardSettingsFragment(
            tag = tag,
            card = card,
            cardProduct = mockCardProduct,
            projectConfiguration = mockConfig.projectConfiguration
        )
    }

    @Test
    fun `should use the factory to instantiate ContentPresenterInterface when showContentPresenter is called`() {

        // Given
        val tag = "ContentPresenterFragment"
        val title = "TEST_TITLE"
        val mockContent: Content = mock()
        val fragmentDouble = mock<ContentPresenterFragment> {
            on { TAG } doReturn tag
        }
        given {
            mockFragmentFactory.contentPresenterFragment(tag = tag, content = mockContent, title = title)
        }.willReturn(fragmentDouble)

        // When
        sut.showContentPresenter(mockContent, title)

        // Then
        verify(mockFragmentFactory).contentPresenterFragment(tag = tag, content = mockContent, title = title)
    }

    private fun configureCard(card: Card) {
        whenever(
            mockAptoPlatform.fetchCard(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyBoolean(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(Either.Right(card))
        }
    }

    private fun configureFetchCardProduct() {
        whenever(
            mockAptoPlatform.fetchCardProduct(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyBoolean(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, CardProduct>) -> Unit).invoke(Either.Right(mockCardProduct))
        }
    }
}
