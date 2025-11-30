package mk.ukim.finki.iskacamebackend.model

import User
import jakarta.persistence.*

@Entity
@Table(name = "chat_messages")
class ChatMessage(
    @Column(name = "content")
    val content: String,

    @ManyToOne
    @JoinColumn(name = "sender_id")
    val sender: User,

    @ManyToOne
    @JoinColumn(name = "gathering_id")
    val gathering: Gathering
) : BaseEntity<Long>()
