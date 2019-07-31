package com.aptopayments.sdk

import android.app.Application
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.di.ApplicationComponent
import com.aptopayments.sdk.core.platform._applicationComponent
import com.nhaarman.mockito_kotlin.mock
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Base class for Unit tests. Inherit from it to create test cases which DO NOT contain android
 * framework dependencies or components.
 *
 * @see AndroidTest
 */
@RunWith(MockitoJUnitRunner::class)
abstract class UnitTest {

    @Suppress("LeakingThis")
    @Rule @JvmField val injectMocks = InjectMocksRule.create(this@UnitTest)

    @Before
    open fun setUp() {
        AptoPlatform.application = Application()
        val mockApplicationComponent = mock<ApplicationComponent>()
        _applicationComponent = mockApplicationComponent
    }
}
