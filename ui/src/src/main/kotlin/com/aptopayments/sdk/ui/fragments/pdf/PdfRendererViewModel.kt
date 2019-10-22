package com.aptopayments.sdk.ui.fragments.pdf

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.File

class PdfRendererViewModel(
    val file: File
) : ViewModel() {

    private var fileDescriptor: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null
    private var pageIndex = 0
    private var currentPage: PdfRenderer.Page? = null
    private var cleared = false

    private val _pageBitmap = MutableLiveData<Bitmap>()
    val pageBitmap: LiveData<Bitmap>
        get() = _pageBitmap

    private val _previousEnabled = MutableLiveData<Boolean>()
    val previousEnabled: LiveData<Boolean>
        get() = _previousEnabled

    private val _nextEnabled = MutableLiveData<Boolean>()
    val nextEnabled: LiveData<Boolean>
        get() = _nextEnabled

    private val _buttonsVisible = MutableLiveData<Boolean>()
    val buttonsVisible: LiveData<Boolean>
        get() = _buttonsVisible

    init {
        viewModelScope.launch {
            openPdfRenderer()
            showPage(0)

            if (cleared) {
                closePdfRenderer()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            closePdfRenderer()
            cleared = true
        }
    }

    fun showPrevious() {
        viewModelScope.launch {
            currentPage?.let { page ->
                if (page.index > 0) {
                    showPage(page.index - 1)
                }
            }
        }
    }

    fun showNext() {
        viewModelScope.launch {
            pdfRenderer?.let { renderer ->
                currentPage?.let { page ->
                    if (page.index + 1 < renderer.pageCount) {
                        showPage(page.index + 1)
                    }
                }
            }
        }
    }

    private fun openPdfRenderer() {
        fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).also {
            pdfRenderer = PdfRenderer(it)
        }
    }

    private fun showPage(index: Int) {
        closeCurrentPage()
        setNewPage(index)
    }

    private fun setNewPage(index: Int) {
        pageIndex = index
        pdfRenderer?.let { renderer ->
            val page = renderer.openPage(index).also { currentPage = it }
            renderPage(page, renderer)
        }
    }

    private fun renderPage(page: PdfRenderer.Page, renderer: PdfRenderer, matrix: Matrix? = null) {
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        _pageBitmap.postValue(bitmap)
        updateValues(renderer)
    }

    private fun closeCurrentPage() {
        currentPage?.let { page ->
            currentPage = null
            page.close()
        }
    }

    private fun updateValues(renderer: PdfRenderer) {
        val count = renderer.pageCount
        _previousEnabled.postValue(pageIndex > 0)
        _nextEnabled.postValue(pageIndex + 1 < count)
        _buttonsVisible.postValue(count == 1)
    }

    private fun closePdfRenderer() {
        currentPage?.close()
        pdfRenderer?.close()
        fileDescriptor?.close()
    }
}
