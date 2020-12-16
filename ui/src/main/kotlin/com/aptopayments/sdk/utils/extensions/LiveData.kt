package com.aptopayments.sdk.utils.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations

fun <X, Y> LiveData<X>.map(mapFunction: ((X) -> Y)) = Transformations.map(this, mapFunction)

fun <X> LiveData<X>.distinctUntilChanged() = Transformations.distinctUntilChanged(this)
