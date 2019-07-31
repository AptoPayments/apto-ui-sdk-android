package com.aptopayments.sdk.core.data.transaction

import com.aptopayments.sdk.R
import com.aptopayments.core.data.transaction.MCC
import com.aptopayments.core.data.transaction.MCC.Icon

private val icons: Map<Icon, Int> = hashMapOf(
        Icon.PLANE to R.drawable.ic_flights,
        Icon.CAR to R.drawable.ic_car,
        Icon.GLASS to R.drawable.ic_alcohol,
        Icon.FINANCE to R.drawable.ic_withdraw,
        Icon.FOOD to R.drawable.ic_food,
        Icon.GAS to R.drawable.ic_fuel,
        Icon.BED to R.drawable.ic_hotel,
        Icon.MEDICAL to R.drawable.ic_medicine,
        Icon.CAMERA to R.drawable.ic_other,
        Icon.CARD to R.drawable.ic_bank_card,
        Icon.CART to R.drawable.ic_purchases,
        Icon.ROAD to R.drawable.ic_toll_road,
        Icon.OTHER to R.drawable.ic_other
    )

val MCC.iconResource: Int
    get() = icons[icon] ?: R.drawable.ic_other
