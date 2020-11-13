package com.aptopayments.sdk.features.card.cardstats.chart

import android.content.Context
import android.util.AttributeSet
import com.aptopayments.mobile.data.config.UIConfig
import com.github.mikephil.charting.charts.PieChart

internal class AptoPieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PieChart(context, attrs, defStyleAttr) {

    init {
        super.init()
        mRenderer = AptoPieChartRenderer(
            this,
            mAnimator,
            mViewPortHandler,
            UIConfig.iconPrimaryColor
        )
    }
}
