package com.aptopayments.sdk.features.inputdata.name

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.core.data.user.NameDataPoint
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.analytics.AnalyticsManager
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val NAME = "Jhon"
private const val SURNAME = "Doe"

internal class CollectNameViewModelTest : UnitTest() {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @Mock
    lateinit var analyticsManager: AnalyticsManager

    @Test
    fun `when nothing is set then continue button is disabled`() {
        val sut = CollectUserNameViewModel(null, analyticsManager)

        assertFalse { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `when name has 1 character then no error is shown`() {
        val sut = CollectUserNameViewModel(null, analyticsManager)
        sut.name.postValue("a")

        assertFalse(sut.nameError.getOrAwaitValue())
        assertFalse { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `when name is right and after that no value then error is shown`() {
        val sut = CollectUserNameViewModel(null, analyticsManager)
        sut.name.postValue("a")
        sut.name.postValue("")

        assertTrue(sut.nameError.getOrAwaitValue())
        assertFalse { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `when name is right, wrong and right then then error is shown`() {
        val sut = CollectUserNameViewModel(null, analyticsManager)
        sut.name.postValue("a")
        sut.name.postValue("")
        sut.name.postValue("a")

        assertFalse(sut.nameError.getOrAwaitValue())
        assertFalse { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `when surname has 1 character then no error is shown`() {
        val sut = CollectUserNameViewModel(null, analyticsManager)
        sut.surname.postValue("a")

        assertFalse(sut.surnameError.getOrAwaitValue())
        assertFalse { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `when surname is right and after that no value then error is shown`() {
        val sut = CollectUserNameViewModel(null, analyticsManager)
        sut.surname.postValue("a")
        sut.surname.postValue("")

        assertTrue(sut.surnameError.getOrAwaitValue())
        assertFalse { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `when surname is right, wrong and right then then error is shown`() {
        val sut = CollectUserNameViewModel(null, analyticsManager)
        sut.surname.postValue("a")
        sut.surname.postValue("")
        sut.surname.postValue("a")

        assertFalse(sut.surnameError.getOrAwaitValue())
        assertFalse { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `when name and surname are correct then error is shown`() {
        val sut = CollectUserNameViewModel(null, analyticsManager)
        sut.name.postValue(NAME)
        sut.surname.postValue(SURNAME)

        assertFalse(sut.nameError.getOrAwaitValue())
        assertFalse(sut.surnameError.getOrAwaitValue())
        assertTrue { sut.continueEnabled.getOrAwaitValue() }
    }

    @Test
    fun `when have initialValue correct then continueButton is enabled`() {
        val initialValue = NameDataPoint(NAME, SURNAME)
        val sut = CollectUserNameViewModel(initialValue, analyticsManager)


        assertTrue { sut.continueEnabled.getOrAwaitValue() }
        assertEquals(NAME, sut.name.getOrAwaitValue())
        assertEquals(SURNAME, sut.surname.getOrAwaitValue())
    }

}
