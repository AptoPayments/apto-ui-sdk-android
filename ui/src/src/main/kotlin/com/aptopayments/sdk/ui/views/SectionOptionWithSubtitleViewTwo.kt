package com.aptopayments.sdk.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.aptopayments.sdk.core.extension.hide
import com.aptopayments.sdk.core.platform.theme.themeManager
import kotlinx.android.synthetic.main.view_section_option_subtitle_two.view.*

class SectionOptionWithSubtitleViewTwo
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr, defStyleRes)
{

    override fun onFinishInflate() {
        super.onFinishInflate()
        setupTheme()
    }

    fun set(title: String, description: String?) {
        tv_option_subtitle_title.text = title
        tv_option_subtitle_description.text = description
    }

    fun hideBottomSeparator() {
        vw_option_bottom_separator.hide()
    }

    private fun setupTheme() {
        with(themeManager()) {
            customizeSectionOptionTitle(tv_option_subtitle_title)
            customizeSectionOptionDescription(tv_option_subtitle_description)
            customizeSectionOptionIcon(iv_option_subtitle_icon)
        }
    }
}
