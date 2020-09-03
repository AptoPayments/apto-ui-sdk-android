package com.aptopayments.sdk.utils

import android.os.Handler

internal interface Timer {
    fun setListener(listener: () -> Unit)
    fun start(periodMillis: Long)
    fun stop()
}

internal class RealTimer() : Timer {

    private var listener: (() -> Unit)? = null
    private val handler = Handler()

    override fun setListener(listener: () -> Unit) {
        this.listener = listener
    }

    override fun start(periodMillis: Long) {
        stop()
        handler.postDelayed({ listener?.invoke() }, periodMillis)
    }

    override fun stop() {
        handler.removeCallbacksAndMessages(null)
    }
}
