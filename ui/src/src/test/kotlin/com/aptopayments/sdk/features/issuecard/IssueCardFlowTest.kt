package com.aptopayments.sdk.features.issuecard

import com.aptopayments.core.data.config.Branding
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.mockito.Mock

class IssueCardFlowTest : AndroidTest() {

    @Mock private lateinit var mockFragmentFactory: FragmentFactory
    @Mock private lateinit var mockIssueCardDelegate: IssueCardContract.Delegate
    @Mock private lateinit var mockIssueCardErrorDelegate: IssueCardErrorContract.Delegate

    @Before
    override fun setUp() {
        super.setUp()
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
        startKoin {
            modules(module {
                single { mockFragmentFactory }
            })
        }
    }

    @Test
    fun `should use the factory to instantiate IssueCardFragment as first fragment on init`() {
        // Given
        UIConfig.updateUIConfigFrom(Branding.createDefault())
        val tag = "IssueCardFragment"
        val fragmentDouble = IssueCardFragmentDouble(mockIssueCardDelegate).apply { this.TAG = tag }
        val cardApplicationId = TestDataProvider.provideCardApplicationId()
        val sut = IssueCardFlow(
                cardApplicationId = cardApplicationId,
                actionConfiguration = null,
                onBack = {},
                onFinish = {})
        given {
            mockFragmentFactory.issueCardFragment(cardApplicationId, tag)
        }.willReturn(fragmentDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).issueCardFragment(cardApplicationId, tag)
    }

    @Test
    fun `should use the factory to instantiate IssueCardErrorFragment on card issue failure`() {
        // Given
        val issueCardTag = "IssueCardFragment"
        val issueCardErrorTag = "IssueCardErrorFragment"

        val issueCardFragmentDouble = IssueCardFragmentDouble(mockIssueCardDelegate).apply { this.TAG = issueCardTag }
        val issueCardErrorFragmentDouble = IssueCardErrorFragmentDouble(mockIssueCardErrorDelegate).apply { this.TAG = issueCardErrorTag }

        val cardApplicationId = TestDataProvider.provideCardApplicationId()
        val sut = IssueCardFlow(
                cardApplicationId = cardApplicationId,
                actionConfiguration = null,
                onBack = {},
                onFinish = {})
        given {
            mockFragmentFactory.issueCardFragment(cardApplicationId, issueCardTag)
        }.willReturn(issueCardFragmentDouble)
        given {
            mockFragmentFactory.issueCardErrorFragment(3, null, issueCardErrorTag)
        }.willReturn(issueCardErrorFragmentDouble)

        // When
        sut.init {}
        issueCardFragmentDouble.delegate?.onCardIssuedFailed(3)

        // Then
        verify(mockFragmentFactory).issueCardErrorFragment(3, null, issueCardErrorTag)
    }
}
