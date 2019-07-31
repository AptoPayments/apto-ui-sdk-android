package com.aptopayments.sdk.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.google.android.material.snackbar.BaseTransientBottomBar

class SnackbarThemeTwo private constructor(
        parent: ViewGroup, content: View, contentViewCallback: ContentViewCallback
): BaseTransientBottomBar<SnackbarThemeTwo>(parent, content, contentViewCallback) {

    private lateinit var title: TextView
    private lateinit var message: TextView
    private lateinit var background: LinearLayout

    init {
        findAllViews(content)
        setupUI()
    }

    fun setTitle(title: String) {
        this.title.text = title
    }

    fun setMessage(message: String) {
        this.message.text = message
    }

    fun setBackgroundColor(backgroundColor: Int) {
        background.setBackgroundColor(backgroundColor)
    }

    private fun findAllViews(content: View) {
        title = content.findViewById(R.id.tv_snackbar_title)
        message = content.findViewById(R.id.tv_snackbar_text)
        background = content.findViewById(R.id.ll_snackbar_wrapper)
    }

    private fun setupUI() {
        with(themeManager()) {
            customizeBannerTitle(title)
            customizeBannerMessage(message)
        }
    }

    private class ContentViewCallback(private val view: View) : BaseTransientBottomBar.ContentViewCallback {

        override fun animateContentIn(delay: Int, duration: Int) {
            ViewCompat.setScaleY(view, 0f)
            ViewCompat.animate(view)
                    .scaleY(1f).setDuration(duration.toLong()).startDelay = delay.toLong()
        }

        override fun animateContentOut(delay: Int, duration: Int) {
            ViewCompat.setScaleY(view, 1f)
            ViewCompat.animate(view)
                    .scaleY(0f)
                    .setDuration(duration.toLong()).startDelay = delay.toLong()
        }
    }

    companion object {
        fun make(rootView: View, duration: Int): SnackbarThemeTwo {
            val inflater = LayoutInflater.from(rootView.context)
            val parent = findSuitableParent(rootView)
            val view = inflater.inflate(R.layout.snackbar_theme_two, parent, false)
            val callback = ContentViewCallback(view)
            val snackbar = SnackbarThemeTwo(parent!!, view, callback)
            snackbar.duration = duration
            snackbar.view.setPadding(0, 0, 0, 0)
            return snackbar
        }

        private fun findSuitableParent(view: View?): ViewGroup? {
            var view = view
            var fallback: ViewGroup? = null
            do {
                if (view is CoordinatorLayout) {
                    // We've found a CoordinatorLayout, use it
                    return view
                } else if (view is FrameLayout) {
                    if (view.id == android.R.id.content) {
                        // If we've hit the decor content view, then we didn't find a CoL in the
                        // hierarchy, so use it.
                        return view
                    } else {
                        // It's not the content view but we'll use it as our fallback
                        fallback = view
                    }
                }

                if (view != null) {
                    // Else, we will loop and crawl up the view hierarchy and try to find a parent
                    val parent = view.parent
                    view = if (parent is View) parent else null
                }
            } while (view != null)

            // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
            return fallback
        }
    }
}
