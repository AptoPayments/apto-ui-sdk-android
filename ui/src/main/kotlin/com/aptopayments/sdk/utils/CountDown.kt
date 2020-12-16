package com.aptopayments.sdk.utils

import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.scheduleAtFixedRate

const val DELAY = 0L
const val PERIOD = 1000L

class CountDown {

    private var timer: Timer? = null

    fun start(seconds: Int, fireBlock: (Int) -> Unit, endBlock: () -> Unit) {
        val finishTime = LocalDateTime.now().plusSeconds(seconds.toLong())
        timer = Timer()
        timer?.scheduleAtFixedRate(delay = DELAY, period = PERIOD) {
            val diffInMs = LocalDateTime.now().until(finishTime, ChronoUnit.SECONDS)
            val diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs)
            if (diffInSec <= 0) {
                stop()
                endBlock.invoke()
            } else {
                fireBlock(diffInSec.toInt())
            }
        }
    }

    fun stop() {
        timer?.cancel()
    }
}
