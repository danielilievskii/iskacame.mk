package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity
import mk.ukim.finki.iskacamebackend.model.enums.MessageReceiptStatus
import java.time.LocalDateTime

@Entity
@Table(
    name = "message_receipts",
    uniqueConstraints = [UniqueConstraint(columnNames = ["message_id", "recipient_id"])]
)
class MessageReceipt(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    var message: ChatMessage,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    var recipient: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: MessageReceiptStatus,

    @Column(name = "delivered_at")
    var deliveredAt: LocalDateTime? = null,

    @Column(name = "seen_at")
    var seenAt: LocalDateTime? = null,
) : BaseEntity<Long>()