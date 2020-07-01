package com.aptopayments.sdk.core.platform

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aptopayments.mobile.exception.Failure
import java.lang.reflect.Modifier

/**
 * Base ViewModel class with default Failure handling.
 * @see ViewModel
 * @see Failure
 */
abstract class BaseViewModel : ViewModel() {

    val failure: MutableLiveData<Failure> = MutableLiveData()

    private val _loading: MutableLiveData<Boolean> = MutableLiveData(false)
    val loading: LiveData<Boolean>
        get() = _loading

    fun handleFailure(failure: Failure) {
        this.failure.value = failure
        hideLoading()
    }

    @VisibleForTesting(otherwise = Modifier.PROTECTED)
    fun showLoading() {
        _loading.value = true
    }

    @VisibleForTesting(otherwise = Modifier.PROTECTED)
    fun hideLoading() {
        _loading.value = false
    }
}
