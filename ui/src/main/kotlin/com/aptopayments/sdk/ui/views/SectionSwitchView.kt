package com.aptopayments.sdk.ui.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.constraintlayout.widget.ConstraintLayout
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.goneIf
import com.aptopayments.sdk.core.extension.visibleIf
import com.aptopayments.sdk.core.platform.theme.themeManager
import kotlinx.android.synthetic.main.view_section_switch.view.*
import android.widget.CompoundButton.OnCheckedChangeListener

internal class SectionSwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var optionTitle = ""
        set(value) {
            field = value
            section_switch_title.localizedText = optionTitle
        }

    var optionDescription = ""
        set(value) {
            field = value
            section_switch_description.localizedText = optionDescription
            section_switch_description.goneIf(optionDescription.isEmpty())
        }
    var optionShowDivider = false
        set(value) {
            field = value
            section_switch_separator.visibleIf(value)
        }

    var isChecked: Boolean
        get() = section_switch_switch.isChecked
        set(value) {
            section_switch_switch.isChecked = value
        }

    init {
        inflate(context, R.layout.view_section_switch, this)
        customizeUi()
        attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(it, R.styleable.SectionSwitchView, 0, 0)

            optionTitle = typedArray.getString(R.styleable.SectionSwitchView_optionTitle) ?: ""
            optionDescription = typedArray.getString(R.styleable.SectionSwitchView_optionDescription) ?: ""
            optionShowDivider = typedArray.getBoolean(R.styleable.SectionSwitchView_optionShowDivider, true)
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
            customizeSectionOptionTitle(section_switch_title)
            customizeSectionOptionDescription(section_switch_description)
            customizeSwitch(section_switch_switch)
        }
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        section_switch_switch.setOnCheckedChangeListener(listener)
    }

    fun silentlyToggleSwitch(listener: ((value: Boolean) -> Unit)?) {
        section_switch_switch.setOnCheckedChangeListener(null)
        section_switch_switch.toggle()
        if (listener == null) {
            section_switch_switch.setOnCheckedChangeListener(null)
        } else {
            section_switch_switch.setOnCheckedChangeListener { _, value -> listener(value) }
        }
    }
}
