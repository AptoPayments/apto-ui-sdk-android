package com.aptopayments.sdk.ui.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.aptopayments.sdk.R
import com.aptopayments.core.data.content.Content
import com.aptopayments.sdk.core.extension.loadFromUrlWithListener
import com.aptopayments.sdk.core.extension.remove
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.view_native_content.view.*

class NativeContentView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.view_native_content, this)
    }

    var delegate: NativeContentContract.Delegate? = null

    var content: Content.Native? = null
        set(value) {
            field = value
            value?.let { updateContent(it) }
        }

    private fun updateContent(content: Content.Native) {
        content.asset?.let {
            iv_native_content_asset.loadFromUrlWithListener(
                    url = it.toString(),
                    listener = object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            delegate?.onNativeContentLoadingFailed()
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            delegate?.onNativeContentLoaded()
                            return false
                        }
                    })
        } ?: run {
            iv_native_content_asset.remove()
            delegate?.onNativeContentLoaded()
        }
        with(themeManager()) {
            customizeLargeTitleLabel(tv_native_content_title)
            customizeRegularTextLabel(tv_native_content_description_main)
            customizeRegularTextLabel(tv_native_content_description_secondary)
        }
    }
}
