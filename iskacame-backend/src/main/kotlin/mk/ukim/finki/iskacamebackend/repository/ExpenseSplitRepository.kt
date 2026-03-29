package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.ExpenseSplit
import org.springframework.data.jpa.repository.JpaRepository

interface ExpenseSplitRepository : JpaRepository<ExpenseSplit, Long> {
    fun findAllByExpenseGatheringId(gatheringId: Long): List<ExpenseSplit>
}