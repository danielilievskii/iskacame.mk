package mk.ukim.finki.iskacamebackend.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.google.genai.GoogleGenAiChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatClientConfig {

    @Bean
    fun geminiChatClient(chatModel: GoogleGenAiChatModel): ChatClient {
        return ChatClient.builder(chatModel)
            .defaultAdvisors(SimpleLoggerAdvisor())
            .build()
    }
}