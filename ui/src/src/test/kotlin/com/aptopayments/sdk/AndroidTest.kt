package com.aptopayments.sdk

import android.app.Application
import android.content.Context
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.di.ApplicationComponent
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform._applicationComponent
import com.nhaarman.mockito_kotlin.mock
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Base class for Android tests. Inherit from it to create test cases which contain android
 * framework dependencies or components.
 *
 * @see UnitTest
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class,
        application = AndroidTest.ApplicationStub::class,
        sdk = [21])
abstract class AndroidTest {

    @Suppress("LeakingThis")
    @Rule @JvmField val injectMocks = InjectMocksRule.create(this@AndroidTest)

    fun context(): Context = RuntimeEnvironment.application

    fun activityContext(): Context = mock(BaseActivity::class.java)

    internal class ApplicationStub : Application()

    @Before
    open fun setUp() {
        AptoPlatform.application = Application()
        val mockApplicationComponent = mock<ApplicationComponent>()
        _applicationComponent = mockApplicationComponent
    }
}
