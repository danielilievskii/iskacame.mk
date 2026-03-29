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
@Table(name = "expense_splits")
class ExpenseSplit(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    var expense: Expense,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(name = "amount_owed", nullable = false)
    var amountOwed: BigDecimal,

    @Column(name = "is_own_share", nullable = false)
    var isOwnShare: Boolean = false

) : BaseEntity<Long>()