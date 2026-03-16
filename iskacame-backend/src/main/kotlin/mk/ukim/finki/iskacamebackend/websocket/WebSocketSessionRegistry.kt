package mk.ukim.finki.iskacamebackend.websocket

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks active WebSocket sessions per user.
 *
 * In-memory only. Should be replaced with Redis for distributed deployments.
 */
@Component
class WebSocketSessionRegistry {

    private val sessions = ConcurrentHashMap<Long, MutableSet<String>>()

    /**
     * Registers a session for the user.
     *
     * @param userId the ID of the authenticated user
     * @param sessionId the session ID assigned by Spring for this connection
     */
    fun registerSession(userId: Long, sessionId: String) {
        sessions.getOrPut(userId) { ConcurrentHashMap.newKeySet() }.add(sessionId)
    }

    /**
     * Removes a specific session for user.
     *
     * @param userId the ID of the authenticated user
     * @param sessionId the session ID to remove
     */
    fun unregisterSession(userId: Long, sessionId: String) {

        val userSessions = sessions[userId] ?: return

        userSessions.remove(sessionId)

        if (userSessions.isEmpty()) {
            sessions.remove(userId)
        }
    }

    /**
     * Returns true if the user has at least one active session.
     *
     * @param userId the ID of the user to check
     * @return true if connected, false if offline or app is closed
     */
    fun isConnected(userId: Long): Boolean = sessions[userId]?.isNotEmpty() == true
}