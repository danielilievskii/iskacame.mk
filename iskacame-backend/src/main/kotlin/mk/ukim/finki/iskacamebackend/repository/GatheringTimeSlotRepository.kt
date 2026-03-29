package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.GatheringTimeSlot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GatheringTimeSlotRepository : JpaRepository<GatheringTimeSlot, Long> {
    fun findAllByGatheringId(gatheringId: Long): List<GatheringTimeSlot>
}