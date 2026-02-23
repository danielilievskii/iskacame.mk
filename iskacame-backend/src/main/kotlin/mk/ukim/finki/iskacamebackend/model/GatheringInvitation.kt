package mk.ukim.finki.iskacamebackend.model

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity
import mk.ukim.finki.iskacamebackend.model.enums.InviteStatus

@Entity
@Table(
    name = "gathering_invitation",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "gathering_id"])
    ]
)
class GatheringInvitation(
    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User,

    @ManyToOne
    @JoinColumn(name = "gathering_id")
    var gathering: Gathering,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: InviteStatus = InviteStatus.PENDING
) : BaseEntity<Long>()