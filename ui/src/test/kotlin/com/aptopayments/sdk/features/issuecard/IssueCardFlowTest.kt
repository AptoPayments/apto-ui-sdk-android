package com.aptopayments.sdk.features.issuecard

import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.workflowaction.WorkflowActionConfigurationIssueCard
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

private const val CARD_ID = "12345"
private const val ERROR_ASSET = "fake_asset.png"
private const val FRAGMENT_TAG = "IssueCardFragment"

class IssueCardFlowTest : UnitTest() {

    private val configurationIssueCard = WorkflowActionConfigurationIssueCard(ERROR_ASSET)
    private val mockFragmentFactory: FragmentFactory = mock()

    @Before
    fun setUp() {
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
        startKoin {
            modules(
                module {
                    single { mockFragmentFactory }
                }
            )
        }
    }

    @Test
    fun `should use the factory to instantiate IssueCardFragment as first fragment on init`() {
        // Given
        val conf = WorkflowActionConfigurationIssueCard(ERROR_ASSET)
        val cardApplicationId = TestDataProvider.provideCardApplicationId()
        configureFragmentFactory(cardApplicationId)
        val sut = IssueCardFlow(
            cardApplicationId = cardApplicationId,
            actionConfiguration = conf,
            onBack = {},
            onFinish = {}
        )

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).issueCardFragment(cardApplicationId, configurationIssueCard, FRAGMENT_TAG)
    }

    @Test
    fun `when onCardIssuedSucceeded then onFinish is called with accountId`() {
        // Given
        val finishMock: TestCallback = mock()
        val card = TestDataProvider.provideCard(accountID = CARD_ID)
        val conf = WorkflowActionConfigurationIssueCard(ERROR_ASSET)
        val cardApplicationId = TestDataProvider.provideCardApplicationId()
        configureFragmentFactory(cardApplicationId)
        val sut = IssueCardFlow(
            cardApplicationId = cardApplicationId,
            actionConfiguration = conf,
            onBack = {},
            onFinish = finishMock::invoke
        )

        // When
        sut.init {}
        sut.onCardIssuedSucceeded(card)

        verify(finishMock).invoke(CARD_ID)
    }

    private fun configureFragmentFactory(cardApplicationId: String) {
        val fragmentDouble = mock<IssueCardFragment> { on { TAG } doReturn FRAGMENT_TAG }
        given {
            mockFragmentFactory.issueCardFragment(
                cardApplicationId = cardApplicationId,
                actionConfiguration = configurationIssueCard,
                tag = FRAGMENT_TAG
            )
        }.willReturn(fragmentDouble)
    }

    interface TestCallback {
        operator fun invoke(value: String)
    }
}
