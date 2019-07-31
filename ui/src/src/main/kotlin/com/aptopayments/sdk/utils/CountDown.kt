package com.aptopayments.sdk.utils

import com.aptopayments.core.extension.add
import java.util.*
import java.util.concurrent.*
import kotlin.concurrent.scheduleAtFixedRate

const val DELAY = 0L
const val PERIOD = 1000L

class CountDown {

    private var timer: Timer? = null

    fun start(seconds: Int, fireBlock: (Int) -> Unit, endBlock: (Unit) -> Unit) {
        val finishTime = Date().add(Calendar.SECOND, seconds)
        timer = Timer()
        timer?.scheduleAtFixedRate(delay = DELAY, period = PERIOD) {
            val diffInMs = finishTime.time - Date().time
            val diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs)
            if (diffInSec <= 0) {
                stop()
                endBlock(Unit)
            }
            else {
                fireBlock(diffInSec.toInt())
            }
        }
    }

    fun stop() {
        timer?.cancel()
    }
}
