package mk.ukim.finki.iskacamebackend.common

/**
 * Definitions for web socket exception error messages.
 */
object WebSocketExceptionMessages {

    const val MISSING_OR_INVALID_AUTH_HEADER = "Missing or invalid Authorization header"
    const val INVALID_OR_EXPIRED_TOKEN = "Invalid or expired token"
    const val AUTHENTICATION_REQUIRED = "You must be logged in to perform this action."
    const val DESTINATION_REQUIRED = "STOMP destination header is required."
    const val SUBSCRIPTION_FORBIDDEN = "You do not have permission to subscribe to this channel."

    fun DESTINATION_UNKNOWN(destination: String) = "Unknown subscription destination: $destination."
}