package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.model.enums.InviteStatus
import mk.ukim.finki.iskacamebackend.repository.GatheringInvitationRepository
import mk.ukim.finki.iskacamebackend.repository.GatheringRepository
import mk.ukim.finki.iskacamebackend.service.PermissionService
import org.springframework.stereotype.Service

@Service("permissionService")
class PermissionServiceImpl(
    private val gatheringInvitationRepository: GatheringInvitationRepository,
    private val gatheringRepository: GatheringRepository
): PermissionService {

    override fun isGatheringParticipant(gatheringId: Long, userId: Long): Boolean {
        return gatheringInvitationRepository.existsByGatheringIdAndUserIdAndStatus(
            gatheringId,
            userId,
            InviteStatus.ACCEPTED
        )
    }

    override fun isGatheringCreator(gatheringId: Long, userId: Long): Boolean {
        return gatheringRepository.findById(gatheringId)
            .map { it.creator.id == userId }
            .orElse(false)
    }
}