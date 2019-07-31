package com.aptopayments.sdk.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.aptopayments.sdk.core.extension.hide
import com.aptopayments.sdk.core.platform.theme.themeManager
import kotlinx.android.synthetic.main.view_section_switch_two.view.*

class SectionSwitchViewTwo
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr, defStyleRes)
{

    override fun onFinishInflate() {
        super.onFinishInflate()
        setupTheme()
    }

    fun set(title: String, description: String?) {
        tv_section_switch_title.text = title
        tv_section_switch_description.text = description
    }

    fun hideBottomSeparator() {
        vw_option_bottom_separator.hide()
    }

    private fun setupTheme() {
        with(themeManager()) {
            customizeSectionOptionTitle(tv_section_switch_title)
            customizeSectionOptionDescription(tv_section_switch_description)
            customizeSwitch(sw_tv_section_switch_switch)
        }
    }
}
