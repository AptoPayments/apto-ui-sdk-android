package com.aptopayments.sdk.utils

class ManualTimer : Timer {
    private var listener: (() -> Unit)? = null

    override fun setListener(listener: () -> Unit) {
        this.listener = listener
    }

    override fun start(periodMillis: Long) {
    }

    override fun stop() {
    }

    public fun timeFinished() {
        listener?.invoke()
    }
}
