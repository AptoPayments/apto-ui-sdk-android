package com.aptopayments.sdk.core.platform

import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.isVisible
import com.aptopayments.sdk.core.extension.remove
import com.aptopayments.sdk.core.extension.show
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.DarkThemeUtils
import com.aptopayments.sdk.utils.ViewUtils
import com.aptopayments.sdk.utils.extensions.setColorFilterCompat
import kotlinx.android.synthetic.main.activity_layout.*
import kotlinx.android.synthetic.main.include_rl_loading.*

private const val RECORD_REQUEST_CODE = 101

abstract class BaseActivity : AppCompatActivity() {

    private var isLoading = false
    private var onRequestPermissionsResult: ((Boolean) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureDarkTheme()
        setContentView(R.layout.activity_layout)
    }

    fun showLoading() {
        isLoading = true
        if (rl_loading_view.isVisible()) return
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        pb_progress.indeterminateDrawable.setColorFilterCompat(UIConfig.uiPrimaryColor, PorterDuff.Mode.SRC_IN)
        rl_loading_view.show()
        rl_loading_view.startAnimation(animation)
    }

    fun hideLoading() {
        isLoading = false
        if (!rl_loading_view.isVisible()) return
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        rl_loading_view.remove()
        rl_loading_view.startAnimation(animation)
    }

    internal fun hideKeyboard()  = ViewUtils.hideKeyboard(this)

    internal fun showKeyboard()  = ViewUtils.showKeyboard(this)

    override fun onBackPressed() {
        if (isLoading) return
        (supportFragmentManager.findFragmentById(
                R.id.fragmentContainer) as? BaseFragment)?.onBackPressed() ?: super.onBackPressed()
    }

    internal fun checkPermission(permission: String): Int {
        return ContextCompat.checkSelfPermission(this, permission)
    }

    internal fun requestPermission(permission: String, onResult: (Boolean) -> Unit) {
        onRequestPermissionsResult = onResult
        ActivityCompat.requestPermissions(this, arrayOf(permission), RECORD_REQUEST_CODE)
    }

    protected fun confirm(
        title: String, text: String, confirm: String, cancel: String, onConfirm: (Unit) -> Unit,
        onCancel: (Unit) -> Unit
    ) {
        val alertDialogBuilder = ViewUtils.getAlertDialogBuilder(this,
            confirm, cancel, { onConfirm(Unit) }, { onCancel(Unit) })
        themeManager().getAlertDialog(alertDialogBuilder, title, text).show()
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

    private fun configureDarkTheme() {
        val darkThemUtils = DarkThemeUtils(AptoUiSdk, this)
        UIConfig.darkTheme = darkThemUtils.isEnabled()
    }
}
