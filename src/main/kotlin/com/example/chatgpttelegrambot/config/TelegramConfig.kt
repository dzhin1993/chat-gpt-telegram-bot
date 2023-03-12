package com.example.chatgpttelegrambot.config

import com.aallam.openai.client.OpenAI
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TelegramConfig(
    @Value("\${open-ai.api-key}")
    private val apiKey: String,
) {
    @Bean
    fun getOpenAIClient(): OpenAI {
        return OpenAI(apiKey);
    }
}