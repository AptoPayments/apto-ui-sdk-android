package com.aptopayments.sdk.core.platform

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.exception.Failure.NetworkConnection
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.test.assertTrue

class BaseViewModelTest {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @Test
    fun `should handle failure by updating live data`() {
        val viewModel = MyViewModel()

        viewModel.handleError(NetworkConnection)

        val error = viewModel.failure.getOrAwaitValue()

        assertTrue(error is NetworkConnection)
    }

    private class MyViewModel : BaseViewModel() {
        fun handleError(failure: Failure) = handleFailure(failure)
    }
}
