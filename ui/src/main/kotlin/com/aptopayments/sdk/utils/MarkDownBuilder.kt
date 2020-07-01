package com.aptopayments.sdk.utils

import android.content.Context
import com.aptopayments.mobile.data.config.UIConfig
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.image.glide.GlideImagesPlugin

internal class MarkDownBuilder {

    fun build(context: Context): Markwon {
        return Markwon.builder(context)
            .usePlugin(GlideImagesPlugin.create(context))
            .usePlugin(TablePlugin.create(context))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    super.configureTheme(builder)
                    builder.linkColor(UIConfig.textLinkColor)
                }
            })
            .build()
    }
}
