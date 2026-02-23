package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.GatheringInvitation
import mk.ukim.finki.iskacamebackend.model.enums.InviteStatus
import org.springframework.data.jpa.repository.JpaRepository

interface GatheringInvitationRepository : JpaRepository<GatheringInvitation, Long> {
    fun findAllByGatheringId(gatheringId: Long): List<GatheringInvitation>
    fun findAllByUserIdAndStatus(userId: Long, status: InviteStatus): List<GatheringInvitation>

    fun findByGatheringIdAndUserId(gatheringId:Long, userId: Long): GatheringInvitation?
    fun existsByGatheringIdAndUserIdAndStatus(gatheringId: Long, userId: Long, status: InviteStatus): Boolean
}