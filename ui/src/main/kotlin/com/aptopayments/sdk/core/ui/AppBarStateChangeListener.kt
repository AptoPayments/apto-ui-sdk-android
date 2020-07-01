package com.aptopayments.sdk.core.ui

import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

abstract class AppBarStateChangeListener : AppBarLayout.OnOffsetChangedListener {

    private var mCurrentState = State.TRANSITION

    private enum class State {
        EXPANDED,
        COLLAPSED,
        TRANSITION
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, offset: Int) {
        val scrolledPercentage = abs(offset) / appBarLayout.totalScrollRange.toFloat()
        mCurrentState = when (scrolledPercentage) {
            0.0f -> {
                if (mCurrentState != State.EXPANDED) onStateChanged(scrolledPercentage)
                State.EXPANDED
            }
            1.0f -> {
                if (mCurrentState != State.COLLAPSED) onStateChanged(scrolledPercentage)
                State.COLLAPSED
            }
            else -> {
                onStateChanged(scrolledPercentage)
                State.TRANSITION
            }
        }
    }

    abstract fun onStateChanged(offsetPercent: Float)
}
