package mk.ukim.finki.iskacamebackend.web.ws

import mk.ukim.finki.iskacamebackend.dto.request.chat.SendMessageRequest
import mk.ukim.finki.iskacamebackend.security.UserPrincipal
import mk.ukim.finki.iskacamebackend.service.intf.ChatService
import org.springframework.messaging.handler.annotation.*
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller

@Controller
class ChatWebSocketController(
    private val chatService: ChatService
) {

    @MessageMapping("/chat.{chatRoomId}.send")
    fun sendMessage(
        @DestinationVariable chatRoomId: Long,
        @Payload request: SendMessageRequest,
        authentication: Authentication
    ) {
        val userPrincipal = authentication.principal as UserPrincipal
        chatService.sendMessage(chatRoomId, request, userPrincipal.id)
    }
}