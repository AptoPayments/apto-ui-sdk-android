package com.aptopayments.sdk.utils

import android.graphics.Typeface
import java.util.*

object FontsUtil {

    private val mFontsMap = HashMap<FontType, Typeface>()

    enum class FontType {
        BOLD,
        REGULAR,
        MEDIUM,
        SEMI_BOLD
    }

    fun overrideFonts(regularFont: Typeface?, boldFont: Typeface?, mediumFont: Typeface?, semiBoldFont: Typeface?) {
        regularFont?.let { mFontsMap[FontType.REGULAR] = it }
        boldFont?.let { mFontsMap[FontType.BOLD] = it }
        mediumFont?.let { mFontsMap[FontType.MEDIUM] = it }
        semiBoldFont?.let { mFontsMap[FontType.SEMI_BOLD] = it }
    }

    fun getFontOfType(type: FontType): Typeface? {
        return mFontsMap[type]
    }
}
