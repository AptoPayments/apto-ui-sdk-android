package com.aptopayments.sdk.features.biometric

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.annotation.VisibleForTesting
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.platform.BaseDialogFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.BiometricAuthenticationResult
import com.aptopayments.sdk.utils.BiometricAuthenticator
import com.aptopayments.sdk.utils.BiometricAvailability
import kotlinx.android.synthetic.main.fragment_biometric_dialog.*
import java.lang.reflect.Modifier

private const val TITLE_KEY = "TITLE"
private const val DESCRIPTION_KEY = "DESCRIPTION"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class BiometricDialogFragmentThemeTwo : BaseDialogFragment(), BiometricDialogContract.View {

    override var delegate: BiometricDialogContract.Delegate? = null
    private var title: String = ""
    private var description: String = ""
    private var biometricAuthenticator: BiometricAuthenticator? = null
    private var pendingDismissalNotification = true

    override fun layoutId(): Int = R.layout.fragment_biometric_dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments!![TITLE_KEY] as String
        description = arguments!![DESCRIPTION_KEY] as String
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val window = dialog!!.window!!
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                resizeWindow(window, view.height)
                positionWindow(window)
            }
        })
    }

    private fun resizeWindow(window: Window, height: Int) {
        val viewHeightPixels = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                height.toFloat(),
                resources.displayMetrics
        )
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(window.attributes)
        val maxHeightPixels = resources.displayMetrics.heightPixels * 0.30
        if(viewHeightPixels > maxHeightPixels) {
            window.setLayout(layoutParams.width, maxHeightPixels.toInt())
        }
    }

    private fun positionWindow(window: Window) {
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setGravity(Gravity.CENTER)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (pendingDismissalNotification) delegate?.onAuthCancelled()
    }

    override fun setUpUI() {
        setupTheme()
        setupTexts()
    }

    private fun setupTheme() {
        themeManager().customizeHighlightTitleLabel(tv_dialog_title)
    }

    private fun setupTexts() {
        tv_dialog_title.text = title
        tv_dialog_description.text = description
    }

    override fun setUpViewModel() {}

    override fun setUpListeners() {}

    override fun onResume() {
        super.onResume()
        biometricAuthenticator?.stopAuth()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context?.let { context ->
                val availability = BiometricAuthenticator.authAvailable(context)
                if (availability == BiometricAvailability.AVAILABLE) {
                    biometricAuthenticator = BiometricAuthenticator()
                    biometricAuthenticator?.startAuth(context) { result ->
                        when (result) {
                            BiometricAuthenticationResult.Success -> {
                                biometricAuthenticator?.stopAuth()
                                pendingDismissalNotification = false
                                delegate?.onAuthSuccess()
                            }
                            BiometricAuthenticationResult.Failure -> {
                                biometricAuthenticator?.stopAuth()
                                delegate?.onAuthFailure()
                            }
                        }
                    }
                }
                else {
                    fingerprint_status.text = availability.toLocalizedDescription(context)
                }
            }
        }
        else delegate?.onAuthNotAvailable()
    }

    override fun onStop() {
        biometricAuthenticator?.stopAuth()
        super.onStop()
    }

    companion object {
        fun newInstance(title: String, description: String) = BiometricDialogFragmentThemeTwo().apply {
            arguments = Bundle().apply {
                putString(TITLE_KEY, title)
                putString(DESCRIPTION_KEY, description)
            }
        }
    }
}
