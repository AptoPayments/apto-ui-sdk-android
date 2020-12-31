package com.aptopayments.sdk.utils.chatbot

import org.junit.Assert.*
import org.junit.Test

class SupportTextResolverTest {

    private lateinit var sut: SupportTextResolver

    @Test
    fun `whenever chatbot is not active then correct texts provided`() {
        sut = SupportTextResolver(false)

        val result = sut.getTexts()

        assertEquals("card_settings_help_contact_support_description", result.first)
        assertEquals("card_settings_help_contact_support_title", result.second)
    }

    @Test
    fun `whenever chatbot is active then correct texts provided`() {
        sut = SupportTextResolver(true)

        val result = sut.getTexts()

        assertEquals("card_settings_help_chatbot_title", result.first)
        assertEquals("card_settings_help_chatbot_description", result.second)
    }
}
