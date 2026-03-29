package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.converter.ChatMessageEncryptConverter
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity
import java.time.LocalDateTime

@Entity
@Table(name = "chat_messages")
class ChatMessage(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    var chatRoom: ChatRoom,

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    var sender: User,

    @Convert(converter = ChatMessageEncryptConverter::class)
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(name = "sent_at", nullable = false)
    var sentAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "lastSeenMessage", cascade = [CascadeType.ALL], orphanRemoval = true)
    var receipts: MutableList<ChatRoomReceipt> = mutableListOf()

) : BaseEntity<Long>()
