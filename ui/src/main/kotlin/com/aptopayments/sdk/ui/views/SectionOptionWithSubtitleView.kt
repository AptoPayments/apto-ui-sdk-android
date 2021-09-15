package com.aptopayments.sdk.ui.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.constraintlayout.widget.ConstraintLayout
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.goneIf
import com.aptopayments.sdk.core.extension.hide
import com.aptopayments.sdk.core.extension.visibleIf
import com.aptopayments.sdk.core.platform.theme.themeManager
import kotlinx.android.synthetic.main.view_section_option_subtitle.view.*

internal open class SectionOptionWithSubtitleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var optionTitle = ""
        set(value) {
            field = value
            option_title.localizedText = optionTitle
        }

    var optionDescription = ""
        set(value) {
            field = value
            option_subtitle.localizedText = optionDescription
            option_subtitle.goneIf(optionDescription.isEmpty())
        }
    var optionShowDivider = false
        set(value) {
            field = value
            option_bottom_divider.visibleIf(value)
        }

    init {
        inflate(context, R.layout.view_section_option_subtitle, this)
        customizeUi()
        attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(it, R.styleable.SectionOptionWithSubtitleView, 0, 0)

            optionTitle = typedArray.getString(R.styleable.SectionOptionWithSubtitleView_optionTitle) ?: ""
            optionDescription = typedArray.getString(R.styleable.SectionOptionWithSubtitleView_optionDescription) ?: ""
            optionShowDivider = typedArray.getBoolean(R.styleable.SectionOptionWithSubtitleView_optionShowDivider, true)
            typedArray.recycle()
        }
        setSelectableItemBackground()
    }

    private fun setSelectableItemBackground() {
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        setBackgroundResource(outValue.resourceId)
    }

    private fun customizeUi() {
        with(themeManager()) {
            customizeSectionOptionTitle(option_title)
            customizeSectionOptionDescription(option_subtitle)
            customizeSectionOptionIcon(option_icon)
        }
    }

    fun hideRightArrow() = option_icon.hide()
}
