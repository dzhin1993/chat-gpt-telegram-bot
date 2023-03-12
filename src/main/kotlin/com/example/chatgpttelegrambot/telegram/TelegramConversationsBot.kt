package com.example.chatgpttelegrambot.telegram

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Component
final class TelegramConversationsBot(
    @Value("\${telegram.bot-token}")
    private val token: String,
    @Value("\${telegram.bot-name}")
    private val botName: String,
    private val openAI: OpenAI
) : TelegramLongPollingBot(token) {

    init {
        val botsApi =  TelegramBotsApi(DefaultBotSession::class.java)
        botsApi.registerBot(this)
    }

    override fun getBotUsername(): String {
        return botName
    }

    override fun onUpdateReceived(update: Update?) {
        val chatId = update?.message?.chatId
        val text = update?.message?.text
        if (chatId != null && text != null) {
            val response = runBlocking { getOpenAIResponse(text) }
            execute(response?.let { SendMessage(chatId.toString(), it) })
        }
    }

    @OptIn(BetaOpenAI::class)
    private suspend fun getOpenAIResponse(text: String): String? {
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = text
                )
            ),
            temperature = 0.7,
            maxTokens = 256,
            topP = 1.0
        )
        val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)
        val chatChoice = completion.choices[0]
        return chatChoice.message?.content
    }
}
