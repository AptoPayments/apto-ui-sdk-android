package com.aptopayments.sdk.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.aptopayments.sdk.core.extension.hide
import com.aptopayments.sdk.core.platform.theme.themeManager
import kotlinx.android.synthetic.main.view_section_option_two.view.*

class SectionOptionViewTwo
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
    RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        setupTheme()
    }

    fun set(title: String) {
        tv_title.text = title
    }

    fun hideBottomSeparator() {
        vw_option_bottom_separator.hide()
    }

    private fun setupTheme() {
        with(themeManager()) {
            customizeSectionTitle(tv_title)
            customizeSectionOptionIcon(iv_icon)
        }
    }
}
