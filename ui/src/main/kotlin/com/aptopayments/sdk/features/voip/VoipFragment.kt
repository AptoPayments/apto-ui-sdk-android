@file:Suppress("DEPRECATION")

package com.aptopayments.sdk.features.voip

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.media.AudioManager
import android.os.Bundle
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.voip.Action
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.voip.VoipViewModel.CallState
import com.aptopayments.sdk.utils.extensions.stringFromTimeInterval
import kotlinx.android.synthetic.main.fragment_voip.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val CARD_ID_KEY = "CARD_ID"
private const val ACTION_KEY = "ACTION"

internal class VoipFragment :
    BaseFragment(),
    VoipContract.View,
    KeyboardView.OnKeyboardActionListener {

    override var delegate: VoipContract.Delegate? = null
    private val viewModel: VoipViewModel by viewModel()
    private lateinit var cardId: String
    private lateinit var action: Action

    override fun setUpArguments() {
        cardId = requireArguments()[CARD_ID_KEY] as String
        action = requireArguments()[ACTION_KEY] as Action
    }

    override fun onStart() {
        super.onStart()
        context?.let { context ->
            val permission = checkPermission(Manifest.permission.RECORD_AUDIO)
            if (permission == PackageManager.PERMISSION_GRANTED) {
                viewModel.startCall(context, cardId, action)
            } else {
                requestPermission(Manifest.permission.RECORD_AUDIO) { granted ->
                    if (granted) {
                        viewModel.startCall(context, cardId, action)
                    } else {
                        delegate?.onVoipCallError("manage_card.get_pin_voip.no_microphone_permission.description".localized())
                    }
                }
            }
        }
    }

    override fun onBackPressed() = close()

    override fun layoutId(): Int = R.layout.fragment_voip

    override fun backgroundColor(): Int = UIConfig.uiNavigationSecondaryColor

    override fun setupViewModel() {
        viewModel.apply {
            observeNotNullable(callState) { callStateChanged(it) }
            observeNotNullable(callEstablished) { updateCallActionButtonState(it) }
            observeNotNullable(elapsedTime) { updateElapsedTime(it) }
            observe(failure) { handleFailure(it) }
        }
    }

    private fun updateElapsedTime(elapsedTime: Long?) {
        elapsedTime?.let { showEstablishedState(elapsedTime = it.toInt()) }
    }

    override fun setupListeners() {
        bttn_finish_call.setOnClickListener { close() }
        bttn_hide_keyboard.setOnClickListener { hideNumericKeyboard() }
        bttn_show_keyboard.setOnClickListener { showNumericKeyboard() }
        bttn_mute_call.setOnClickListener { toggleMute() }
        bttn_call_speaker.setOnClickListener { toggleSpeaker() }
        super.setupListeners()
    }

    private fun close() {
        viewModel.disconnect()
        delegate?.onVoipCallFinished()
    }

    private fun callStateChanged(newState: CallState?) {
        when (newState) {
            is CallState.NotInitiated -> showNotInitiatedState()
            is CallState.Ringing -> showRingingState()
            is CallState.Established -> showEstablishedState(newState.elapsedTime)
            is CallState.Finished -> delegate?.onVoipCallFinished()
            is CallState.Error -> showErrorState(newState.error)
            is CallState.Reconnecting -> showReconnectingState()
        }
    }

    override fun setupUI() {
        setupTheme()
        setupKeyboard()
    }

    private fun setupTheme() {
        setActionTitle()
        with(themeManager()) {
            customizeToolbarTitle(tv_title)
            customizeRegularTextLabel(tv_description)
            tv_description.setTextColor(Color.WHITE)
        }
    }

    private fun setActionTitle() {
        when (action) {
            Action.LISTEN_PIN -> {
                tv_title.localizedText = "manage_card.get_pin_voip.title"
                tv_description.localizedText = "manage_card.get_pin_voip.message"
            }
            else -> {
            }
        }
    }

    private fun setupKeyboard() {
        keyboard_view.keyboard = Keyboard(context, R.xml.keyboard)
        keyboard_view.setOnKeyboardActionListener(this)
        keyboard_view.isPreviewEnabled = false
        keyboard_view.setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        keyboard_view.hide()
    }

    override fun swipeRight() {}
    override fun onPress(p0: Int) {}
    override fun onRelease(p0: Int) {}
    override fun swipeLeft() {}
    override fun swipeUp() {}
    override fun swipeDown() {}
    override fun onText(p0: CharSequence?) {}
    override fun onKey(primaryCode: Int, keyCodes: IntArray?) =
        viewModel.sendDigits(convertFromAsciiToString(primaryCode))

    private fun convertFromAsciiToString(ascii: Int) = ascii.toChar().toString()

    private fun showNotInitiatedState() {
        showDefaultTexts()
    }

    private fun showRingingState() {
        showDefaultTexts()
    }

    private fun showDefaultTexts() {
        tv_title.localizedText = "manage_card_get_pin_voip_title"
        tv_description.localizedText = "manage_card_get_pin_voip_message"
    }

    private fun showEstablishedState(elapsedTime: Int) {
        tv_description.text = elapsedTime.stringFromTimeInterval()
    }

    private fun showReconnectingState() {
        tv_description.localizedText = "manage_card_get_pin_voip_reconnecting"
        tv_description.localizedText = "manage_card_get_pin_voip_reconnecting"
    }

    private fun showErrorState(error: String?) {
        delegate?.onVoipCallError(error)
    }

    private fun updateCallActionButtonState(isCallEstablished: Boolean) {
        bttn_call_speaker.isEnabled = isCallEstablished
        bttn_mute_call.isEnabled = isCallEstablished
        bttn_show_keyboard.isEnabled = isCallEstablished
        bttn_call_speaker.alpha = if (isCallEstablished) 1f else 0.3f
        bttn_mute_call.alpha = if (isCallEstablished) 1f else 0.3f
        bttn_show_keyboard.alpha = if (isCallEstablished) 1f else 0.3f
    }

    private fun showNumericKeyboard() {
        keyboard_view.show()
        bttn_hide_keyboard.show()
        ll_call_actions.hide()
    }

    private fun hideNumericKeyboard() {
        keyboard_view.hide()
        bttn_hide_keyboard.hide()
        ll_call_actions.show()
    }

    private fun toggleMute() {
        viewModel.toggleMute()
    }

    private fun toggleSpeaker() {
        (activity?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager)?.let { audioManager ->
            audioManager.isSpeakerphoneOn = !audioManager.isSpeakerphoneOn
        }
    }

    companion object {
        fun newInstance(cardId: String, action: Action) = VoipFragment().apply {
            arguments = Bundle().apply {
                putString(CARD_ID_KEY, cardId)
                putSerializable(ACTION_KEY, action)
            }
        }
    }
}
