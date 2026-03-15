package mk.ukim.finki.iskacamebackend.model.projections

interface ChatRoomUnseenMessagesCount {
    val chatRoomId: Long
    val unseenMessagesCount: Long
}