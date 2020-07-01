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
import com.aptopayments.sdk.core.extension.remove
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.google.android.material.snackbar.BaseTransientBottomBar

class SnackbarThemeTwo private constructor(parent: ViewGroup, content: View, contentViewCallback: ContentViewCallback) :
    BaseTransientBottomBar<SnackbarThemeTwo>(parent, content, contentViewCallback) {

    private lateinit var titleTextView: TextView
    private lateinit var messageTextView: TextView
    private lateinit var background: LinearLayout

    init {
        findAllViews(content)
        setupUI()
    }

    fun setTitle(title: String?) {
        if (title != null) titleTextView.text = title
        else titleTextView.remove()
    }

    fun setMessage(message: String) {
        messageTextView.text = message
    }

    fun setBackgroundColor(backgroundColor: Int) {
        background.setBackgroundColor(backgroundColor)
    }

    private fun findAllViews(content: View) {
        titleTextView = content.findViewById(R.id.tv_snackbar_title)
        messageTextView = content.findViewById(R.id.tv_snackbar_text)
        background = content.findViewById(R.id.ll_snackbar_wrapper)
    }

    private fun setupUI() {
        with(themeManager()) {
            customizeBannerTitle(titleTextView)
            customizeBannerMessage(messageTextView)
        }
    }

    @Suppress("DEPRECATION")
    private class ContentViewCallback(private val view: View) : BaseTransientBottomBar.ContentViewCallback {

        override fun animateContentIn(delay: Int, duration: Int) {
            view.scaleY = 0f
            ViewCompat.animate(view)
                .scaleY(1f).setDuration(duration.toLong()).startDelay = delay.toLong()
        }

        override fun animateContentOut(delay: Int, duration: Int) {
            view.scaleY = 1f
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
            var tempView = view
            var fallback: ViewGroup? = null
            do {
                if (tempView is CoordinatorLayout) {
                    // We've found a CoordinatorLayout, use it
                    return tempView
                } else if (tempView is FrameLayout) {
                    if (tempView.id == android.R.id.content) {
                        // If we've hit the decor content view, then we didn't find a CoL in the
                        // hierarchy, so use it.
                        return tempView
                    } else {
                        // It's not the content view but we'll use it as our fallback
                        fallback = tempView
                    }
                }

                if (tempView != null) {
                    // Else, we will loop and crawl up the view hierarchy and try to find a parent
                    val parent = tempView.parent
                    tempView = if (parent is View) parent else null
                }
            } while (tempView != null)

            // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
            return fallback
        }
    }
}
