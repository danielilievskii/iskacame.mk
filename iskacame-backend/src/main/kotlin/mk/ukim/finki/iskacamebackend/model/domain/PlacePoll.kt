package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity
import mk.ukim.finki.iskacamebackend.model.enums.PollStatus
import java.time.Instant

@Entity
@Table(name = "place_polls")
class PlacePoll(
    @ManyToOne
    @JoinColumn(name = "gathering_id")
    var gathering: Gathering,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: PollStatus = PollStatus.ACTIVE,

    @Column(name = "ends_at")
    var endsAt: Instant
) : BaseEntity<Long>()
