package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.ChatRoomReceipt
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatRoomReceiptRepository : JpaRepository<ChatRoomReceipt, Long> {

    fun findByChatRoomIdAndUserId(chatRoomId: Long, userId: Long): ChatRoomReceipt?
}