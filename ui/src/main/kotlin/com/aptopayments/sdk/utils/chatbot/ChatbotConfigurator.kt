package com.aptopayments.sdk.utils.chatbot

import zendesk.chat.ChatConfiguration

internal class ChatbotConfigurator(
    private val chatbotProvider: ChatbotProvider
) {

    fun configureChatbot(chatbotParameters: ChatbotParameters): ChatConfiguration {
        addData(chatbotParameters)
        return getConfiguration()
    }

    private fun getConfiguration(): ChatConfiguration {
        return chatbotProvider.provideConfigurationBuilder()
            .withTranscriptEnabled(false)
            .withAgentAvailabilityEnabled(false)
            .withPreChatFormEnabled(false)
            .build()
    }

    private fun addData(param: ChatbotParameters) {
        addVisitorNote(param)
        addVisitorInfoData(param)
    }

    private fun addVisitorNote(param: ChatbotParameters) {
        val note = "CardId: ${param.cardId}, CardProductId: ${param.cardProductId}"
        chatbotProvider.profileProvider()?.setVisitorNote(note, null)
    }

    private fun addVisitorInfoData(param: ChatbotParameters) {
        val visitorInfo = chatbotProvider.provideVisitorInfo(param.name)
        chatbotProvider.profileProvider()?.setVisitorInfo(visitorInfo, null)
    }
}

data class ChatbotParameters(val name: String, val cardId: String, val cardProductId: String? = null)
