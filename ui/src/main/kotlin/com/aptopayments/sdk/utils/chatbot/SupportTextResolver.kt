package com.aptopayments.sdk.utils.chatbot

internal class SupportTextResolver(private val isChatbotActive: Boolean) {

    fun getTexts(): Pair<String, String> {
        return if (isChatbotActive) {
            Pair("card_settings_help_chatbot_title", "card_settings_help_chatbot_description")
        } else {
            Pair("card_settings_help_contact_support_description", "card_settings_help_contact_support_title")
        }
    }
}
