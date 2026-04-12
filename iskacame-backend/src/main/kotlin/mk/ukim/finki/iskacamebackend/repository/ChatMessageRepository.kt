package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import java.time.LocalDateTime

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {

    @EntityGraph(attributePaths = ["receipts"])
    @Query("""
        SELECT m FROM ChatMessage m
        WHERE m.chatRoom.id = :chatRoomId AND m.deletedAt IS NULL
        ORDER BY m.sentAt DESC
    """)
    fun findByChatRoomId(chatRoomId: Long, pageable: Pageable): Page<ChatMessage>

    fun countByChatRoomIdAndDeletedAtIsNull(chatRoomId: Long): Long

    @Query("""
        SELECT m FROM ChatMessage m
        WHERE m.chatRoom.id = :chatRoomId AND m.deletedAt IS NULL
        ORDER BY m.sentAt DESC
        LIMIT 1
    """)
    fun findFirstByChatRoomIdOrderBySentAtDesc(chatRoomId: Long): ChatMessage?

    fun findTopByChatRoomIdAndSentAtBeforeAndDeletedAtIsNullOrderBySentAtDesc(
        chatRoomId: Long,
        sentAt: LocalDateTime
    ): ChatMessage?


}