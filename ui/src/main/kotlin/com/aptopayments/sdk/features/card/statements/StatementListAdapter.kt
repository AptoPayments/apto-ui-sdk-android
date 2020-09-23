package com.aptopayments.sdk.features.card.statements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aptopayments.mobile.data.statements.StatementMonth
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.monthLocalized
import com.aptopayments.sdk.ui.views.SectionOptionWithSubtitleView
import kotlinx.android.synthetic.main.view_section_header_element.view.*

internal class StatementListAdapter(val delegate: Delegate) :
    RecyclerView.Adapter<StatementListAdapter.BaseViewHolder>() {

    interface Delegate {
        fun onMonthTapped(month: StatementMonth)
    }

    private var list = listOf<StatementListItem>()

    fun setData(list: List<StatementListItem>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = list[position].itemType()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View

        return when (viewType) {
            StatementListItem.YEAR_VIEW_TYPE -> {
                view = inflater.inflate(R.layout.view_section_header_element, parent, false)
                ViewHolderYear(view)
            }
            StatementListItem.MONTH_VIEW_TYPE -> {
                view = inflater.inflate(
                    R.layout.view_section_option_subtitle_element,
                    parent,
                    false
                ) as (SectionOptionWithSubtitleView)
                ViewHolderMonth(view)
            }
            else -> throw IllegalArgumentException("Unexpected view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    abstract inner class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: StatementListItem)
    }

    inner class ViewHolderMonth(val view: SectionOptionWithSubtitleView) : BaseViewHolder(view) {

        override fun bind(item: StatementListItem) {
            val monthStatement = (item as StatementListItem.MonthRow).month

            view.setOnClickListener { delegate.onMonthTapped(monthStatement) }
            view.optionTitle = monthStatement.toLocalDate().monthLocalized()
        }
    }

    inner class ViewHolderYear(val view: View) : BaseViewHolder(view) {

        override fun bind(item: StatementListItem) {
            view.header_item.title = (item as StatementListItem.YearRow).year.toString()
        }
    }
}
