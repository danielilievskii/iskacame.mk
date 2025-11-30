package mk.ukim.finki.iskacamebackend.model

import User
import jakarta.persistence.*

@Entity
@Table(name = "gathering_place_votes")
class GatheringPlaceVote(
    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne
    @JoinColumn(name = "gathering_id")
    val gathering: Gathering,

    @ManyToOne
    @JoinColumn(name = "place_id")
    val place: Place
) : BaseEntity<Long>()
