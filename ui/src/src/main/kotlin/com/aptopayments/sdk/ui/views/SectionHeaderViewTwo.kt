package com.aptopayments.sdk.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.sdk.core.platform.theme.themeManager
import kotlinx.android.synthetic.main.view_section_option_two.view.*

class SectionHeaderViewTwo
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        setupTheme()
    }

    fun set(title: String) {
        tv_title.text = title
    }

    private fun setupTheme() = themeManager().customizeStarredSectionTitle(tv_title, UIConfig.textSecondaryColor)
}
