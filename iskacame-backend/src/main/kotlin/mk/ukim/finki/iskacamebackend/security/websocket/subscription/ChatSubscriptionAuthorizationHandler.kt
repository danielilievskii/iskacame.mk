package mk.ukim.finki.iskacamebackend.security.websocket.subscription

import mk.ukim.finki.iskacamebackend.service.intf.PermissionService
import org.springframework.stereotype.Component

/**
 * Implementation of the SubscriptionAuthorizationHandler.
 *
 * Handles subscription authorization for chat room destinations.
 * Supports destinations matching /topic/chat.{chatRoomId} and /topic/chat.{chatRoomId}.receipts.
 *
 * @see SubscriptionAuthorizationHandler
 */
@Component
class ChatSubscriptionAuthorizationHandler(
    private val permissionService: PermissionService
) : SubscriptionAuthorizationHandler {

    // handles /topic/chat.{id} and /topic/chat.{id}.receipts
    private val pattern = Regex("^/topic/chat\\.(\\d+)")

    override fun supports(destination: String) = pattern.containsMatchIn(destination)

    override fun authorize(destination: String, userId: Long): Boolean {

        val matchResult = pattern.find(destination) ?: return false
        val chatRoomIdString = matchResult.groupValues.getOrNull(1) ?: return false
        val chatRoomId = chatRoomIdString.toLong()

        return permissionService.isGatheringParticipantByChatRoom(chatRoomId, userId)
    }
}