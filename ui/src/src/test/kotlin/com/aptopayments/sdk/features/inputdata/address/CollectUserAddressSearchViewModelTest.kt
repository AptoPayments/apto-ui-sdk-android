package com.aptopayments.sdk.features.inputdata.address

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.user.AddressDataPoint
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.utils.MainCoroutineRule
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.android.libraries.places.api.model.Place
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val PLACE_ID = "PLACE1234"
private const val PLACE_ID_2 = "PLACE5678"
private const val OPTIONAL = "OPTIONAL VALUE"
private const val STREET_ONE = "ONE"
private const val STREET_TWO = "TWO"
private const val LOCALITY = "Bcn"
private const val REGION = "Cat"
private const val COUNTRY = "Spain"
private const val POSTAL_CODE = "08010"

@ExperimentalCoroutinesApi
internal class CollectUserAddressSearchViewModelTest : UnitTest() {
    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    lateinit var analyticsManager: AnalyticsManager

    @Mock
    lateinit var addressGenerator: AddressDataPointGenerator

    @Mock
    lateinit var placeFetcher: PlaceFetcher

    @Mock
    lateinit var place: Place

    @Mock
    lateinit var addressComponents: AddressComponents

    @Mock
    lateinit var datapoint: AddressDataPoint

    lateinit var sut: CollectUserAddressViewModel

    @Test
    fun `onViewLoaded tracks correct event`() {
        val sut = CollectUserAddressViewModel(null, analyticsManager, addressGenerator, placeFetcher)

        sut.viewLoaded()

        verify(analyticsManager).track(Event.WorkflowUserIdAddress)
    }

    @Test
    fun `when created continue is disabled`() {
        val sut = CollectUserAddressViewModel(null, analyticsManager, addressGenerator, placeFetcher)

        assertFalse(sut.continueEnabled.getOrAwaitValue())
    }

    @Test
    fun `when place selected and cant be fetched then continue button is not enabled`() = runBlockingTest {
        val sut = CollectUserAddressViewModel(null, analyticsManager, addressGenerator, placeFetcher)
        whenever(placeFetcher.fetchPlace(PLACE_ID)).thenReturn(null)

        sut.onAddressClicked(PLACE_ID)

        verify(placeFetcher).fetchPlace(PLACE_ID)
        assertFalse(sut.continueEnabled.getOrAwaitValue())
    }

    @Test
    fun `when place selected without components then continue button is not enabled`() = runBlockingTest {
        val sut = CollectUserAddressViewModel(null, analyticsManager, addressGenerator, placeFetcher)
        whenever(placeFetcher.fetchPlace(PLACE_ID)).thenReturn(place)
        whenever(place.addressComponents).thenReturn(null)

        sut.onAddressClicked(PLACE_ID)

        verify(placeFetcher).fetchPlace(PLACE_ID)
        verify(place).addressComponents
        assertFalse(sut.continueEnabled.getOrAwaitValue())
    }

    @Test
    fun `when place selected then continue button is not enabled`() = runBlockingTest {
        val sut = CollectUserAddressViewModel(null, analyticsManager, addressGenerator, placeFetcher)
        configureCorrectCase()

        sut.onAddressClicked(PLACE_ID)

        assertTrue { sut.continueEnabled.getOrAwaitValue() }
        assertFalse(sut.showPoweredByGoogle.getOrAwaitValue())
    }

    @Test
    fun `when started editing after selecting then continue button disabled`() = runBlockingTest {
        val sut = CollectUserAddressViewModel(null, analyticsManager, addressGenerator, placeFetcher)
        configureCorrectCase()

        sut.onAddressClicked(PLACE_ID)
        sut.onEditingAddress()

        assertTrue { sut.showPoweredByGoogle.getOrAwaitValue() }
        assertFalse(sut.continueEnabled.getOrAwaitValue())
    }

