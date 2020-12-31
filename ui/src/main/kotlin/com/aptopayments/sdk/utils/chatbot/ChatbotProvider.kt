package com.aptopayments.sdk.utils.chatbot

import zendesk.chat.Chat
import zendesk.chat.ChatConfiguration
import zendesk.chat.ProfileProvider
import zendesk.chat.VisitorInfo

internal interface ChatbotProvider {
    fun provide(): Chat
    fun profileProvider(): ProfileProvider?
    fun provideConfigurationBuilder(): ChatConfiguration.Builder
    fun provideVisitorInfo(name: String): VisitorInfo
}

internal class ChatbotProviderImpl : ChatbotProvider {
    override fun provide() = Chat.INSTANCE
    override fun profileProvider() = Chat.INSTANCE.providers()?.profileProvider()
    override fun provideConfigurationBuilder(): ChatConfiguration.Builder = ChatConfiguration.builder()
    override fun provideVisitorInfo(name: String): VisitorInfo = VisitorInfo.builder().withName(name).build()
}
