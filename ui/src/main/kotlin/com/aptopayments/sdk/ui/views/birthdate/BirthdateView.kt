package com.aptopayments.sdk.ui.views.birthdate

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.TextInputWatcher
import com.aptopayments.sdk.utils.ValidInputListener
import com.aptopayments.sdk.utils.ViewUtils.showKeyboard
import com.aptopayments.sdk.utils.extensions.shake
import kotlinx.android.synthetic.main.view_birthdate_picker.view.*
import org.threeten.bp.LocalDate

private const val FORMAT_TWO = "%02d"

class BirthdateView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr) {
    private val dateParser = BirthdateViewDateParser()
    private lateinit var editTextList: MutableList<EditText>
    private lateinit var birthdayDay: EditText
    private lateinit var birthdayMonth: EditText
    private lateinit var birthdayYear: EditText

    private var day = ""
    private var month = ""
    private var year = ""

    var delegate: Delegate? = null

    interface Delegate {
        fun onDateInput(value: LocalDate?)
    }

    init {
        inflate(context, R.layout.view_birthdate_picker, this)
        configureViewOrder()
    }

    fun clear() {
        editTextList.forEach { it.text.clear() }
    }

    fun setDate(date: LocalDate) {
        birthdayDay.setText(FORMAT_TWO.format(date.dayOfMonth))
        birthdayMonth.setText(FORMAT_TWO.format(date.monthValue))
        birthdayYear.setText(date.year.toString())
    }

    fun getDate(): LocalDate? {
        val date = dateParser.parse(year, month, day)
        return date?.let {
            if (date.year > 1900 && date.isBefore(LocalDate.now())) {
                it
            } else {
                this.shake()
                null
            }
        }
    }

    private fun onNewDateEntered() {
        delegate?.onDateInput(getDate())
    }

    private fun configureViewOrder() {
        val provider = FormatOrderProvider(context)
        val dateOrder = FormatOrderGenerator(provider).getFormatOrder()
        showCorrectStub(dateOrder)
        bindDateViews()
        setEditTextOrderList(dateOrder)
        configureDateViewsListeners()
        focusFirst()
    }

    private fun showCorrectStub(dateOrderDate: DateFormatOrder) {
        when (dateOrderDate) {
            DateFormatOrder.MDY -> stub_mdy
            DateFormatOrder.YMD -> stub_ymd
            else -> stub_dmy
        }.inflate()
    }

    private fun bindDateViews() {
        birthdayDay = findViewById(R.id.et_birthday_day)
        birthdayMonth = findViewById(R.id.et_birthday_month)
        birthdayYear = findViewById(R.id.et_birthday_year)
        container.findViewById<View>(R.id.separator_left).setBackgroundColor(UIConfig.uiBackgroundPrimaryColor)
        container.findViewById<View>(R.id.separator_right).setBackgroundColor(UIConfig.uiBackgroundPrimaryColor)
        with(themeManager()) {
            customizeEditText(birthdayDay)
            customizeEditText(birthdayMonth)
            customizeEditText(birthdayYear)
        }
    }

    private fun setEditTextOrderList(isDayMonth: DateFormatOrder) {
        editTextList = when (isDayMonth) {
            DateFormatOrder.MDY -> mutableListOf(birthdayMonth, birthdayDay, birthdayYear)
            DateFormatOrder.YMD -> mutableListOf(birthdayYear, birthdayMonth, birthdayDay)
            else -> mutableListOf(birthdayDay, birthdayMonth, birthdayYear)
        }
    }

    private fun configureDateViewsListeners() {
        configureDateComponent(birthdayDay, DateComponent.DAY) {
            day = it
            onNewDateEntered()
        }
        configureDateComponent(birthdayMonth, DateComponent.MONTH) {
            month = it
            onNewDateEntered()
        }
        configureDateComponent(birthdayYear, DateComponent.YEAR) {
            year = it
            onNewDateEntered()
        }
    }

    private fun focusFirst() {
        editTextList.firstOrNull()?.requestFocus()
        showKeyboard(context)
    }

    private fun setColorForInput(field: EditText, isPassed: Boolean) =
        field.setTextColor(if (isPassed) UIConfig.textPrimaryColor else UIConfig.uiErrorColor)

    private fun focusNext(editText: EditText) {
        val current = editTextList.indexOf(editText)
        editTextList.getOrNull(current + 1)?.requestFocus()
    }

    private fun configureDateComponent(view: EditText, component: DateComponent, onChange: (String) -> Unit) {
        val listener = object : ValidInputListener {
            override fun onValidInput(isValid: Boolean) {
                setColorForInput(view, isValid)
                if (isValid) {
                    focusNext(view)
                }
                onChange(if (isValid) view.text.toString() else "")
            }
        }
        view.addTextChangedListener(TextInputWatcher(listener, component.length, view, component.regex))
    }
}
