package mk.ukim.finki.iskacamebackend.events

data class ChatMessageDeletedEvent(
    val chatRoomId: Long,
    val messageId: Long
)
