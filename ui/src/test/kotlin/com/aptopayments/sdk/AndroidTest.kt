package com.aptopayments.sdk

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Base class for Android tests. Inherit from it to create test cases which contain android
 * framework dependencies or components.
 *
 * @see UnitTest
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = AndroidTest.ApplicationStub::class, sdk = [28])
abstract class AndroidTest : AutoCloseKoinTest() {

    fun context(): Context = ApplicationProvider.getApplicationContext()

    internal class ApplicationStub : Application()
}
