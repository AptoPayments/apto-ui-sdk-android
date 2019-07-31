package com.aptopayments.sdk.core.ui

import com.google.android.material.appbar.AppBarLayout

abstract class AppBarStateChangeListener : AppBarLayout.OnOffsetChangedListener {

    private var mCurrentState = State.TRANSITION

    private enum class State {
        EXPANDED,
        COLLAPSED,
        TRANSITION
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, offset: Int) {
        val scrolledPercentage = Math.abs(offset)/appBarLayout.totalScrollRange.toFloat()
        when (scrolledPercentage) {
            0.0f -> {
                if (mCurrentState != State.EXPANDED) onStateChanged(scrolledPercentage)
                mCurrentState = State.EXPANDED
            }
            1.0f -> {
                if (mCurrentState != State.COLLAPSED) onStateChanged(scrolledPercentage)
                mCurrentState = State.COLLAPSED
            }
            else -> {
                onStateChanged(scrolledPercentage)
                mCurrentState = State.TRANSITION
            }
        }
    }

    abstract fun onStateChanged(offsetPercent: Float)
}
