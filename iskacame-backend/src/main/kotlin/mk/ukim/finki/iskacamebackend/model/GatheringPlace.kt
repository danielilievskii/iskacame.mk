package mk.ukim.finki.iskacamebackend.model

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity

@Entity
@Table(name = "gathering_places")
class GatheringPlace(
    @ManyToOne
    @JoinColumn(name = "gathering_id")
    var gathering: Gathering,

    @ManyToOne
    @JoinColumn(name = "place_id")
    var place: Place,

    @ManyToOne
    @JoinColumn(name = "suggested_by_user_id")
    var suggestedBy: User
) : BaseEntity<Long>()