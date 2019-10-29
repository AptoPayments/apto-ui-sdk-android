package com.aptopayments.sdk.ui.fragments.pdf

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.goneIf
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.ui.views.ZoomScrollListener
import com.aptopayments.sdk.utils.FileSharer
import kotlinx.android.synthetic.main.fragment_pdf_renderer.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf
import java.io.File

private const val FILE_KEY = "file"
private const val TITLE_KEY = "title"

internal class PdfRendererFragment : BaseFragment(),
    PdfRendererContract.View, KoinComponent {

    override var delegate: PdfRendererContract.Delegate? = null
    private val viewModel: PdfRendererViewModel by viewModel { parametersOf(file) }
    private lateinit var file: File
    private lateinit var title: String
    private val fileSharer: FileSharer by inject()
    private var menu: Menu? = null

    override fun layoutId() = R.layout.fragment_pdf_renderer

    override fun setUpArguments() {
        file = arguments!![FILE_KEY] as File
        title = arguments!![TITLE_KEY] as String
    }

    private val zoomScrollListener: ZoomScrollListener by lazy { ZoomScrollListener(context!!) }

    override fun setupViewModel() {
        observeNotNullable(viewModel.pageBitmap) {
            image.setImageBitmap(it)
            zoomScrollListener.resetScale()
        }
        observeNotNullable(viewModel.previousEnabled) { previous.isEnabled = it }
        observeNotNullable(viewModel.nextEnabled) { next.isEnabled = it }
        observeNotNullable(viewModel.buttonsVisible) { buttonBar.goneIf(it) }
    }

    override fun setupUI() {
        setClickListeners()
        setUpToolbar()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_pdf_renderer, menu)
        setupMenuItem(menu, R.id.menu_pdf_share)
        this.menu = menu
        tintMenuItem()
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun tintMenuItem() {
        menu?.let {
            val item = it.findItem(R.id.menu_pdf_share)
            val icon = item.icon
            DrawableCompat.setTint(icon, UIConfig.textTopBarSecondaryColor)
            item.icon = icon
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_pdf_share -> {
                share(file)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setClickListeners() {
        previous.setOnClickListener { viewModel.showPrevious() }
        next.setOnClickListener { viewModel.showNext() }
        image.setOnTouchListener(zoomScrollListener)
    }

    private fun share(file: File) {
        context?.let {
            fileSharer.shareFile(file, it)
        }
    }

    private fun setUpToolbar() {
        val toolbar = tb_llsdk_toolbar as Toolbar
        toolbar.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)

        tb_llsdk_toolbar.setTitleTextColor(UIConfig.textTopBarSecondaryColor)
        delegate?.configureToolbar(
            tb_llsdk_toolbar,
            title,
            backButtonMode = BaseActivity.BackButtonMode.Back(null, UIConfig.textTopBarSecondaryColor)
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
