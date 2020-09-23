package com.aptopayments.sdk.core.platform

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.exception.Failure.NetworkConnection
import com.aptopayments.sdk.AndroidTest
import org.junit.Test
import kotlin.test.assertTrue

class BaseViewModelTest : AndroidTest() {

    @Test
    fun `should handle failure by updating live data`() {
        val viewModel = MyViewModel()

        viewModel.handleError(NetworkConnection)

        val error = viewModel.failure.value

        assertTrue(error is NetworkConnection)
    }

    private class MyViewModel : BaseViewModel() {
        fun handleError(failure: Failure) = handleFailure(failure)
    }
}
