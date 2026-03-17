package mk.ukim.finki.iskacamebackend.websocket

import jakarta.annotation.PostConstruct
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

/**
 * Registry for tracking active WebSocket sessions per user.
 *
 * Note: On application startup, all existing session keys are cleared
 * to avoid stale data from previous runs.
 */
@Component
class WebSocketSessionRegistry(
    private val redis: StringRedisTemplate
) {

    companion object {
        private const val KEY_PREFIX = "ws:sessions:"
    }

    @PostConstruct
    fun clearStaleSessions() {
        redis.keys("$KEY_PREFIX*").forEach { redis.delete(it) }
    }

    private fun key(userId: Long) = "$KEY_PREFIX$userId"

    /**
     * Register a new WebSocket session for the given user.
     */
    fun registerSession(userId: Long, sessionId: String) {
        redis.opsForSet().add(key(userId), sessionId)
    }

    /**
     * Unregister a WebSocket session for the given user.
     */
    fun unregisterSession(userId: Long, sessionId: String) {
        redis.opsForSet().remove(key(userId), sessionId)
    }

    /**
     * Check if the user currently has any active WebSocket sessions.
     */
    fun isConnected(userId: Long): Boolean =
        (redis.opsForSet().size(key(userId)) ?: 0) > 0
}