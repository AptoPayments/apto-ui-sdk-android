package com.aptopayments.sdk.features.inputdata.id

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.geo.Country
import com.aptopayments.mobile.data.user.IdDataPointConfiguration
import com.aptopayments.mobile.data.user.IdDocumentDataPoint
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val COUNTRY_US = Country("US")
private val COUNTRY_UK = Country("UK")
private val COUNTRY_AR = Country("AR")
private val MULTI_DOCUMENT_TYPE_LIST = listOf(IdDocumentDataPoint.Type.SSN, IdDocumentDataPoint.Type.IDENTITY_CARD)
private val SINGLE_DOCUMENT_TYPE_LIST = listOf(IdDocumentDataPoint.Type.SSN)
private const val DOCUMENT_NUMBER = "1234567"
private const val VALID_SSN = "778628144"

@ExtendWith(InstantExecutorExtension::class)
class CollectUserIdViewModelTest {

    private val multiCountryMap =
        mapOf(
            COUNTRY_US to MULTI_DOCUMENT_TYPE_LIST,
            COUNTRY_AR to MULTI_DOCUMENT_TYPE_LIST,
            COUNTRY_UK to MULTI_DOCUMENT_TYPE_LIST
        )

    private val config: IdDataPointConfiguration = mock()
    private val analyticsManager: AnalyticsServiceContract = mock()
    private lateinit var sut: CollectUserIdViewModel

    @Test
    fun `on init tracks correct event`() {
        createSut()

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
    fun `given AR, document_type set when no number then continue is disabled`() {
        whenever(config.allowedDocumentTypes).thenReturn(multiCountryMap)

        createSut()
        sut.selectedCountry.value = COUNTRY_AR
        sut.typeList.getOrAwaitValue()
        sut.onIdTypeSelected(0)

        assertFalse(sut.continueEnabled.getOrAwaitValue())
    }

    @Test
    fun `given AR, document_type set when number entered then continue is enabled`() {
        whenever(config.allowedDocumentTypes).thenReturn(multiCountryMap)

        createSut()
        sut.selectedCountry.value = COUNTRY_AR
        sut.typeList.getOrAwaitValue()
        sut.onIdTypeSelected(0)
        sut.number.value = DOCUMENT_NUMBER

        assertTrue { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `given US, SSN selected when wrong number entered then continue is disabled`() {
        whenever(config.allowedDocumentTypes).thenReturn(multiCountryMap)

        createSut()
        sut.selectedCountry.value = COUNTRY_US
        sut.typeList.getOrAwaitValue()
        sut.onIdTypeSelected(0)
        sut.number.value = DOCUMENT_NUMBER

        assertFalse { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `given US, SSN selected when correct number entered then continue is enabled`() {
        whenever(config.allowedDocumentTypes).thenReturn(multiCountryMap)

        createSut()
        sut.selectedCountry.value = COUNTRY_US
        sut.typeList.getOrAwaitValue()
        sut.onIdTypeSelected(0)
        sut.number.value = VALID_SSN

        assertTrue { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `given US, ID_card selected when any number entered then continue is enabled`() {
        whenever(config.allowedDocumentTypes).thenReturn(multiCountryMap)

        createSut()
        sut.selectedCountry.value = COUNTRY_US
        sut.typeList.getOrAwaitValue()
        sut.onIdTypeSelected(1)
        sut.number.value = DOCUMENT_NUMBER

        assertTrue { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `given US, ID_card selected when empty number entered then continue is enabled`() {
        whenever(config.allowedDocumentTypes).thenReturn(multiCountryMap)

        createSut()
        sut.selectedCountry.value = COUNTRY_US
        sut.typeList.getOrAwaitValue()
        sut.onIdTypeSelected(1)
        sut.number.value = ""

        assertFalse { sut.continueEnabled.getOrAwaitValue() }
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
