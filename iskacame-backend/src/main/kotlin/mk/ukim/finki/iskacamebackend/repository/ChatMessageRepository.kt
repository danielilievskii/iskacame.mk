package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.ChatMessage
import mk.ukim.finki.iskacamebackend.model.projections.ChatRoomUnseenMessagesCount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {

    @EntityGraph(attributePaths = ["receipts"])
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE m.chatRoom.id = :chatRoomId AND m.deletedAt IS NULL
        ORDER BY m.sentAt DESC
    """)
    fun findByChatRoomId(chatRoomId: Long, pageable: Pageable): Page<ChatMessage>

//    @Query(
//        """
//        SELECT m.chatRoom.id as chatRoomId, COUNT(m) as unseenMessagesCount
//        FROM ChatMessage m
//        JOIN ChatRoomReceipt r ON r.lastSeenMessage = m
//        WHERE m.chatRoom.id IN :chatRoomIds
//          AND r.user.id = :userId
//          AND r.status <> mk.ukim.finki.iskacamebackend.model.enums.MessageReceiptStatus.SEEN
//          AND m.deletedAt IS NULL
//        GROUP BY m.chatRoom.id
//    """
//    )
//    fun countUnseenForUserGrouped(chatRoomIds: List<Long>, userId: Long): List<ChatRoomUnseenMessagesCount>

    @Query("""
        SELECT m FROM ChatMessage m
        WHERE m.chatRoom.id = :chatRoomId AND m.deletedAt IS NULL
        ORDER BY m.sentAt DESC
    """)
    fun findFirstByChatRoomIdOrderBySentAtDesc(chatRoomId: Long): ChatMessage?

}