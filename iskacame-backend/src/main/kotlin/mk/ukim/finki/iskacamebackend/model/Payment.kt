package mk.ukim.finki.iskacamebackend.model

import User
import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.enums.PaidStatus

@Entity
@Table(name = "payments")
class Payment(
    @Column(name = "amount_paid")
    val amountPaid: Double,

    @Enumerated(EnumType.STRING)
    @Column(name = "paid_status")
    val paidStatus: PaidStatus,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne
    @JoinColumn(name = "gathering_id")
    val gathering: Gathering
) : BaseEntity<Long>()
