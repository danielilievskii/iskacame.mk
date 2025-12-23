package mk.ukim.finki.iskacamebackend.model

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity

@Entity
@Table(name = "chat_images")
class ChatImage(
    @Column(name = "format")
    var format: String,

    @Lob
    @Column(name = "image")
    var image: ByteArray,

    @ManyToOne
    @JoinColumn(name = "sender_id")
    var sender: User,

    @ManyToOne
    @JoinColumn(name = "gathering_id")
    var gathering: Gathering
) : BaseEntity<Long>()