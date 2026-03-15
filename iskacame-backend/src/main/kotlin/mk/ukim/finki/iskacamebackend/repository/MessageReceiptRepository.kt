package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.MessageReceipt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MessageReceiptRepository : JpaRepository<MessageReceipt, Long> {

    @Modifying
    @Query("""
        UPDATE MessageReceipt r
        SET r.status = mk.ukim.finki.iskacamebackend.model.enums.MessageReceiptStatus.DELIVERED,
            r.deliveredAt = CURRENT_TIMESTAMP
        WHERE r.recipient.id = :recipientId
          AND r.message.chatRoom.id = :chatRoomId
          AND r.status = mk.ukim.finki.iskacamebackend.model.enums.MessageReceiptStatus.SENT
    """)
    fun markDeliveredForChatRoom(chatRoomId: Long, recipientId: Long): Int

    @Modifying
    @Query("""
        UPDATE MessageReceipt r
        SET r.status = mk.ukim.finki.iskacamebackend.model.enums.MessageReceiptStatus.SEEN,
            r.seenAt = CURRENT_TIMESTAMP
        WHERE r.recipient.id = :recipientId
          AND r.message.chatRoom.id = :chatRoomId
          AND r.status <> mk.ukim.finki.iskacamebackend.model.enums.MessageReceiptStatus.SEEN
    """)
    fun markSeenForChatRoom(chatRoomId: Long, recipientId: Long): Int
}