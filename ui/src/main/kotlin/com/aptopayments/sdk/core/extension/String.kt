package com.aptopayments.sdk.core.extension

internal fun String.starsExceptLast() = if (this.length <= 1) this else "*".repeat(this.length - 1).plus(this.last())

internal fun String.toOnlyDigits(): String {
    return this.filter { it.isDigit() }
}
