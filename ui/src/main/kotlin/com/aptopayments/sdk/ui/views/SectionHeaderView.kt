package com.aptopayments.sdk.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.platform.theme.themeManager
import kotlinx.android.synthetic.main.view_secret_pin.view.*
import kotlinx.android.synthetic.main.view_section_header.view.*

internal class SectionHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var title = ""
        set(value) {
            field = value
            section_header_title.localizedText = title
        }

    init {
        inflate(context, R.layout.view_section_header, this)
        customizeUi()
        attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(it, R.styleable.SectionHeaderView, 0, 0)

            title = typedArray.getString(R.styleable.SectionHeaderView_headerTitle) ?: ""
            typedArray.recycle()
        }
        orientation = VERTICAL
    }

    private fun customizeUi() {
        themeManager().customizeStarredSectionTitle(section_header_title, UIConfig.textSecondaryColor)
    }
}
