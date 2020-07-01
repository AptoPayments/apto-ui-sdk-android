package com.aptopayments.sdk.utils.extensions

import android.annotation.SuppressLint
import java.util.Locale

@SuppressLint("DefaultLocale")
internal fun String.toCapitalized(): String =
    this.toLowerCase(Locale.getDefault()).split(' ').joinToString(" ") { it.capitalize() }
