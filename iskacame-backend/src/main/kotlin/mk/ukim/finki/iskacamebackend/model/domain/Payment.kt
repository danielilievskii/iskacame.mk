package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity
import java.math.BigDecimal

@Entity
@Table(name = "payments")
class Payment(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    var fromUser: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    var toUser: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gathering_id", nullable = false)
    var gathering: Gathering,

    @Column(name = "amount", nullable = false)
    var amount: BigDecimal

) : BaseEntity<Long>()