package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.data.card.AchAccountFeature
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.Disclaimer
import com.aptopayments.mobile.data.card.Features
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.data.fundingsources.Balance
import com.aptopayments.mobile.data.fundingsources.AchAccountDetails
import com.aptopayments.mobile.data.user.agreements.AgreementAction
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.left
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.utils.MainCoroutineRule
import com.aptopayments.sdk.utils.runBlockingTest
import com.aptopayments.sdk.utils.shouldBeLeftAndInstanceOf
import com.aptopayments.sdk.utils.shouldBeRightAndEqualTo
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyList

private const val CARD_ID = "crd_12345"
private const val AGREEMENT_KEY = "key_1"
private const val BALANCE_ID = "bid_12345"

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
internal class AcceptAchDisclaimerUseCaseTest : UnitTest() {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val disclaimer = Disclaimer(keys = listOf(AGREEMENT_KEY), content = Content.PlainText(""))

    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val sut = AcceptAchDisclaimerUseCase(aptoPlatform)
    private val achAccountDetails: AchAccountDetails = mock()

    @Test
    fun `whenever everything works then AccountDetails`() = coroutineRule.runBlockingTest {
        configureCard(disclaimer)
        configureReviewAgreements(Unit.right())
        configureGetCardFundingSource()
        configureSetachAccountForBalance(achAccountDetails.right())

        val result = sut.invoke(AcceptAchDisclaimerUseCase.Params(cardId = CARD_ID))

        result.shouldBeRightAndEqualTo(achAccountDetails)
    }

    @Test
    fun `whenever account can not be created then Failure`() = coroutineRule.runBlockingTest {
        configureCard(disclaimer)
        configureReviewAgreements(Unit.right())
        configureGetCardFundingSource()
        configureSetachAccountForBalance(Failure.ServerError(1, "").left())

        val result = sut.invoke(AcceptAchDisclaimerUseCase.Params(cardId = CARD_ID))

        result.shouldBeLeftAndInstanceOf(Failure.ServerError::class.java)
    }

    @Test
    fun `whenever agreements can't be accepted then  Failure`() = coroutineRule.runBlockingTest {
        configureCard(disclaimer)
        configureReviewAgreements(Failure.ServerError(1, "").left())

        val result = sut.invoke(AcceptAchDisclaimerUseCase.Params(cardId = CARD_ID))

        result.shouldBeLeftAndInstanceOf(Failure.ServerError::class.java)
    }

    private fun configureCard(disclaimer: Disclaimer?) {
        val feature = AchAccountFeature(isEnabled = true, isAccountProvisioned = false, disclaimer = disclaimer)
        val card = TestDataProvider.provideCard(features = Features(achAccount = feature))
        whenever(
            aptoPlatform.fetchCard(
                eq(CARD_ID),
                anyBoolean(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(card.right())
        }
    }

    private fun configureReviewAgreements(result: Either<Failure, Unit>) {
        whenever(
            aptoPlatform.reviewAgreements(
                anyList(),
                eq(AgreementAction.ACCEPTED),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Unit>) -> Unit).invoke(result)
        }
    }

    private fun configureGetCardFundingSource() {
        whenever(
            aptoPlatform.fetchCardFundingSource(
                eq(CARD_ID),
                anyBoolean(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Balance>) -> Unit).invoke(
                TestDataProvider.provideBalance(id = BALANCE_ID).right()
            )
        }
    }

    private fun configureSetachAccountForBalance(result: Either<Failure, AchAccountDetails>) {
        whenever(
            aptoPlatform.assignAchAccount(
                eq(BALANCE_ID),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[1] as (Either<Failure, AchAccountDetails>) -> Unit).invoke(
                result
            )
        }
    }
}
