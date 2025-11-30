package mk.ukim.finki.iskacamebackend.model

import User
import jakarta.persistence.*

@Entity
@Table(name = "chat_images")
class ChatImage(
    @Column(name = "format")
    val format: String,

    @Lob
    @Column(name = "image")
    val image: ByteArray,

    @ManyToOne
    @JoinColumn(name = "sender_id")
    val sender: User,

    @ManyToOne
    @JoinColumn(name = "gathering_id")
    val gathering: Gathering
) : BaseEntity<Long>()
