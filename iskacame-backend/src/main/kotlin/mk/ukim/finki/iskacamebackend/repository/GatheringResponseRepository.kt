package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.GatheringResponse
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GatheringResponseRepository : JpaRepository<GatheringResponse, Long> {
    fun existsByGatheringIdAndUserId(gatheringId: Long, userId: Long): Boolean
    fun findByGatheringIdAndUserId(gatheringId: Long, userId: Long): GatheringResponse?

    fun findAllByGatheringId(gatheringId: Long): List<GatheringResponse>
}