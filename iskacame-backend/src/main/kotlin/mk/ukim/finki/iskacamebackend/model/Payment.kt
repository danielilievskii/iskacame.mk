package mk.ukim.finki.iskacamebackend.model

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.enums.PaidStatus

@Entity
@Table(name = "payments")
class Payment(
    @Column(name = "amount_paid")
    var amountPaid: Double,

    @Enumerated(EnumType.STRING)
    @Column(name = "paid_status")
    var paidStatus: PaidStatus,

    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User,

    @ManyToOne
    @JoinColumn(name = "gathering_id")
    var gathering: Gathering
) : BaseEntity<Long>()
