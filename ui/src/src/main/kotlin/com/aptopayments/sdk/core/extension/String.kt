package com.aptopayments.sdk.core.extension

fun String.starsExceptLast() = if (this.length <= 1) this else "*".repeat(this.length - 1).plus(this.last())
