package com.aptopayments.sdk.features.issuecard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.workflowaction.WorkflowActionConfigurationIssueCard
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.repository.IssueCardAdditionalFieldsRepository
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val CARD_APPLICATION_ID = "CRD_12345"
private const val ERROR_ASSET = "fake_error_asset.png"
private const val ERROR_INSUFFICIENT_FUNDS = 90196
private const val ERROR_BALANCE_VALIDATIONS_EMAIL_SENDS_DISABLED = 200040
private const val ERROR_BALANCE_VALIDATIONS_INSUFFICIENT_APPLICATION_LIMIT = 200041
private const val ERROR_OTHER = 1234

class IssueCardViewModelTest : AndroidTest() {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var sut: IssueCardViewModel

    @Mock
    private lateinit var analyticsManager: AnalyticsManager

    @Mock
    private lateinit var aptoPlatform: AptoPlatformProtocol

    @Mock
    private lateinit var issueCardAdditionalRepo: IssueCardAdditionalFieldsRepository

    private val additionalFields = mapOf("test" to "test1")

    @Before
    override fun setUp() {
        super.setUp()
        whenever(issueCardAdditionalRepo.get()).thenReturn(additionalFields)
    }

    @Test
    fun `when IssueCardViewModel then issueCard call is made correctly`() {
        val card = TestDataProvider.provideCard()
        val captor = argumentCaptor<String>()
        whenever(
            aptoPlatform.issueCard(
                captor.capture(),
                TestDataProvider.anyObject(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(Either.Right(card))
        }

        createSut()

        verify(aptoPlatform).issueCard(any(), any(), any())
        verify(analyticsManager).track(Event.IssueCard)
        assertEquals(CARD_APPLICATION_ID, captor.firstValue)
        assertFalse(sut.errorVisible.getOrAwaitValue())
        assertEquals(card, sut.card.getOrAwaitValue())
    }

    @Test
    fun `when additional Field set them it is sent to the core sdk`() {
        val card = TestDataProvider.provideCard()
        val captor = argumentCaptor<Map<String, String>>()
        whenever(
            aptoPlatform.issueCard(
                any(),
                captor.capture(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(Either.Right(card))
        }

        createSut()

        assertEquals(additionalFields, captor.firstValue)
    }

    @Test
    fun `when issueCard fails insufficient funds then correct errors shown`() {
        configureIssueCardApi(Either.Left(Failure.ServerError(ERROR_INSUFFICIENT_FUNDS)))

        createSut()

        verify(analyticsManager).track(eq(Event.IssueCardInsufficientFunds), any())
        val title = "issue_card_issue_card_error_insufficient_funds_title"
        val description = "issue_card_issue_card_error_insufficient_funds_description"
        val primaryCta = "issue_card_issue_card_error_insufficient_funds_primary_cta"
        val secondaryCta = "issue_card_issue_card_error_insufficient_funds_secondary_cta"
        checkErrors(title, description, primaryCta, secondaryCta)
    }

    @Test
    fun `when issueCard fails then IssueCardViewModel then issueCard call is made correctly`() {
        configureIssueCardApi(Either.Left(Failure.ServerError(ERROR_BALANCE_VALIDATIONS_EMAIL_SENDS_DISABLED)))

        createSut()

        verify(analyticsManager).track(eq(Event.IssueCardEmailSendsDisabled), any())
        val title = "issue_card_issue_card_error_email_sends_disabled_title"
        val description = "issue_card_issue_card_error_email_sends_disabled_description"
        val primaryCta = "issue_card_issue_card_error_email_sends_disabled_primary_cta"
        val secondaryCta = "issue_card_issue_card_error_email_sends_disabled_secondary_cta"
        checkErrors(title, description, primaryCta, secondaryCta)
    }

    @Test
    fun `when issueCard fails for insufficient application limit then correct errors shown`() {
        configureIssueCardApi(Either.Left(Failure.ServerError(ERROR_BALANCE_VALIDATIONS_INSUFFICIENT_APPLICATION_LIMIT)))

        createSut()

        verify(analyticsManager).track(eq(Event.IssueCardInsufficientApplicationLimit), any())
        val title = "issue_card_issue_card_error_insufficient_application_limit_title"
        val description = "issue_card_issue_card_error_insufficient_application_limit_description"
        val primaryCta = "issue_card_issue_card_error_insufficient_application_limit_primary_cta"
        val secondaryCta = "issue_card_issue_card_error_insufficient_application_limit_secondary_cta"
        checkErrors(title, description, primaryCta, secondaryCta)
    }

    @Test
    fun `when issueCard fails for unknown error then correct errors shown`() {
        configureIssueCardApi(Either.Left(Failure.ServerError(ERROR_OTHER)))

        createSut()

        verify(analyticsManager).track(eq(Event.IssueCardUnknownError), any())
        val title = "issue_card_issue_card_generic_error_title"
        val description = "issue_card_issue_card_generic_error_description"
        val primaryCta = "issue_card_issue_card_generic_error_primary_cta"
        val secondaryCta = "issue_card_issue_card_generic_error_secondary_cta"
        checkErrors(title, description, primaryCta, secondaryCta)
    }

    @Test
    fun `when error and retry erroneous then error shown`() {
        configureIssueCardApi(Either.Left(Failure.ServerError(ERROR_OTHER)))

        createSut()
        sut.retryIssueCard()

        assertTrue { sut.errorVisible.getOrAwaitValue() }
        verify(aptoPlatform, times(2)).issueCard(eq(CARD_APPLICATION_ID), any(), any())
    }

    @Test
    fun `when error and retry correct then card provided`() {
        configureIssueCardApi(Either.Left(Failure.ServerError(ERROR_OTHER)))

        createSut()

        configureIssueCardApi(Either.Right(TestDataProvider.provideCard()))

        sut.retryIssueCard()

        assertEquals(TestDataProvider.provideCard(), sut.card.getOrAwaitValue())
        assertFalse(sut.errorVisible.getOrAwaitValue())
        verify(aptoPlatform, times(2)).issueCard(eq(CARD_APPLICATION_ID), any(), any())
    }

    private fun createSut() {
        sut = IssueCardViewModel(
            CARD_APPLICATION_ID,
            WorkflowActionConfigurationIssueCard(ERROR_ASSET),
            analyticsManager,
            aptoPlatform,
            issueCardAdditionalRepo
        )
    }

    private fun checkErrors(
        title: String,
        description: String,
        primaryCta: String,
        secondaryCta: String
    ) {
        assertEquals(title, sut.title.getOrAwaitValue())
        assertEquals(description, sut.description.getOrAwaitValue())
        assertEquals(primaryCta, sut.primaryCta.getOrAwaitValue())
        assertEquals(secondaryCta, sut.secondaryCta.getOrAwaitValue())
        assertTrue { sut.errorVisible.getOrAwaitValue() }
    }

    private fun configureIssueCardApi(answer: Either<Failure, Card>) {
        whenever(
            aptoPlatform.issueCard(any(), any(), TestDataProvider.anyObject())
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(answer)
        }
    }
}
