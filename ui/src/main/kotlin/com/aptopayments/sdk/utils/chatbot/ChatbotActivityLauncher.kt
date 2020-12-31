package com.aptopayments.sdk.utils.chatbot

import androidx.fragment.app.FragmentActivity
import zendesk.chat.ChatEngine
import zendesk.messaging.MessagingActivity

internal class ChatbotActivityLauncher(private val chatbotConfigurator: ChatbotConfigurator) {

    fun show(activity: FragmentActivity, chatbotParameters: ChatbotParameters) {
        val config = chatbotConfigurator.configureChatbot(chatbotParameters)

        MessagingActivity.builder()
            .withEngines(ChatEngine.engine())
            .show(activity, config)
    }
}