    @Test
    fun `when dismissed after selecting then continue button disabled`() = runBlockingTest {
        val sut = CollectUserAddressViewModel(null, analyticsManager, addressGenerator, placeFetcher)
        configureCorrectCase()

        sut.onAddressClicked(PLACE_ID)
        sut.onEditingAddress()
        sut.onAddressDismissed()

        assertFalse(sut.showPoweredByGoogle.getOrAwaitValue())
        assertFalse(sut.continueEnabled.getOrAwaitValue())
    }

    @Test
    fun `when address selected and continue clicked then continue event fired`() = runBlockingTest {
        val sut = CollectUserAddressViewModel(null, analyticsManager, addressGenerator, placeFetcher)
        configureCorrectCase()

        sut.onAddressClicked(PLACE_ID)
        sut.continueEnabled.getOrAwaitValue()
        sut.continueClicked()

        assertEquals(sut.continueClicked.getOrAwaitValue(), datapoint)
    }

    @Test
    fun `when address but place returned null then continue is not enabled`() = runBlockingTest {
        val sut = CollectUserAddressViewModel(null, analyticsManager, addressGenerator, placeFetcher)
        whenever(placeFetcher.fetchPlace(PLACE_ID)).thenReturn(null)

        sut.onAddressClicked(PLACE_ID)
        sut.continueEnabled.getOrAwaitValue()

        assertFalse(sut.continueEnabled.getOrAwaitValue())
    }

    @Test
    fun `when address was correct but second time place returned null then continue is not enabled`() =
        runBlockingTest {
            val sut = CollectUserAddressViewModel(null, analyticsManager, addressGenerator, placeFetcher)
            configureCorrectCase()
            whenever(placeFetcher.fetchPlace(PLACE_ID)).thenReturn(null)

            sut.onAddressClicked(PLACE_ID)
            sut.onAddressClicked(PLACE_ID_2)

            assertFalse(sut.continueEnabled.getOrAwaitValue())
        }

    @Test
    fun `when address set and continue clicked then generator called correctly`() = runBlockingTest {
        val sut = CollectUserAddressViewModel(null, analyticsManager, addressGenerator, placeFetcher)
        configureCorrectCase()

        sut.onAddressClicked(PLACE_ID)
        sut.continueClicked()

        verify(addressGenerator).generate(place.addressComponents!!, "")
    }

    @Test
    fun `when address and optional set and continue clicked then generator called correctly`() = runBlockingTest {
        val sut = CollectUserAddressViewModel(null, analyticsManager, addressGenerator, placeFetcher)
        configureCorrectCase()

        sut.onAddressClicked(PLACE_ID)
        sut.optionalText.value = OPTIONAL
        sut.continueClicked()

        verify(addressGenerator).generate(place.addressComponents!!, OPTIONAL)
    }

    @Test
    fun `when addressDataPoint is set in constructor then continue button is enabled`() = runBlockingTest {
        val initialValue = AddressDataPoint(STREET_ONE, STREET_TWO, LOCALITY, REGION, POSTAL_CODE, COUNTRY)
        val sut = CollectUserAddressViewModel(initialValue, analyticsManager, addressGenerator, placeFetcher)

        assertTrue { sut.continueEnabled.getOrAwaitValue() }
        assertEquals(STREET_TWO, sut.optionalText.getOrAwaitValue())
        assertEquals("$STREET_ONE, $LOCALITY, $COUNTRY", sut.searchText.getOrAwaitValue())
    }

    @Test
    fun `when addressDataPoint is set in constructor and continue clicked then that object is fired in event`() =
        runBlockingTest {
            val initialValue = AddressDataPoint(STREET_ONE, STREET_TWO, LOCALITY, REGION, POSTAL_CODE, COUNTRY)
            val sut = CollectUserAddressViewModel(initialValue, analyticsManager, addressGenerator, placeFetcher)

            sut.continueClicked()

            assertEquals(initialValue, sut.continueClicked.getOrAwaitValue())
        }

    private suspend fun configureCorrectCase() {
        whenever(placeFetcher.fetchPlace(PLACE_ID)).thenReturn(place)
        whenever(place.addressComponents).thenReturn(addressComponents)
        whenever(addressGenerator.generate(eq(addressComponents), any())).thenReturn(datapoint)
    }
}
