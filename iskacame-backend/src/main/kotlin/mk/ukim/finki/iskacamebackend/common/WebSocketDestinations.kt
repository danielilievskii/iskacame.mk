package mk.ukim.finki.iskacamebackend.common

object WebSocketDestinations {
    fun chatTopic(chatRoomId: Long) = "/topic/chat.$chatRoomId"
    fun chatReceiptsTopic(chatRoomId: Long) = "/topic/chat.$chatRoomId.receipts"
}