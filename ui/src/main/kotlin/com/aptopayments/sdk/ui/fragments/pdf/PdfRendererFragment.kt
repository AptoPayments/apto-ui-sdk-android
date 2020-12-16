package com.aptopayments.sdk.ui.fragments.pdf

import android.os.Bundle
import android.view.MenuItem
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.BackButtonMode
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.FileSharer
import kotlinx.android.synthetic.main.fragment_pdf_renderer.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.File

private const val FILE_KEY = "file"
private const val TITLE_KEY = "title"

internal class PdfRendererFragment :
    BaseFragment(),
    PdfRendererContract.View,
    KoinComponent {

    override var delegate: PdfRendererContract.Delegate? = null
    private lateinit var file: File
    private lateinit var title: String
    private val fileSharer: FileSharer by inject()

    override fun layoutId() = R.layout.fragment_pdf_renderer

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setUpArguments() {
        file = requireArguments()[FILE_KEY] as File
        title = requireArguments()[TITLE_KEY] as String
    }

    override fun setupViewModel() {
    }

    override fun viewLoaded() {
        loadAndConfigurePdf()
    }

    private fun loadAndConfigurePdf() {
        pdfView.fromFile(file)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(0)
            .enableAntialiasing(true)
            .load()
    }

    override fun setupUI() {
        setUpToolbar()
    }

    private fun tintMenuItem() {
        tb_llsdk_toolbar?.menu?.let {
            themeManager().customizeMenuImage(it.findItem(R.id.menu_pdf_share))
        }
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_pdf_share -> {
                share(file)
                true
            }
            else -> false
        }
    }

    private fun share(file: File) {
        context?.let {
            fileSharer.shareFile(file, it)
        }
    }

    private fun setUpToolbar() {
        tb_llsdk_toolbar?.apply {
            inflateMenu(R.menu.menu_pdf_renderer)
            setOnMenuItemClickListener { onMenuItemSelected(it) }
            tintMenuItem()
        }
        tb_llsdk_toolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .backButtonMode(BackButtonMode.Back(UIConfig.textTopBarSecondaryColor))
                .title(title)
                .setSecondaryColors()
                .build()
        )
    }

    override fun onBackPressed() {
        delegate?.onPdfBackPressed()
    }

    companion object {
        fun newInstance(title: String, file: File) = PdfRendererFragment().apply {
            arguments = Bundle().apply {
                putSerializable(FILE_KEY, file)
                putSerializable(TITLE_KEY, title)
            }
        }
    }
}
