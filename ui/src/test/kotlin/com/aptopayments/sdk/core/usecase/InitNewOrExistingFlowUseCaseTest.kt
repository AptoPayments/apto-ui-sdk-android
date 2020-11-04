package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.data.PaginatedList
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.left
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.usecase.InitNewOrExistingFlowUseCase.Action
import com.aptopayments.sdk.repository.ForceIssueCardRepository
import com.aptopayments.sdk.repository.ManageCardIdRepository
import com.aptopayments.sdk.utils.MainCoroutineRule
import com.aptopayments.sdk.utils.shouldBeLeftAndInstanceOf
import com.aptopayments.sdk.utils.shouldBeRightAndInstanceOf
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

private const val CARD_ID = "id_12345"

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
internal class InitNewOrExistingFlowUseCaseTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val manageCardIdRepository: ManageCardIdRepository = mock()
    private val forceIssueCardRepository: ForceIssueCardRepository = mock()

    private val sut = InitNewOrExistingFlowUseCase(aptoPlatform, manageCardIdRepository, forceIssueCardRepository)

    @Test
    fun `when has cardId saved then API is called with correct cardId`() = runBlockingTest {
        configureSavedCardWillReturn(CARD_ID)
        configureFetchCard(TestDataProvider.provideCard(accountID = CARD_ID).right())

        sut.invoke()

        verify(aptoPlatform).fetchCard(eq(CARD_ID), any(), any())
        verifyNoMoreInteractions(aptoPlatform)
    }

    @Test
    fun `when should start with cardId and card exist then correct Action returned`() = runBlockingTest {
        configureSavedCardWillReturn(CARD_ID)
        configureFetchCard(TestDataProvider.provideCard(accountID = CARD_ID).right())

        val result = sut.invoke()

        result.shouldBeRightAndInstanceOf(Action.ContinueFlowWithCard::class.java)
        assertEquals(CARD_ID, (result as Either.Right<Action.ContinueFlowWithCard>).b.cardId)
    }

    @Test
    fun `when not should force apply to card then correct Action returned`() = runBlockingTest {
        configureSavedCardWillReturn(null)
        configureForceApplyToCard(true)

        val result = sut.invoke()

        result.shouldBeRightAndInstanceOf(Action.ContinueWithCardProductSelectorFlow::class.java)
        verify(forceIssueCardRepository).clear()
    }

    @Test
    fun `when should start with cardId & don't force & card don't exist then Left`() = runBlockingTest {
        configureSavedCardWillReturn(CARD_ID)
        configureFetchCard(Failure.ServerError(null).left())

        val result = sut.invoke()

        result.shouldBeLeftAndInstanceOf(Failure.ServerError::class.java)
    }

    @Test
    fun `when doesn't have cardId saved & don't force then card list is fetched`() = runBlockingTest {
        configureSavedCardWillReturn(null)
        configureForceApplyToCard(false)
        configureFetchCardList(Failure.ServerError(null).left())

        sut.invoke()

        verify(aptoPlatform).fetchCards(anyOrNull(), any())
        verifyNoMoreInteractions(aptoPlatform)
    }

    @Test
    fun `when doesn't have cardId saved & don't force & failure in API then Failure`() = runBlockingTest {
        configureSavedCardWillReturn(null)
        configureForceApplyToCard(false)
        configureFetchCardList(Failure.ServerError(null).left())

        val result = sut.invoke()

        result.shouldBeLeftAndInstanceOf(Failure.ServerError::class.java)
    }

    @Test
    fun `when doesn't have cardId saved & don't force & empty card list then Failure`() = runBlockingTest {
        configureSavedCardWillReturn(null)
        configureForceApplyToCard(false)
        configureFetchCardList(PaginatedList(data = emptyList<Card>()).right())

        val result = sut.invoke()

        result.shouldBeRightAndInstanceOf(Action.ContinueWithCardProductSelectorFlow::class.java)
    }

    @Test
    fun `when doesn't have cardId saved & don't force & all cancelled cards then Failure`() = runBlockingTest {
        configureSavedCardWillReturn(null)
        configureForceApplyToCard(false)
        configureFetchCardList(
            PaginatedList(
                data = listOf(
                    TestDataProvider.provideCard(
                        accountID = CARD_ID,
                        state = Card.CardState.CANCELLED
                    )
                )
            ).right()
        )

        val result = sut.invoke()

        result.shouldBeRightAndInstanceOf(Action.ContinueWithCardProductSelectorFlow::class.java)
    }

    @Test
    fun `when doesn't have cardId saved & don't force & one uncancelled card then ContinueFlowWithCard`() =
        runBlockingTest {
            configureSavedCardWillReturn(null)
            configureForceApplyToCard(false)
            configureFetchCardList(
                PaginatedList(
                    data = listOf(
                        TestDataProvider.provideCard(
                            accountID = "id_12345",
                            state = Card.CardState.CANCELLED
                        ),
                        TestDataProvider.provideCard(
                            accountID = CARD_ID,
                            state = Card.CardState.ACTIVE
                        )
                    )
                ).right()
            )

            val result = sut.invoke()

            result.shouldBeRightAndInstanceOf(Action.ContinueFlowWithCard::class.java)
            assertEquals(CARD_ID, (result as Either.Right<Action.ContinueFlowWithCard>).b.cardId)
        }

    private fun configureSavedCardWillReturn(cardId: String?) {
        whenever(manageCardIdRepository.data).thenReturn(cardId)
    }

    private fun configureForceApplyToCard(apply: Boolean) {
        whenever(forceIssueCardRepository.data).thenReturn(apply)
    }

    private fun configureFetchCard(value: Either<Failure, Card>) {
        whenever(
            aptoPlatform.fetchCard(eq(CARD_ID), any(), TestDataProvider.anyObject())
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(
                value
            )
        }
    }

    private fun configureFetchCardList(value: Either<Failure, PaginatedList<Card>>) {
        whenever(
            aptoPlatform.fetchCards(anyOrNull(), TestDataProvider.anyObject())
        ).thenAnswer { invocation ->
            (invocation.arguments[1] as (Either<Failure, PaginatedList<Card>>) -> Unit).invoke(
                value
            )
        }
    }
}
