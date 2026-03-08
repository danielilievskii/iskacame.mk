package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity
import mk.ukim.finki.iskacamebackend.model.enums.ParticipationStatus

@Entity
@Table(
    name = "gathering_participation",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "gathering_id"])
    ]
)
class GatheringParticipation(
    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User,

    @ManyToOne
    @JoinColumn(name = "gathering_id")
    var gathering: Gathering,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: ParticipationStatus = ParticipationStatus.INVITED
) : BaseEntity<Long>()