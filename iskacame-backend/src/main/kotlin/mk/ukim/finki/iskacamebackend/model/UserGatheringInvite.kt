package mk.ukim.finki.iskacamebackend.model

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity
import mk.ukim.finki.iskacamebackend.model.enums.InviteStatus

@Entity
@Table(name = "user_gathering_invites")
class UserGatheringInvite(
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