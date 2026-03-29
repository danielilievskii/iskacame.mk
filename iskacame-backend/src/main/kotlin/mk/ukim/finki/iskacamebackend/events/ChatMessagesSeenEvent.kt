package mk.ukim.finki.iskacamebackend.events

data class ChatMessagesSeenEvent(
    val chatRoomId: Long,
    val recipientId: Long
)