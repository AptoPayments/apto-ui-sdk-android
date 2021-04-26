package com.aptopayments.sdk.core.platform

import androidx.annotation.RestrictTo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aptopayments.mobile.exception.Failure

/**
 * Base ViewModel class with default Failure handling.
 * @see ViewModel
 * @see Failure
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class BaseViewModel : ViewModel() {

    val failure: MutableLiveData<Failure> = MutableLiveData()

    private val _loading: MutableLiveData<Boolean> = MutableLiveData(false)
    val loading: LiveData<Boolean>
        get() = _loading

    fun handleFailure(failure: Failure) {
        this.failure.value = failure
        hideLoading()
    }

    fun showLoading() {
        _loading.value = true
    }

    fun hideLoading() {
        _loading.value = false
    }
}
