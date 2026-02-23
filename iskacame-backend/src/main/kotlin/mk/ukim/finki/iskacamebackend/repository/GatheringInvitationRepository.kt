package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.GatheringInvitation
import mk.ukim.finki.iskacamebackend.model.enums.InviteStatus
import org.springframework.data.jpa.repository.JpaRepository

interface GatheringInvitationRepository : JpaRepository<GatheringInvitation, Long> {
    fun existsByGatheringIdAndUserIdAndStatus(gatheringId: Long, userId: Long, status: InviteStatus): Boolean
    fun findByGatheringIdAndUserIdAndStatus(gatheringId: Long, userId: Long, status: InviteStatus): GatheringInvitation?
    fun findAllByGatheringId(gatheringId: Long): List<GatheringInvitation>
    fun findAllByUserId(userId: Long): List<GatheringInvitation>
    fun findAllByUserIdAndStatus(userId: Long, status: InviteStatus): List<GatheringInvitation>
}