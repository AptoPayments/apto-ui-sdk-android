package com.aptopayments.sdk.ui.views

import android.content.Context
import android.graphics.Point
import android.view.*
import kotlin.math.min

class ZoomScrollListener(context: Context) : GestureDetector.SimpleOnGestureListener(), View.OnTouchListener,
    GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {
    private var view: View? = null
    private val gesture: GestureDetector = GestureDetector(context, this)
    private val gestureScale: ScaleGestureDetector = ScaleGestureDetector(context, this)
    private var scaleFactor = 1f
    private var inScale: Boolean = false

    fun resetScale() {
        view?.run {
            setScaleFactor(1f)
            translateToXY(0f, 0f)
        }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        this.view = view
        gesture.onTouchEvent(event)
        gestureScale.onTouchEvent(event)
        return true
    }

    override fun onScroll(event1: MotionEvent?, event2: MotionEvent?, x: Float, y: Float): Boolean {
        view?.let{
            var newX = it.x
            var newY = it.y
            if (!inScale) {
                newX -= x
                newY -= y
            }
            val wm = it.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val d = wm.defaultDisplay
            val p = Point()
            d.getSize(p)

            if (newX > (it.width * scaleFactor - p.x) / 2) {
                newX = (it.width * scaleFactor - p.x) / 2
            } else if (newX < -((it.width * scaleFactor - p.x) / 2)) {
                newX = -((it.width * scaleFactor - p.x) / 2)
            }

            if (newY > (it.height * scaleFactor - p.y) / 2) {
                newY = (it.height * scaleFactor - p.y) / 2
            } else if (newY < -((it.height * scaleFactor - p.y) / 2)) {
                newY = -((it.height * scaleFactor - p.y) / 2)
            }

            translateToXY(newX, newY)
        }

        return true
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        val newScale = calculateDobleTapScale()
        setScaleFactor(newScale)
        return true
    }

    private fun calculateDobleTapScale(): Float {
        return when (scaleFactor) {
            MIN_SCALE -> MED_SCALE
            MED_SCALE -> MAX_SCALE
            MAX_SCALE -> MIN_SCALE
            in MIN_SCALE..MED_SCALE -> MED_SCALE
            in MED_SCALE..MAX_SCALE -> MAX_SCALE
            in 0f..MIN_SCALE -> MIN_SCALE
            else -> MIN_SCALE
        }
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (detector.scaleFactor > 1 && scaleFactor == MAX_SCALE) {
            return true
        }

        val newScaleFactor = calculateNewScaleFactor(detector)
        setScaleFactor(newScaleFactor)
        return true
    }

    private fun calculateNewScaleFactor(detector: ScaleGestureDetector): Float {
        var factor = scaleFactor * detector.scaleFactor
        factor = correctSmallScaleFactor(factor)
        factor = min(factor, MAX_SCALE)
        return factor
    }

    private fun correctSmallScaleFactor(scaleFactor: Float) = if (scaleFactor < 1) 1f else scaleFactor

    private fun setScaleFactor(scaleFactor: Float) {
        this.scaleFactor = scaleFactor
        view!!.scaleX = scaleFactor
        view!!.scaleY = scaleFactor
        falseScroll()
    }

    private fun translateToXY(newX: Float, newY: Float) {
        view!!.x = newX
        view!!.y = newY
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        inScale = true
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        inScale = false
        falseScroll()
    }

    private fun falseScroll() {
        onScroll(null, null, 0f, 0f)
    }

    companion object {
        private const val MIN_SCALE = 1f
        private const val MED_SCALE = 1.5f
        private const val MAX_SCALE = 2f
    }
}
