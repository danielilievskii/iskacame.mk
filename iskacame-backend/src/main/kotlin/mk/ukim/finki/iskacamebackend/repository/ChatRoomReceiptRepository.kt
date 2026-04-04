package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.ChatRoomReceipt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChatRoomReceiptRepository : JpaRepository<ChatRoomReceipt, Long> {

    fun findByChatRoomIdAndUserId(chatRoomId: Long, userId: Long): ChatRoomReceipt?

    fun findByChatRoomIdAndUserIdIn(chatRoomId: Long, userIds: List<Long>): List<ChatRoomReceipt>

    @Query("""
        SELECT r FROM ChatRoomReceipt r
        WHERE r.chatRoom.id IN :chatRoomIds AND r.user.id = :userId
    """)
    fun findByChatRoomIdsAndUserId(chatRoomIds: List<Long>, userId: Long): List<ChatRoomReceipt>

    fun findByChatRoomIdAndUserIdNot(chatRoomId: Long, userId: Long): List<ChatRoomReceipt>

}