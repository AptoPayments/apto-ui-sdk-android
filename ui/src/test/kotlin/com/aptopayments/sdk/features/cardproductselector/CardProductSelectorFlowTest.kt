package com.aptopayments.sdk.features.cardproductselector

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.cardproduct.CardProductSummary
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.geo.Country
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.selectcountry.CardProductSelectorFlow
import com.aptopayments.sdk.features.selectcountry.CountrySelectorFragment
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

class CardProductSelectorFlowTest : UnitTest() {

    private lateinit var sut: CardProductSelectorFlow
    private val mockFragmentFactory: FragmentFactory = mock()
    private val mockAptoPlatform: AptoPlatform = mock()

    private var analyticsManager: AnalyticsServiceContract = mock()

    @Suppress("UNCHECKED_CAST")
    @BeforeEach
    fun setUp() {
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
        startKoin {
            modules(
                module {
                    single { mockFragmentFactory }
                    single { analyticsManager }
                    single<AptoPlatformProtocol> { mockAptoPlatform }
                }
            )
        }
        val mockCardProducts = ArrayList<CardProductSummary>()
        mockCardProducts.add(mock())
        mockCardProducts.add(mock())
        sut = CardProductSelectorFlow(onBack = {}, onFinish = {})
        whenever(mockAptoPlatform.fetchCardProducts(TestDataProvider.anyObject())).thenAnswer { invocation ->
            (invocation.arguments[0] as (Either<Failure, List<CardProductSummary>>) -> Unit)
                .invoke(Either.Right(mockCardProducts))
        }
    }

    @Test
    fun `should use the factory to instantiate CountrySelectorFragment as first fragment`() {
        // Given
        val tag = "CountrySelectorFragment"
        val fragmentDouble = mock<CountrySelectorFragment> { on { TAG } doReturn tag }

        given {
            mockFragmentFactory.countrySelectorFragment(emptyList(), tag)
        }.willReturn(fragmentDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).countrySelectorFragment(emptyList(), tag)
        verify(analyticsManager).track(Event.CardProductSelectorCountrySelectorShown)
    }

    @Test
    fun `correct event tracked when going back from country selector`() {
        // When
        sut.onBackFromCountrySelector()

        // Then
        verify(analyticsManager).track(Event.CardProductSelectorCountrySelectorClosed)
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
        verify(analyticsManager).track(eq(Event.CardProductSelectorProductSelected), anyOrNull())
    }
}
