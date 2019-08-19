package com.aptopayments.sdk.core.platform

import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aptopayments.sdk.R
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.sdk.core.extension.remove
import com.aptopayments.sdk.core.extension.show
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.ViewUtils
import kotlinx.android.synthetic.main.activity_layout.*
import kotlinx.android.synthetic.main.include_rl_loading.*

abstract class BaseActivity : AppCompatActivity() {

    private var isLoading = false
    private var onRequestPermissionsResult: ((Boolean) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout)
    }

    internal fun showLoading() {
        isLoading = true
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        pb_progress.indeterminateDrawable.setColorFilter(UIConfig.uiPrimaryColor, PorterDuff.Mode.SRC_IN)
        rl_loading_view.show()
        rl_loading_view.startAnimation(animation)
    }

    internal fun hideLoading() {
        isLoading = false
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        rl_loading_view.remove()
        rl_loading_view.startAnimation(animation)
    }

    internal fun hideKeyboard()  = ViewUtils.hideKeyboard(this)

    internal fun showKeyboard()  = ViewUtils.showKeyboard(this)

    fun configureStatusBar() {
        window?.let { themeManager().customizeStatusBar(it) }
    }

    fun configureSecondaryStatusBar() {
        window?.let { themeManager().customizeSecondaryNavigationStatusBar(it) }
    }

    fun configureToolbar(
            toolbar: Toolbar,
            title: String?,
            backButtonMode: BackButtonMode) {
        if (supportActionBar != toolbar) {
            setSupportActionBar(toolbar)
        }
        when (backButtonMode) {
            is BackButtonMode.Back -> {
                if (backButtonMode.title.isNullOrEmpty()) {
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    val closeIcon = getDrawable(R.drawable.ic_nav_back_icon)
                    closeIcon?.setColorFilter(backButtonMode.color, PorterDuff.Mode.SRC_ATOP)
                    supportActionBar?.setHomeAsUpIndicator(closeIcon)
                }
                // TODO: Handle the case where's there's a title to be shown as back button
            }
            is BackButtonMode.Close -> {
                if (backButtonMode.title.isNullOrEmpty()) {
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    val closeIcon = getDrawable(R.drawable.ic_close)
                    closeIcon?.setColorFilter(backButtonMode.color, PorterDuff.Mode.SRC_ATOP)
                    supportActionBar?.setHomeAsUpIndicator(closeIcon)
                }
                // TODO: Handle the case where's there's a title to be shown as close button
            }
            // TODO: Handle the "None" case
        }
        supportActionBar?.title = title
    }

    override fun onBackPressed() {
        if (isLoading) return
        (supportFragmentManager.findFragmentById(
                R.id.fragmentContainer) as? BaseFragment)?.onBackPressed() ?: super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                this.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    sealed class BackButtonMode {
        class Back(val title: String?, @ColorInt val color: Int = UIConfig.textTopBarPrimaryColor) : BackButtonMode()
        class Close(val title: String?, @ColorInt val color: Int = UIConfig.textTopBarPrimaryColor) : BackButtonMode()
        object None : BackButtonMode()
    }

    sealed class NextButtonMode {
        class Next(val title: String?) : NextButtonMode()
        object None : NextButtonMode()
    }

    internal fun checkPermission(permission: String): Int {
        return ContextCompat.checkSelfPermission(this, permission)
    }

    private val RECORD_REQUEST_CODE = 101
    internal fun requestPermission(permission: String, onResult: (Boolean) -> Unit) {
        onRequestPermissionsResult = onResult
        ActivityCompat.requestPermissions(this, arrayOf(permission), RECORD_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    onRequestPermissionsResult?.invoke(false)
                } else {
                    onRequestPermissionsResult?.invoke(true)
                }
                onRequestPermissionsResult = null
            }
        }
    }
}

