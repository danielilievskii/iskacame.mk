package mk.ukim.finki.iskacamebackend.security.websocket.subscription

/**
 * Strategy interface for authorizing STOMP subscriptions.
 *
 * Each implementation is responsible for validating access
 * to one or more destination patterns.
 *
 * The [StompSubscriptionAuthorizationInterceptor] delegates
 * subscription checks to the handler that [supports] the
 * requested destination.
 */
interface SubscriptionAuthorizationHandler {

    /**
     * Returns true if this handler is responsible for authorizing the given [destination].
     *
     * @param destination the STOMP destination string (e.g. "/topic/chat.1")
     */
    fun supports(destination: String): Boolean

    /**
     * Returns true if the user with the given [userId] is allowed to subscribe to [destination].
     * Only called when [supports] returns true.
     *
     * @param destination the STOMP destination string
     * @param userId the ID of the authenticated user attempting to subscribe
     * @throws MessageDeliveryException if access is denied (alternatively return false)
     */
    fun authorize(destination: String, userId: Long): Boolean
}