package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity

@Entity
@Table(
    name = "chat_room_receipts",
    uniqueConstraints = [UniqueConstraint(columnNames = ["chatroom_id", "user_id"])]
)
class ChatRoomReceipt(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", nullable = false)
    var chatRoom: ChatRoom,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_seen_message_id", nullable = true)
    var lastSeenMessage: ChatMessage?,

    var unseenMessagesCounter: Long = 0,

    ) : BaseEntity<Long>()