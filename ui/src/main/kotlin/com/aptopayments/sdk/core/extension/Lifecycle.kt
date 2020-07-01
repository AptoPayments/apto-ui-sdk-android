package com.aptopayments.sdk.core.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.aptopayments.mobile.exception.Failure

fun <T : Any, L : LiveData<T>> LifecycleOwner.observe(liveData: L, body: (T?) -> Unit) =
        liveData.observe(this, Observer(body))

fun <T : Any, L : LiveData<T>> LifecycleOwner.observeNotNullable(liveData: L, body: (T) -> Unit) =
    liveData.observe(this, Observer(body))

fun <T : Any?, L : LiveData<T?>> LifecycleOwner.observeNullable(liveData: L, body: (T?) -> Unit) =
        liveData.observe(this, Observer(body))

fun <T1 : Any?, L1 : LiveData<T1?>, T2 : Any?, L2 : LiveData<T2?>>
        LifecycleOwner.observeTwo(liveData1: L1, liveData2: L2, body: (T1?, T2?) -> Unit) {
    liveData1.observe(this, Observer {
        val value2 = liveData2.value
        body(it, value2)
    })
    liveData2.observe(this, Observer {
        val value1 = liveData1.value
        body(value1, it)
    })
}

fun <T1 : Any?, L1 : LiveData<T1?>, T2 : Any?, L2 : LiveData<T2?>, T3 : Any?, L3 : LiveData<T3?>>
        LifecycleOwner.observeThree(liveData1: L1, liveData2: L2, liveData3: L3, body: (T1?, T2?, T3?) -> Unit) {
    liveData1.observe(this, Observer {
        body(it, liveData2.value, liveData3.value)
    })
    liveData2.observe(this, Observer {
        body(liveData1.value, it, liveData3.value)
    })
    liveData3.observe(this, Observer {
        body(liveData1.value, liveData2.value, it)
    })
}

fun <L : LiveData<Failure>> LifecycleOwner.failure(liveData: L, body: (Failure?) -> Unit) =
        liveData.observe(this, Observer(body))
