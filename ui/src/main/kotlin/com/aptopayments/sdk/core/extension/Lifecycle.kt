package com.aptopayments.sdk.core.extension

import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <T : Any, L : LiveData<T>> LifecycleOwner.observe(liveData: L, body: (T?) -> Unit) =
    liveData.observe(this, Observer(body))

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <T : Any, L : LiveData<T>> LifecycleOwner.observeNotNullable(liveData: L, body: (T) -> Unit) =
    liveData.observe(this, Observer(body))

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <T : Any?, L : LiveData<T?>> LifecycleOwner.observeNullable(liveData: L, body: (T?) -> Unit) =
    liveData.observe(this, Observer(body))
