package com.aptopayments.sdk.core.extension

import android.graphics.PorterDuff
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.utils.extensions.setColorFilterCompat

internal fun Toolbar.configure(fragment: BaseFragment, config: ToolbarConfiguration) {
    title = config.title
    when (config.backButtonMode) {
        is BackButtonMode.Back -> {
            val closeIcon = this.context.getDrawable(R.drawable.ic_nav_back_icon)
            closeIcon?.setColorFilterCompat(config.backButtonMode.color, PorterDuff.Mode.SRC_ATOP)
            navigationIcon = closeIcon
        }
        is BackButtonMode.Close -> {
            val closeIcon = this.context.getDrawable(R.drawable.ic_close)
            closeIcon?.setColorFilterCompat(config.backButtonMode.color, PorterDuff.Mode.SRC_ATOP)
            navigationIcon = closeIcon
        }
        is BackButtonMode.None -> {
        }
    }
    config.backgroundColor?.let { setBackgroundColor(it) }
    config.titleTextColor?.let { setTitleTextColor(it) }
    setNavigationOnClickListener { fragment.onBackPressed() }
}

sealed class BackButtonMode {
    class Back(@ColorInt val color: Int = UIConfig.textTopBarPrimaryColor) : BackButtonMode()
    class Close(@ColorInt val color: Int = UIConfig.textTopBarPrimaryColor) : BackButtonMode()
    object None : BackButtonMode()
}

class ToolbarConfiguration private constructor(
    val backButtonMode: BackButtonMode,
    val title: String,
    val backgroundColor: Int?,
    val titleTextColor: Int?
) {

    data class Builder(
        private var backButtonMode: BackButtonMode = BackButtonMode.Back(),
        private var title: String = "",
        @ColorInt private var backgroundColor: Int? = null,
        @ColorInt private var titleTextColor: Int? = null
    ) {
        fun backButtonMode(backButtonMode: BackButtonMode) = apply { this.backButtonMode = backButtonMode }
        fun title(title: String?) = apply { this.title = title ?: "" }
        fun backgroundColor(backgroundColor: Int) = apply { this.backgroundColor = backgroundColor }
        fun titleTextColor(titleTextColor: Int) = apply { this.titleTextColor = titleTextColor }

        fun build() = ToolbarConfiguration(backButtonMode, title, backgroundColor, titleTextColor)

        fun setPrimaryColors() = apply {
            backgroundColor = UIConfig.uiNavigationPrimaryColor
            titleTextColor = UIConfig.textTopBarPrimaryColor
        }

        fun setSecondaryColors() = apply {
            backgroundColor = UIConfig.uiNavigationSecondaryColor
            titleTextColor = UIConfig.textTopBarSecondaryColor
        }

        fun setSecondaryTertiaryColors() = apply {
            backgroundColor = UIConfig.uiNavigationSecondaryColor
            titleTextColor = UIConfig.iconTertiaryColor
        }
    }
}
