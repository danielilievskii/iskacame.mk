package mk.ukim.finki.iskacamebackend.config

import mk.ukim.finki.iskacamebackend.security.websocket.StompAuthenticationInterceptor
import mk.ukim.finki.iskacamebackend.security.websocket.StompSubscriptionAuthorizationInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    @Value("\${rabbitmq.host}")
    private val rabbitHost: String,

    @Value("\${rabbitmq.port}")
    private val rabbitPort: Int,

    @Value("\${rabbitmq.username}")
    private val rabbitUsername: String,

    @Value("\${rabbitmq.password}")
    private val rabbitPassword: String,

    private val stompAuthenticationInterceptor: StompAuthenticationInterceptor,
    private val stompSubscriptionAuthorizationInterceptor: StompSubscriptionAuthorizationInterceptor
) : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableStompBrokerRelay("/topic", "/queue")
            .setRelayHost(rabbitHost)
            .setRelayPort(rabbitPort)
            .setClientLogin(rabbitUsername)
            .setClientPasscode(rabbitPassword)
            .setSystemLogin(rabbitUsername)
            .setSystemPasscode(rabbitPassword)

        registry.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
            .addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(
            stompAuthenticationInterceptor,
            stompSubscriptionAuthorizationInterceptor
        )
    }
}