package com.aptopayments.sdk.features.managecard

import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.features.card.cardsettings.CardSettingsContract

internal class CardSettingsFragmentDouble(override var delegate: CardSettingsContract.Delegate?) :
        BaseFragment(),
        CardSettingsContract.View
{
    override fun layoutId(): Int = 0
    override fun backgroundColor() = 0
    override fun setupViewModel() {}
    override fun setupUI() {}
}
