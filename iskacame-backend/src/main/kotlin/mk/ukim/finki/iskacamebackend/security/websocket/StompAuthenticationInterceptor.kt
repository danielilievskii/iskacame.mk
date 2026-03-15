package mk.ukim.finki.iskacamebackend.security.websocket

import mk.ukim.finki.iskacamebackend.common.JWTConstants
import mk.ukim.finki.iskacamebackend.common.WebSocketExceptionMessages
import mk.ukim.finki.iskacamebackend.security.CustomUserDetailsService
import mk.ukim.finki.iskacamebackend.security.JwtService
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageDeliveryException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component

/**
 * Interceptor that enforces authentication on STOMP frames.
 *
 * On CONNECT - extracts and validates the JWT from the Authorization header,
 * then attaches the authenticated user to the STOMP session.
 *
 * On SEND - rejects the frame if no authenticated user is present in the session.
 */
@Component
class StompAuthenticationInterceptor(
    private val jwtService: JwtService,
    private val customUserDetailsService: CustomUserDetailsService
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return message

        when (accessor.command) {
            StompCommand.CONNECT -> {
                val authHeader = accessor.getFirstNativeHeader(JWTConstants.BEARER_TOKEN_HEADER)

                if (authHeader.isNullOrBlank() || !authHeader.startsWith(JWTConstants.TOKEN_PREFIX))
                    throw MessageDeliveryException(WebSocketExceptionMessages.MISSING_OR_INVALID_AUTH_HEADER)

                val token = authHeader.substring(7)

                try {
                    jwtService.validateToken(token)
                    val email = jwtService.getEmailFromToken(token)
                    val userDetails = customUserDetailsService.loadUserByUsername(email)

                    accessor.user = UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.authorities
                    )
                } catch (ex: Exception) {
                    throw MessageDeliveryException(ex.message.toString())
                }
            }

            StompCommand.SEND, StompCommand.SUBSCRIBE -> {
                accessor.user ?: throw MessageDeliveryException(WebSocketExceptionMessages.AUTHENTICATION_REQUIRED)
            }

            else -> Unit
        }

        return message
    }
}