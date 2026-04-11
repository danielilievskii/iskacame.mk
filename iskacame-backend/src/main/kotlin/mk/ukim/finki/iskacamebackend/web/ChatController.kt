package mk.ukim.finki.iskacamebackend.web


import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import mk.ukim.finki.iskacamebackend.dto.response.chat.ChatMessageDto
import mk.ukim.finki.iskacamebackend.service.intf.ChatService
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "Operations for managing chat rooms, messages, and receipts")
class ChatController(
    private val chatService: ChatService
) {

    @GetMapping("/room/{chatRoomId}/messages")
    @Operation(summary = "Get chat room messages")
    fun getChatRoomMessages(
        @PathVariable chatRoomId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "30") size: Int
    ): ResponseEntity<Page<ChatMessageDto>> {

        val page = chatService.getChatRoomMessages(chatRoomId, page, size)
        return ResponseEntity.ok(page)
    }

    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "Delete chat room message")
    fun deleteChatRoomMessage(@PathVariable messageId: Long): ResponseEntity<Void> {

        chatService.deleteMessage(messageId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/room/{chatRoomId}/seen")
    @Operation(summary = "Mark chat room messages as seen")
    fun markChatRoomMessagesSeen(
        @PathVariable chatRoomId: Long,
    ): ResponseEntity<Void> {

        chatService.markChatRoomMessagesSeen(chatRoomId)
        return ResponseEntity.noContent().build()
    }
}