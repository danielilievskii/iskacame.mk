package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity

@Entity
@Table(name = "chat_messages")
class ChatMessage(
    @Column(name = "content")
    var content: String,

    @ManyToOne
    @JoinColumn(name = "sender_id")
    var sender: User,

    @ManyToOne
    @JoinColumn(name = "gathering_id")
    var gathering: Gathering
) : BaseEntity<Long>()
