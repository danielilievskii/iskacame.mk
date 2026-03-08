package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.assembler.GatheringDetailsAssembler
import mk.ukim.finki.iskacamebackend.common.GatheringExceptionMessages
import mk.ukim.finki.iskacamebackend.dto.response.GatheringDetailsDto
import mk.ukim.finki.iskacamebackend.dto.response.GatheringInvitationDto
import mk.ukim.finki.iskacamebackend.exception.BadRequestException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.mapper.GatheringParticipationMapper
import mk.ukim.finki.iskacamebackend.model.GatheringParticipation
import mk.ukim.finki.iskacamebackend.model.enums.GatheringStatus
import mk.ukim.finki.iskacamebackend.model.enums.ParticipationStatus
import mk.ukim.finki.iskacamebackend.repository.*
import mk.ukim.finki.iskacamebackend.service.AuthService
import mk.ukim.finki.iskacamebackend.service.GatheringParticipationService
import mk.ukim.finki.iskacamebackend.service.GatheringService
import mk.ukim.finki.iskacamebackend.service.UserService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GatheringParticipationServiceImpl(
    private val gatheringParticipationRepository: GatheringParticipationRepository,
    private val authService: AuthService,
    private val gatheringParticipationMapper: GatheringParticipationMapper,
    private val userService: UserService,
    private val gatheringService: GatheringService,
    private val gatheringDetailsAssembler: GatheringDetailsAssembler
) : GatheringParticipationService {

    @Transactional(readOnly = true)
    override fun getGatheringInvitations(): List<GatheringInvitationDto> {

        val currentUserId = authService.getCurrentUserId()

        val invitedParticipations = gatheringParticipationRepository
            .findAllByUserIdAndStatus(currentUserId, ParticipationStatus.INVITED)

        return invitedParticipations.map(gatheringParticipationMapper::toGatheringInvitationDto)
    }

    @PreAuthorize("@permissionService.isGatheringParticipant(#gatheringId, authentication.principal.id)")
    override fun inviteUserToGathering(userId: Long, gatheringId: Long) {

        val gathering = gatheringService.getGatheringById(gatheringId)

        if (gathering.status == GatheringStatus.CANCELLED) {
            throw BadRequestException(GatheringExceptionMessages.CANNOT_INVITE_TO_CANCELLED_GATHERING)
        }

        val existingParticipation = gatheringParticipationRepository.findByGatheringIdAndUserId(gatheringId, userId)

        if (existingParticipation != null) {
            reinviteUser(existingParticipation)
            return
        }

        val user = userService.getUserById(userId)

        val participation = GatheringParticipation(
            user = user,
            gathering = gathering,
            status = ParticipationStatus.INVITED
        )
        gatheringParticipationRepository.save(participation)
    }

    @PreAuthorize("@permissionService.isParticipationOwner(#participationId, authentication.principal.id)")
    override fun acceptInvitation(participationId: Long): GatheringDetailsDto {

        val participation = getParticipationById(participationId)

        if (participation.status != ParticipationStatus.INVITED) {
            throw BadRequestException(
                when (participation.status) {
                    ParticipationStatus.JOINED -> GatheringExceptionMessages.INVITATION_ALREADY_ACCEPTED
                    ParticipationStatus.DECLINED -> GatheringExceptionMessages.INVITATION_ALREADY_DECLINED
                    else -> GatheringExceptionMessages.INVITATION_NO_LONGER_VALID
                }
            )
        }

        participation.status = ParticipationStatus.JOINED
        gatheringParticipationRepository.save(participation)

        return gatheringDetailsAssembler.assemble(participation.gathering)
    }

    @PreAuthorize("@permissionService.isParticipationOwner(#participationId, authentication.principal.id)")
    override fun declineInvitation(participationId: Long) {

        val participation  = getParticipationById(participationId)

        if (participation.status != ParticipationStatus.INVITED) {
            throw BadRequestException(
                when (participation.status) {
                    ParticipationStatus.JOINED -> GatheringExceptionMessages.INVITATION_ALREADY_ACCEPTED
                    ParticipationStatus.DECLINED -> GatheringExceptionMessages.INVITATION_ALREADY_DECLINED
                    else -> GatheringExceptionMessages.INVITATION_NO_LONGER_VALID
                }
            )
        }

        participation.status = ParticipationStatus.DECLINED
        gatheringParticipationRepository.save(participation)
    }

    @PreAuthorize("@permissionService.isGatheringParticipant(#gatheringId, authentication.principal.id)")
    override fun leaveGathering(gatheringId: Long) {

        val currentUserId = authService.getCurrentUserId()

        val participation = gatheringParticipationRepository.findByGatheringIdAndUserIdAndStatus(
            gatheringId,
            currentUserId,
            ParticipationStatus.JOINED
        ) ?: throw BadRequestException(GatheringExceptionMessages.USER_NOT_IN_GATHERING)

        participation.status = ParticipationStatus.LEFT
        gatheringParticipationRepository.save(participation)
    }

    @PreAuthorize("@permissionService.isGatheringCreator(#gatheringId, authentication.principal.id)")
    override fun removeUserFromGathering(userId: Long, gatheringId: Long) {

        val participation = gatheringParticipationRepository.findByGatheringIdAndUserIdAndStatus(
            gatheringId,
            userId,
            ParticipationStatus.JOINED
        ) ?: throw BadRequestException(GatheringExceptionMessages.USER_NOT_IN_GATHERING)

        participation.status = ParticipationStatus.REMOVED
        gatheringParticipationRepository.save(participation)
    }

    /**
     * Retrieves a gathering participation by its ID.
     *
     * @param id the ID of the gathering participation
     * @return the gathering participation
     * @throws ResourceNotFoundException if no participation with the given ID exists
     */
    private fun getParticipationById(id: Long): GatheringParticipation {
        return gatheringParticipationRepository.findById(id)
            .orElseThrow {
                ResourceNotFoundException(GatheringExceptionMessages.GATHERING_PARTICIPATION_NOT_FOUND)
            }
    }

    /**
     * Handles re-inviting a user who already has an existing participation record.
     * Throws an exception if the user is already active or has a pending invitation.
     *
     * @param participation the existing participation record
     * @throws BadRequestException if the user is already in the gathering or already invited
     */
    private fun reinviteUser(participation: GatheringParticipation) {

        when (participation.status) {
            ParticipationStatus.JOINED ->
                throw BadRequestException(GatheringExceptionMessages.USER_ALREADY_IN_GATHERING)

            ParticipationStatus.INVITED ->
                throw BadRequestException(GatheringExceptionMessages.INVITATION_ALREADY_SENT)

            ParticipationStatus.DECLINED, ParticipationStatus.LEFT, ParticipationStatus.REMOVED -> {
                participation.status = ParticipationStatus.INVITED
                gatheringParticipationRepository.save(participation)
            }
        }
    }
}