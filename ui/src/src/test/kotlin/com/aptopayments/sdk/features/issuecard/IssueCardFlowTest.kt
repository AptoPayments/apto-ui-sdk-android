package com.aptopayments.sdk.features.issuecard

import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.config.UITheme
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

class IssueCardFlowTest : AndroidTest() {

    @Mock private lateinit var mockFragmentFactory: FragmentFactory
    @Mock private lateinit var mockIssueCardDelegate: IssueCardContract.Delegate
    @Mock private lateinit var mockIssueCardErrorDelegate: IssueCardErrorContract.Delegate

    @Before
    override fun setUp() {
        super.setUp()
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
    }

    @Test
    fun `should use the factory to instantiate IssueCardFragment as first fragment on init for THEME_2`() {
        // Given
        UIConfig.uiTheme = UITheme.THEME_2
        val tag = "IssueCardFragment"
        val fragmentDouble = IssueCardFragmentDouble(mockIssueCardDelegate).apply { this.TAG = tag }
        val cardApplicationId = TestDataProvider.provideCardApplicationId()
        val sut = IssueCardFlow(
                cardApplicationId = cardApplicationId,
                actionConfiguration = null,
                onBack = {},
                onFinish = {})
        given {
            mockFragmentFactory.issueCardFragment(
                uiTheme = UITheme.THEME_2,
                cardApplicationId = cardApplicationId,
                tag = tag)
        }.willReturn(fragmentDouble)

        // When
        sut.fragmentFactory = mockFragmentFactory
        sut.init {}

        // Then
        verify(mockFragmentFactory).issueCardFragment(
                uiTheme = UITheme.THEME_2,
                cardApplicationId = cardApplicationId,
                tag = tag)
    }

    @Test
    fun `should use the factory to instantiate IssueCardErrorFragment for THEME_2 on card issue failure`() {
        // Given
        UIConfig.uiTheme = UITheme.THEME_2
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
            mockFragmentFactory.issueCardFragment(
                    uiTheme = UITheme.THEME_2,
                    cardApplicationId = cardApplicationId,
                    tag = issueCardTag)
        }.willReturn(issueCardFragmentDouble)
        given {
            mockFragmentFactory.issueCardErrorFragment(
                    uiTheme = UITheme.THEME_2,
                    tag = issueCardErrorTag,
                    errorCode = 3,
                    errorAsset = null)
        }.willReturn(issueCardErrorFragmentDouble)

        // When
        sut.fragmentFactory = mockFragmentFactory
        sut.init {}
        issueCardFragmentDouble.delegate?.onCardIssuedFailed(3)

        // Then
        verify(mockFragmentFactory).issueCardErrorFragment(
                uiTheme = UITheme.THEME_2,
                tag = issueCardErrorTag,
                errorCode = 3,
                errorAsset = null)
    }
}
