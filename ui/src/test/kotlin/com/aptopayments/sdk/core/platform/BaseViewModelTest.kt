package com.aptopayments.sdk.core.platform

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.exception.Failure.NetworkConnection
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertTrue

@ExtendWith(InstantExecutorExtension::class)
class BaseViewModelTest {

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
