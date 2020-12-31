package com.aptopayments.sdk.utils.chatbot

import com.aptopayments.sdk.AndroidTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class ChatbotActivityLauncherTest : AndroidTest() {

    private val chatbotConfigurator: ChatbotConfigurator = mock()

    @Test
    fun `whenever Chatbot is shown then is configured`() {
        val parameters = ChatbotParameters("asd", "11", "22")
        ChatbotActivityLauncher(chatbotConfigurator).show(activity(), parameters)

        verify(chatbotConfigurator).configureChatbot(parameters)
    }
}
