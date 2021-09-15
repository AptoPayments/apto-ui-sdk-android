package com.aptopayments.sdk.core.usecase

import com.aptopayments.mobile.data.card.AchAccountFeature
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.Disclaimer
import com.aptopayments.mobile.data.card.Features
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.data.user.agreements.AgreementAction
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.left
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.usecase.DeclineAchDisclaimerUseCase.Params
import com.aptopayments.sdk.utils.shouldBeLeftAndInstanceOf
import com.aptopayments.sdk.utils.shouldBeRightAndEqualTo
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers

private const val CARD_ID = "crd_12345"
private const val AGREEMENT_KEY = "key_1"

@Suppress("UNCHECKED_CAST")
@ExperimentalCoroutinesApi
internal class DeclineAchDisclaimerUseCaseTest : UnitTest() {

    private val disclaimer = Disclaimer(keys = listOf(AGREEMENT_KEY), content = Content.PlainText(""))

    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val sut = DeclineAchDisclaimerUseCase(aptoPlatform)

    @Test
    fun `given card and agreement succeeds when invoked then Unit returned`() = runBlockingTest {
        configureCard(disclaimer)
        configureDeclineAgreements(Unit.right())

        val result = sut.invoke(Params(cardId = CARD_ID))

        result.shouldBeRightAndEqualTo(Unit)
    }

    @Test
    fun `given agreement fails when invoked then Failure returned`() = runBlockingTest {
        configureCard(disclaimer)
        configureDeclineAgreements(Failure.ServerError(1, "").left())

        val result = sut.invoke(Params(cardId = CARD_ID))

        result.shouldBeLeftAndInstanceOf(Failure.ServerError::class.java)
    }

    private fun configureCard(disclaimer: Disclaimer?) {
        val feature = AchAccountFeature(isEnabled = true, isAccountProvisioned = false, disclaimer = disclaimer)
        val card = TestDataProvider.provideCard(features = Features(achAccount = feature))
        whenever(
            aptoPlatform.fetchCard(
                eq(CARD_ID),
                ArgumentMatchers.anyBoolean(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(card.right())
        }
    }

    private fun configureDeclineAgreements(result: Either<Failure, Unit>) {
        whenever(
            aptoPlatform.reviewAgreements(
                ArgumentMatchers.anyList(),
                eq(AgreementAction.DECLINED),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Unit>) -> Unit).invoke(result)
        }
    }
}
