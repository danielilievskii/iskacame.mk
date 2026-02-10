package mk.ukim.finki.iskacamebackend.model

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity

@Entity
@Table(name = "gathering_place_votes")
class GatheringPlaceVote(
    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User,

    @ManyToOne
    @JoinColumn(name = "gathering_id")
    var gathering: Gathering,

    @ManyToOne
    @JoinColumn(name = "place_id")
    var place: Place
) : BaseEntity<Long>()
