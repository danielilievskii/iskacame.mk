package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.model.enums.ParticipationStatus
import mk.ukim.finki.iskacamebackend.repository.GatheringImageRepository
import mk.ukim.finki.iskacamebackend.repository.GatheringParticipationRepository
import mk.ukim.finki.iskacamebackend.repository.GatheringRepository
import mk.ukim.finki.iskacamebackend.service.intf.PermissionService
import org.springframework.stereotype.Service

@Service("permissionService")
class PermissionServiceImpl(
    private val gatheringParticipationRepository: GatheringParticipationRepository,
    private val gatheringRepository: GatheringRepository,
    private val gatheringImageRepository: GatheringImageRepository
): PermissionService {

    override fun isGatheringParticipant(gatheringId: Long, userId: Long): Boolean {
        return gatheringParticipationRepository.existsByGatheringIdAndUserIdAndStatus(
            gatheringId,
            userId,
            ParticipationStatus.JOINED
        )
    }

    override fun isGatheringCreator(gatheringId: Long, userId: Long): Boolean {
        return gatheringRepository.existsByIdAndCreatorId(gatheringId, userId)
    }

    override fun isParticipationOwner(participationId: Long, userId: Long): Boolean {
        return gatheringParticipationRepository.existsByIdAndUserId(participationId, userId)
    }

    override fun isGatheringImageOwner(gatheringId: Long, imageId: Long, userId: Long): Boolean {
        return gatheringImageRepository.existsByIdAndGatheringIdAndUploaderId(imageId, gatheringId, userId)
    }
}