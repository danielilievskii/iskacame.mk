package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.GatheringParticipation
import mk.ukim.finki.iskacamebackend.model.enums.ParticipationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GatheringParticipationRepository : JpaRepository<GatheringParticipation, Long> {
    fun findAllByGatheringId(gatheringId: Long): List<GatheringParticipation>
    fun findAllByUserIdAndStatus(userId: Long, status: ParticipationStatus): List<GatheringParticipation>

    fun findByGatheringIdAndUserId(gatheringId:Long, userId: Long): GatheringParticipation?
    fun findByGatheringIdAndUserIdAndStatus(gatheringId:Long, userId: Long, status: ParticipationStatus): GatheringParticipation?
    fun existsByGatheringIdAndUserIdAndStatus(gatheringId: Long, userId: Long, status: ParticipationStatus): Boolean

    fun existsByIdAndUserId(id: Long, userId: Long): Boolean
}