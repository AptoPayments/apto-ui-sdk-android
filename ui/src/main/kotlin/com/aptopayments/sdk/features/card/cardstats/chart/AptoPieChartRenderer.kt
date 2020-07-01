package com.aptopayments.sdk.features.card.cardstats.chart

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Path
import android.graphics.RectF
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet
import com.github.mikephil.charting.renderer.PieChartRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

class AptoPieChartRenderer(
    chart: PieChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler,
    private val borderColor: Int
) : PieChartRenderer(chart, animator, viewPortHandler) {

    private var angle = 0f
    private var userInnerRadius = 0f
    private var visibleAngleCount = 0
    private var radius = 0f
    private lateinit var center: MPPointF

    private val drawInnerArc: Boolean
        get() = mChart.isDrawHoleEnabled && !mChart.isDrawSlicesUnderHoleEnabled

    // Based on https://stackoverflow.com/a/38775142
    override fun drawDataSet(c: Canvas?, dataSet: IPieDataSet?) {
        val mPathBuffer = Path()
        val mInnerRectBuffer = RectF()
        val entryCount = dataSet?.entryCount ?: 0
        center = mChart.centerCircleBox
        radius = mChart.radius
        if (drawInnerArc) userInnerRadius = radius * (mChart.holeRadius / 100f)
        visibleAngleCount = getVisibleAngleCount(entryCount, dataSet)

        for (i in 0 until entryCount) drawEntry(i, dataSet, mPathBuffer, mInnerRectBuffer)
        MPPointF.recycleInstance(center)
    }

    private fun getVisibleAngleCount(entryCount: Int, dataSet: IPieDataSet?): Int {
        var visibleAngleCount = 0
        for (j in 0 until entryCount) {
            // draw only if the value is greater than zero
            dataSet?.getEntryForIndex(j)?.y?.let {
                if (Math.abs(it) > Utils.FLOAT_EPSILON) visibleAngleCount++
            }
        }
        return visibleAngleCount
    }

    private fun drawEntry(index: Int, dataSet: IPieDataSet?, mPathBuffer: Path, mInnerRectBuffer: RectF) {
        val drawAngles = mChart.drawAngles
        val rotationAngle = mChart.rotationAngle
        val circleBox = mChart.circleBox
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY
        val sliceSpace = if (visibleAngleCount <= 1) 0f else getSliceSpace(dataSet)
        val sliceAngle = drawAngles[index]
        var innerRadius = userInnerRadius
        val entry = dataSet?.getEntryForIndex(index)

        // draw only if the value is greater than zero
        entry?.y?.let {
            if (Math.abs(it) > Utils.FLOAT_EPSILON) {
                if (!mChart.needsHighlight(index)) {
                    val accountForSliceSpacing = sliceSpace > 0f && sliceAngle <= 180f
                    mRenderPaint.color = dataSet.getColor(index)

                    val sliceSpaceAngleOuter = if (visibleAngleCount == 1) 0f
                    else sliceSpace / (Utils.FDEG2RAD * radius)
                    val startAngleOuter = rotationAngle + (angle + sliceSpaceAngleOuter / 2f) * phaseY
                    var sweepAngleOuter = (sliceAngle - sliceSpaceAngleOuter) * phaseY
                    if (sweepAngleOuter < 0f) sweepAngleOuter = 0f

                    mPathBuffer.reset()

                    val arcStartPointX =
                        center.x + radius * Math.cos((startAngleOuter * Utils.FDEG2RAD).toDouble()).toFloat()
                    val arcStartPointY =
                        center.y + radius * Math.sin((startAngleOuter * Utils.FDEG2RAD).toDouble()).toFloat()

                    if (sweepAngleOuter >= 360f && sweepAngleOuter % 360f <= Utils.FLOAT_EPSILON) {
                        // Android is doing "mod 360"
                        mPathBuffer.addCircle(center.x, center.y, radius, Path.Direction.CW)
                    } else {
                        mPathBuffer.moveTo(arcStartPointX, arcStartPointY)
                        mPathBuffer.arcTo(circleBox, startAngleOuter, sweepAngleOuter)
                    }

                    mInnerRectBuffer.set(
                        center.x - innerRadius,
                        center.y - innerRadius,
                        center.x + innerRadius,
                        center.y + innerRadius
                    )

                    if (drawInnerArc && (innerRadius > 0f || accountForSliceSpacing)) {
                        if (accountForSliceSpacing) {
                            var minSpacedRadius = calculateMinimumRadiusForSpacedSlice(
                                center, radius,
                                sliceAngle * phaseY,
                                arcStartPointX, arcStartPointY,
                                startAngleOuter,
                                sweepAngleOuter
                            )

                            minSpacedRadius = Math.abs(minSpacedRadius)
                            innerRadius = Math.max(innerRadius, minSpacedRadius)
                        }

                        val sliceSpaceAngleInner = if (visibleAngleCount == 1 || innerRadius == 0f) 0f
                        else sliceSpace / (Utils.FDEG2RAD * innerRadius)
                        val startAngleInner = rotationAngle + (angle + sliceSpaceAngleInner / 2f) * phaseY
                        var sweepAngleInner = (sliceAngle - sliceSpaceAngleInner) * phaseY
                        if (sweepAngleInner < 0f) sweepAngleInner = 0f
                        val endAngleInner = startAngleInner + sweepAngleInner

                        if (sweepAngleOuter >= 360f && sweepAngleOuter % 360f <= Utils.FLOAT_EPSILON) {
                            // Android is doing "mod 360"
                            mPathBuffer.addCircle(center.x, center.y, innerRadius, Path.Direction.CCW)
                        } else {
                            mPathBuffer.lineTo(
                                center.x + innerRadius * Math.cos((endAngleInner * Utils.FDEG2RAD).toDouble())
                                    .toFloat(),
                                center.y + innerRadius * Math.sin((endAngleInner * Utils.FDEG2RAD).toDouble()).toFloat()
                            )

                            mPathBuffer.arcTo(mInnerRectBuffer, endAngleInner, -sweepAngleInner)
                        }
                    } else {
                        if (sweepAngleOuter % 360f > Utils.FLOAT_EPSILON) {
                            if (accountForSliceSpacing) {
                                val angleMiddle = startAngleOuter + sweepAngleOuter / 2f
                                val sliceSpaceOffset = calculateMinimumRadiusForSpacedSlice(
                                    center,
                                    radius,
                                    sliceAngle * phaseY,
                                    arcStartPointX,
                                    arcStartPointY,
                                    startAngleOuter,
                                    sweepAngleOuter
                                )

                                val arcEndPointX =
                                    center.x + sliceSpaceOffset * Math.cos((angleMiddle * Utils.FDEG2RAD).toDouble())
                                        .toFloat()
                                val arcEndPointY =
                                    center.y + sliceSpaceOffset * Math.sin((angleMiddle * Utils.FDEG2RAD).toDouble())
                                        .toFloat()
                                mPathBuffer.lineTo(arcEndPointX, arcEndPointY)
                            } else mPathBuffer.lineTo(center.x, center.y)
                        }
                    }
                    mPathBuffer.close()
                    drawBorders(mPathBuffer)
                }
            }
        }
        angle += sliceAngle * phaseX
    }

    private fun drawBorders(mPathBuffer: Path) {
        mBitmapCanvas.drawPath(mPathBuffer, mRenderPaint)
        val strokePaint = Paint(ANTI_ALIAS_FLAG)
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = 4f
        strokePaint.color = borderColor
        mBitmapCanvas.drawPath(mPathBuffer, strokePaint)
    }
}
