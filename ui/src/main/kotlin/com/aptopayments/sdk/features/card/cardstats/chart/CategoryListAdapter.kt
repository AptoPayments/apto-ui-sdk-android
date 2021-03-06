package com.aptopayments.sdk.features.card.cardstats.chart

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.stats.CategorySpending
import com.aptopayments.mobile.data.transaction.MCC
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.iconResource
import com.aptopayments.sdk.core.extension.setBackgroundColorKeepShape
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.extensions.setColorFilterCompat
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe

internal class CategoryListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface Delegate {
        fun onCategoryTapped(categorySpending: CategorySpending)
    }

    var delegate: Delegate? = null
    var categorySpendingList: List<CategoryListItem>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(
            inflater.inflate(R.layout.view_category_row, parent, false)
        )
    }

    override fun getItemCount(): Int = categorySpendingList?.size ?: 0

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        categorySpendingList?.get(position)?.let { lineItem ->
            val mcc = MCC(lineItem.categorySpending.categoryId)
            val icon =
                ContextCompat.getDrawable(holder.icon.context, mcc.iconResource)?.constantState?.newDrawable()?.mutate()
            if (lineItem.isSelected) {
                icon?.setColorFilterCompat(UIConfig.uiTertiaryColor, PorterDuff.Mode.SRC_ATOP)
                viewHolder.icon.setBackgroundColorKeepShape(UIConfig.uiPrimaryColor)
            } else {
                icon?.setColorFilterCompat(UIConfig.iconSecondaryColor, PorterDuff.Mode.SRC_ATOP)
                viewHolder.icon.setBackgroundColorKeepShape(UIConfig.uiTertiaryColor)
            }
            viewHolder.icon.setImageDrawable(icon)
            viewHolder.category.text = mcc.toLocalizedString()
            viewHolder.amount.text = lineItem.categorySpending.spending?.toAbsString()
            viewHolder.row.setOnClickListenerSafe { delegate?.onCategoryTapped(lineItem.categorySpending) }
        }
    }

    fun selectCategorySpending(spending: CategorySpending) =
        categorySpendingList?.forEach { if (it.categorySpending == spending) it.isSelected = true }

    fun clearSelectedCategory() = categorySpendingList?.forEach { it.isSelected = false }

    private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icon: ImageView = itemView.findViewById(R.id.iv_category_icon)
        var category: TextView = itemView.findViewById(R.id.tv_category_description)
        var amount: TextView = itemView.findViewById(R.id.tv_category_amount)
        var row: LinearLayout = itemView.findViewById(R.id.ll_category_row)

        init {
            with(themeManager()) {
                customizeMainItem(category)
                customizeAmountSmall(amount)
            }
        }
    }
}
