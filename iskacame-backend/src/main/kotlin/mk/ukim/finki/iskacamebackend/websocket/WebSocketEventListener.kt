package mk.ukim.finki.iskacamebackend.websocket

import mk.ukim.finki.iskacamebackend.utils.extractUserId
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

/**
 * Syncs [WebSocketSessionRegistry] with Spring's WebSocket lifecycle events.
 */
@Component
class WebSocketEventListener(
    private val sessionRegistry: WebSocketSessionRegistry
) {

    /**
     * Invoked by Spring after a STOMP CONNECT frame has been fully processed
     * and the session is established.
     *
     * Extracts the user and session ID, and registers the session.
     */
    @EventListener
    fun onSessionConnected(event: SessionConnectedEvent) {

        val accessor = StompHeaderAccessor.wrap(event.message)

        val userId = accessor.user?.extractUserId() ?: return
        val sessionId = accessor.sessionId ?: return

        sessionRegistry.registerSession(userId, sessionId)

    }

    /**
     * Invoked by Spring when a WebSocket session ends — either because the
     * client sent a STOMP DISCONNECT frame, the connection dropped, or the
     * app was closed.
     *
     * Extracts the user and session ID, and unregisters the session
     * so the user is no longer considered online.
     */
    @EventListener
    fun onSessionDisconnect(event: SessionDisconnectEvent) {

        val accessor = StompHeaderAccessor.wrap(event.message)

        val userId = accessor.user?.extractUserId() ?: return
        val sessionId = accessor.sessionId ?: return

        sessionRegistry.unregisterSession(userId, sessionId)
    }
}