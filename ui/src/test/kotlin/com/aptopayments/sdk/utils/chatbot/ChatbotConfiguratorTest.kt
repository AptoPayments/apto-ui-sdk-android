package com.aptopayments.sdk.utils.chatbot

import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import zendesk.chat.ChatConfiguration
import zendesk.chat.ProfileProvider
import zendesk.chat.VisitorInfo

private const val NAME = "Jhon Snow"
private const val CARD_ID = "1234"
private const val CARD_PRODUCT_ID = "5678"

class ChatbotConfiguratorTest {
    private val visitorInfo: VisitorInfo = mock()
    private val configuration: ChatConfiguration = mock()
    private val configurationBuilder: ChatConfiguration.Builder = mock()
    private val profileProvider: ProfileProvider = mock()
    private val chatbotProvider: ChatbotProvider = mock()

    private val sut = ChatbotConfigurator(chatbotProvider)

    @Before
    fun setUp() {
        whenever(configurationBuilder.build()).thenReturn(configuration)
        whenever(configurationBuilder.withTranscriptEnabled(any())).thenReturn(configurationBuilder)
        whenever(configurationBuilder.withAgentAvailabilityEnabled(any())).thenReturn(configurationBuilder)
        whenever(configurationBuilder.withPreChatFormEnabled(any())).thenReturn(configurationBuilder)
        whenever(chatbotProvider.provideVisitorInfo(any())).thenReturn(visitorInfo)
        whenever(chatbotProvider.provideConfigurationBuilder()).thenReturn(configurationBuilder)
        whenever(chatbotProvider.profileProvider()).thenReturn(profileProvider)
    }

    @Test
    fun `instance was configured correctly`() {
        sut.configureChatbot(ChatbotParameters(name = NAME, cardId = CARD_ID, cardProductId = CARD_PRODUCT_ID))

        verify(configurationBuilder).withTranscriptEnabled(false)
        verify(configurationBuilder).withAgentAvailabilityEnabled(false)
        verify(configurationBuilder).withPreChatFormEnabled(false)
    }

    @Test
    fun `name was added to the profile`() {
        sut.configureChatbot(ChatbotParameters(name = NAME, cardId = CARD_ID, cardProductId = CARD_PRODUCT_ID))

        verify(chatbotProvider).provideVisitorInfo(NAME)
        verify(profileProvider).setVisitorInfo(eq(visitorInfo), anyOrNull())
    }

    @Test
    fun `note was added to the profile`() {
        sut.configureChatbot(ChatbotParameters(name = NAME, cardId = CARD_ID, cardProductId = CARD_PRODUCT_ID))

        verify(profileProvider).setVisitorNote("CardId: $CARD_ID, CardProductId: $CARD_PRODUCT_ID", null)
    }
}
