package com.aptopayments.sdk.features.inputdata.id

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.geo.Country
import com.aptopayments.core.data.user.IdDataPointConfiguration
import com.aptopayments.core.data.user.IdDocumentDataPoint
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val COUNTRY_US = Country("US")
private val COUNTRY_UK = Country("UK")
private val COUNTRY_AR = Country("AR")
private val MULTI_DOCUMENT_TYPE_LIST = listOf(IdDocumentDataPoint.Type.SSN, IdDocumentDataPoint.Type.IDENTITY_CARD)
private val SINGLE_DOCUMENT_TYPE_LIST = listOf(IdDocumentDataPoint.Type.SSN)
private val DOCUMENT_NUMBER = "1234567"

class CollectUserIdViewModelTest : UnitTest() {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private val multiCountryMap =
        mapOf(
            COUNTRY_US to MULTI_DOCUMENT_TYPE_LIST,
            COUNTRY_AR to MULTI_DOCUMENT_TYPE_LIST,
            COUNTRY_UK to MULTI_DOCUMENT_TYPE_LIST
        )

    @Mock
    private lateinit var config: IdDataPointConfiguration

    @Mock
    private lateinit var analyticsManager: AnalyticsServiceContract
    private lateinit var sut: CollectUserIdViewModel

    @Test
    fun `onViewLoaded tracks correct event`() {
        createSut()

        sut.viewLoaded()

        verify(analyticsManager).track(Event.WorkflowUserIdDocument)
    }

    @Test
    fun `when created continue is disabled`() {
        createSut()

        assertFalse(sut.continueEnabled.getOrAwaitValue())
    }

    @Test
    fun `when only one country then countryInput is invisible`() {
        val singleCountryMap = mapOf(COUNTRY_US to MULTI_DOCUMENT_TYPE_LIST)
        whenever(config.allowedDocumentTypes).thenReturn(singleCountryMap)

        createSut()

        assertFalse(sut.countryIsVisible)
    }

    @Test
    fun `when multiple country then countryInput is visible`() {
        whenever(config.allowedDocumentTypes).thenReturn(multiCountryMap)

        createSut()

        assertTrue { sut.countryIsVisible }
    }

    @Test
    fun `when single country and single document list then type is selected`() {
        val mapWithOneCountryAndOneType = mapOf(COUNTRY_US to SINGLE_DOCUMENT_TYPE_LIST)
        whenever(config.allowedDocumentTypes).thenReturn(mapWithOneCountryAndOneType)

        createSut()
        sut.typeList.getOrAwaitValue()

        assertEquals(sut.typePosition.getOrAwaitValue(), 0)
    }

    @Test
    fun `when country and document type selected but no number then continue is disabled`() {
        whenever(config.allowedDocumentTypes).thenReturn(multiCountryMap)

        createSut()
        sut.selectedCountry.value = COUNTRY_AR
        sut.typeList.getOrAwaitValue()
        sut.onIdTypeSelected(0)

        assertFalse(sut.continueEnabled.getOrAwaitValue())
    }

    @Test
    fun `when country, documentType selected and number entered then continue is enabled`() {
        whenever(config.allowedDocumentTypes).thenReturn(multiCountryMap)

        createSut()
        sut.selectedCountry.value = COUNTRY_AR
        sut.typeList.getOrAwaitValue()
        sut.onIdTypeSelected(0)
        sut.number.value = "1234"

        assertTrue { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `when have initialValue then data is set`() {
        whenever(config.allowedDocumentTypes).thenReturn(multiCountryMap)
        val initialValue =
            IdDocumentDataPoint(IdDocumentDataPoint.Type.IDENTITY_CARD, DOCUMENT_NUMBER, COUNTRY_AR.isoCode)
        createSut(initialValue)


        assertEquals(DOCUMENT_NUMBER, sut.number.getOrAwaitValue())
        assertEquals(COUNTRY_AR, sut.selectedCountry.getOrAwaitValue())
    }

    private fun createSut(initialValue: IdDocumentDataPoint? = null) {
        sut = CollectUserIdViewModel(initialValue, config, analyticsManager)
    }
}
