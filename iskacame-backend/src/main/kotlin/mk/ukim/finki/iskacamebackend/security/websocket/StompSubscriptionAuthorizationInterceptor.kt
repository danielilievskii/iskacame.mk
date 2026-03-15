package mk.ukim.finki.iskacamebackend.security.websocket

import mk.ukim.finki.iskacamebackend.common.WebSocketExceptionMessages
import mk.ukim.finki.iskacamebackend.security.UserPrincipal
import mk.ukim.finki.iskacamebackend.security.websocket.subscription.SubscriptionAuthorizationHandler
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageDeliveryException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component
import java.security.Principal

/**
 * Interceptor that authorizes STOMP SUBSCRIBE frames.
 *
 * Delegates authorization logic to the appropriate
 * [SubscriptionAuthorizationHandler] based on the destination.
 *
 * If no handler supports the destination or authorization fails,
 * the subscription is rejected.
 */
@Component
class StompSubscriptionAuthorizationInterceptor(
    private val subscriptionHandlers: List<SubscriptionAuthorizationHandler>
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return message

        if (accessor.command == StompCommand.SUBSCRIBE) {
            val userId = extractUserId(accessor.user!!)

            val destination = accessor.destination
                ?: throw MessageDeliveryException(WebSocketExceptionMessages.DESTINATION_REQUIRED)

            val handler = subscriptionHandlers.find { it.supports(destination) }
                ?: throw MessageDeliveryException(WebSocketExceptionMessages.DESTINATION_UNKNOWN(destination))

            if (!handler.authorize(destination, userId)) {
                throw MessageDeliveryException(WebSocketExceptionMessages.SUBSCRIPTION_FORBIDDEN)
            }
        }

        return message
    }

    private fun extractUserId(principal: Principal): Long {
        val auth = principal as UsernamePasswordAuthenticationToken
        val userPrincipal = auth.principal as UserPrincipal
        return userPrincipal.id
    }
}