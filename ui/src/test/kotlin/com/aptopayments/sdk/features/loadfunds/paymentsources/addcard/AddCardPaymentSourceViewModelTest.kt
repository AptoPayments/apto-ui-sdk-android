package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard

import androidx.lifecycle.Observer
import com.aptopayments.mobile.data.card.*
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourcesRepository
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private const val CARD_ID = "card_id"
private const val MAX_AMOUNT = 1000.0
private const val VISA_TEST_CARD = "4242424242424242"
private const val CVV_VALUE = "123"
private const val EXPIRATION_VALUE = "1222"
private const val ZIP_VALUE = "12225"

@ExtendWith(InstantExecutorExtension::class)
internal class AddCardPaymentSourceViewModelTest {

    private val repo: PaymentSourcesRepository = mock()
    private val aptoPlatform: AptoPlatform = mock()

    private lateinit var sut: AddCardPaymentSourceViewModel

    private val fakeContinueObserver: Observer<Boolean> = mock()
    private val fakeNetworkObserver: Observer<CardNetwork> = mock()

    @BeforeEach
    fun setUp() {
        setFetchCard()

        sut = AddCardPaymentSourceViewModel(CARD_ID, repo, aptoPlatform)

        sut.creditCardNetwork.observeForever(fakeNetworkObserver)
        sut.continueButtonEnabled.observeForever(fakeContinueObserver)
    }

    @AfterEach
    fun tearDown() {
        sut.continueButtonEnabled.removeObserver(fakeContinueObserver)
        sut.creditCardNetwork.removeObserver(fakeNetworkObserver)
    }

    @Nested
    inner class CreditCardNumberTests {

        @Test
        fun `given cardNumber Is partially filled then all fields are NOT shown`() {
            sut.creditCardNumber.value = VISA_TEST_CARD.dropLast(1)

            assertEquals(false, sut.showAllFields.getOrAwaitValue())
        }

        @Test
        fun `given cardNumber Is set from different network then all fields are NOT shown`() {
            sut.creditCardNumber.value = TestDataProvider.provideAmexValidNumbers().first()

            assertEquals(false, sut.showAllFields.getOrAwaitValue())
        }

        @Nested
        inner class GivenCreditCardNumberFilled {

            @BeforeEach
            internal fun setUp() {
                sut.creditCardNumber.value = VISA_TEST_CARD
            }

            @Test
            fun `when continue is observed then continue button is not enabled`() {
                assertEquals(false, sut.continueButtonEnabled.getOrAwaitValue())
            }

            @Test
            fun `when creditCardNetwork is observed then carNetwork Is set`() {
                val network = sut.creditCardNetwork.getOrAwaitValue()

                assertEquals(CardNetwork.VISA, network)
            }

            @Test
            fun `when showAllFields is observed then all fields are shown`() {
                assertEquals(true, sut.showAllFields.getOrAwaitValue())
            }
        }
    }

    @Nested
    inner class GivenCcnExpirationAndZipFilled {

        @BeforeEach
        internal fun setUp() {
            sut.creditCardNumber.value = VISA_TEST_CARD
            sut.expiration.value = EXPIRATION_VALUE
            sut.zipCode.value = ZIP_VALUE
        }

        @Test
        fun `given cvv is empty when button is observed then value is false`() {
            assertEquals(false, sut.continueButtonEnabled.getOrAwaitValue())
        }

        @Test
        fun `given cvv is partially filled when button is observed then value is false`() {
            sut.cvv.value = CVV_VALUE.dropLast(1)

            assertEquals(false, sut.continueButtonEnabled.getOrAwaitValue())
        }

        @Test
        fun `given cvv is fully filled when button is observed then value is false`() {
            sut.cvv.value = CVV_VALUE

            assertEquals(true, sut.continueButtonEnabled.getOrAwaitValue())
        }
    }

    @Nested
    inner class GivenCcnCvvAndZipFilled {

        @BeforeEach
        internal fun setUp() {
            sut.creditCardNumber.value = VISA_TEST_CARD
            sut.cvv.value = CVV_VALUE
            sut.zipCode.value = ZIP_VALUE
        }

        @Test
        fun `given expiration is empty when button is observed then value is false`() {
            assertEquals(false, sut.continueButtonEnabled.getOrAwaitValue())
        }

        @Test
        fun `given expiration is partially filled when button is observed then value is false`() {
            sut.expiration.value = EXPIRATION_VALUE.dropLast(1)

            assertEquals(false, sut.continueButtonEnabled.getOrAwaitValue())
        }

        @Test
        fun `given expiration is before when button is observed then value is false`() {
            sut.expiration.value = "1119"

            assertEquals(false, sut.continueButtonEnabled.getOrAwaitValue())
        }

        @Test
        fun `given expiration is fully filled when button is observed then value is false`() {
            sut.expiration.value = EXPIRATION_VALUE

            assertEquals(true, sut.continueButtonEnabled.getOrAwaitValue())
        }
    }

    @Nested
    inner class GivenCcnCvvAndExpirationFilled {

        @BeforeEach
        internal fun setUp() {
            sut.creditCardNumber.value = VISA_TEST_CARD
            sut.cvv.value = CVV_VALUE
            sut.expiration.value = EXPIRATION_VALUE
        }

        @Test
        fun `given zip is empty when button is observed then value is false`() {
            assertEquals(false, sut.continueButtonEnabled.getOrAwaitValue())
        }

        @Test
        fun `given zip is partially filled when button is observed then value is false`() {
            sut.zipCode.value = ZIP_VALUE.dropLast(1)

            assertEquals(false, sut.continueButtonEnabled.getOrAwaitValue())
        }

        @Test
        fun `given zip is fully filled when button is observed then value is false`() {
            sut.zipCode.value = ZIP_VALUE

            assertEquals(true, sut.continueButtonEnabled.getOrAwaitValue())
        }
    }

    @Nested
    inner class CompletenessTests {
        @Test
        fun `whenever no data is loaded then continue button is NOT enabled`() {
            val continueEnabled = sut.continueButtonEnabled.getOrAwaitValue()

            assertEquals(false, continueEnabled)
        }

        @Test
        fun `whenever all data is completed then continue button is enabled`() {
            sut.creditCardNumber.value = VISA_TEST_CARD
            sut.cvv.value = CVV_VALUE
            sut.expiration.value = EXPIRATION_VALUE
            sut.zipCode.value = ZIP_VALUE

            val continueEnabled = sut.continueButtonEnabled.value

            assertEquals(true, continueEnabled)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setFetchCard() {
        val fundingFeature = FundingFeature(
            isEnabled = true,
            cardNetworks = listOf(Card.CardNetwork.VISA),
            limits = FundingLimits(FundingSingleLimit(TestDataProvider.provideMoney(amount = MAX_AMOUNT))),
            softDescriptor = ""
        )
        val features = Features(funding = fundingFeature)
        whenever(
            aptoPlatform.fetchCard(eq(CARD_ID), any(), TestDataProvider.anyObject())
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(
                TestDataProvider.provideCard(accountID = CARD_ID, features = features).right()
            )
        }
    }
}
