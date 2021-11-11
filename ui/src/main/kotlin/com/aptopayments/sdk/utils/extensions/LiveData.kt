package com.aptopayments.sdk.utils.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations

fun <X, Y> LiveData<X>.map(mapFunction: ((X) -> Y)) = Transformations.map(this, mapFunction)

fun <X> LiveData<X>.distinctUntilChanged() = Transformations.distinctUntilChanged(this)

fun <X> MutableLiveData<X>.asLiveData() = this as LiveData<X>
