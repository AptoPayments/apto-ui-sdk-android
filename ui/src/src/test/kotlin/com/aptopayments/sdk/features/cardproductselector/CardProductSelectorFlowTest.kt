package com.aptopayments.sdk.features.cardproductselector

import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.cardproduct.CardProductSummary
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.geo.Country
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.core.platform.AptoPlatformProtocol
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.cardproductselector.countryselector.CountrySelectorFragmentDouble
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import com.aptopayments.sdk.features.selectcountry.CardProductSelectorFlow
import com.aptopayments.sdk.features.selectcountry.CountrySelectorContract
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Spy
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CardProductSelectorFlowTest: AndroidTest() {

    private lateinit var sut: CardProductSelectorFlow
    @Mock private lateinit var mockFragmentFactory: FragmentFactory
    @Mock private lateinit var mockDelegate: CountrySelectorContract.Delegate
    @Mock private lateinit var mockAptoPlatform: AptoPlatform
    @Spy
    private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()

    @Before
    override fun setUp() {
        super.setUp()
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
        startKoin {
            modules(module {
                single { mockFragmentFactory }
                single<AnalyticsServiceContract> { analyticsManager }
                single<AptoPlatformProtocol> { mockAptoPlatform }
            })
        }
        val mockCardProducts = ArrayList<CardProductSummary>()
        mockCardProducts.add(mock())
        mockCardProducts.add(mock())
        sut = CardProductSelectorFlow(onBack = {}, onFinish = {})
        Mockito.`when`(mockAptoPlatform.fetchCardProducts(TestDataProvider.anyObject())).thenAnswer { invocation ->
            (invocation.arguments[0] as (Either<Failure, List<CardProductSummary>>) -> Unit)
                    .invoke(Either.Right(mockCardProducts))
        }
    }

    @Test
    fun `should use the factory to instantiate CountrySelectorFragment as first fragment`() {
        // Given
        val tag = "CountrySelectorFragment"
        val fragmentDouble = CountrySelectorFragmentDouble(mockDelegate).apply { this.TAG = tag }

        given { mockFragmentFactory.countrySelectorFragment(
                uiTheme = TestDataProvider.provideDefaultTheme(),
                allowedCountries = emptyList(),
                tag = tag)
        }.willReturn(fragmentDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).countrySelectorFragment(
                uiTheme = TestDataProvider.provideDefaultTheme(),
                allowedCountries = emptyList(),
                tag = tag)
        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.CardProductSelectorCountrySelectorShown)
    }

    @Test
    fun `correct event tracked when going back from country selector`() {
        // When
        sut.onBackFromCountrySelector()

        // Then
        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.CardProductSelectorCountrySelectorClosed)
    }

    @Test
    fun `correct event tracked when country is selected`() {
        // Given
        val testIsoCode = "testIsoCode"
        val testCountry = Country(testIsoCode)
        val testCountryCardProductMap: HashMap<String, ArrayList<String>?> = HashMap()
        testCountryCardProductMap[testIsoCode] = arrayListOf("testCardProductId")
        sut.countryCardProductMap = testCountryCardProductMap

        // When
        sut.onCountrySelected(testCountry)

        // Then
        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.CardProductSelectorProductSelected)
    }
}
