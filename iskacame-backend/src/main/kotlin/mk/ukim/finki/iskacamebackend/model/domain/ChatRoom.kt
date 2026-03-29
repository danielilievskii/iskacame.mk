package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity

@Entity
@Table(name = "chat_rooms")
class ChatRoom(

    @OneToOne
    @JoinColumn(name = "gathering_id", unique = true, nullable = false)
    var gathering: Gathering,

) : BaseEntity<Long>()