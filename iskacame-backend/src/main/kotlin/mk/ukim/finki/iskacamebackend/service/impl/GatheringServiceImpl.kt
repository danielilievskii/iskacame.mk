package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.assembler.GatheringDetailsAssembler
import mk.ukim.finki.iskacamebackend.common.GatheringExceptionMessages
import mk.ukim.finki.iskacamebackend.dto.request.CreateGatheringRequest
import mk.ukim.finki.iskacamebackend.dto.request.UpdateGatheringRequest
import mk.ukim.finki.iskacamebackend.dto.response.GatheringDetailsDto
import mk.ukim.finki.iskacamebackend.dto.response.GatheringSummaryDto
import mk.ukim.finki.iskacamebackend.exception.BadRequestException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.mapper.GatheringMapper
import mk.ukim.finki.iskacamebackend.model.Gathering
import mk.ukim.finki.iskacamebackend.model.GatheringInvitation
import mk.ukim.finki.iskacamebackend.model.User
import mk.ukim.finki.iskacamebackend.model.enums.GatheringStatus
import mk.ukim.finki.iskacamebackend.model.enums.InviteStatus
import mk.ukim.finki.iskacamebackend.repository.*
import mk.ukim.finki.iskacamebackend.service.AuthService
import mk.ukim.finki.iskacamebackend.service.GatheringService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GatheringServiceImpl(
    private val gatheringRepository: GatheringRepository,
    private val gatheringInvitationRepository: GatheringInvitationRepository,
    private val userRepository: UserRepository,
    private val authService: AuthService,
    private val gatheringMapper: GatheringMapper,
    private val gatheringDetailsAssembler: GatheringDetailsAssembler
) : GatheringService {

    override fun getGatheringById(id: Long): Gathering {

        return gatheringRepository.findById(id)
            .orElseThrow { ResourceNotFoundException(GatheringExceptionMessages.GATHERING_NOT_FOUND) }
    }

    @Transactional
    override fun createGathering(request: CreateGatheringRequest): GatheringDetailsDto {

        val currentUser = authService.getCurrentUser()
        validateGatheringCreate(request, currentUser)

        val gathering = Gathering(
            creator = currentUser,
            title = request.title,
            description = request.description,
            startDate = request.startDate,
            endDate = request.endDate,
            status = GatheringStatus.DRAFT,
            finalizedTime = null,
            finalizedPlace = null
        )

        val savedGathering = gatheringRepository.save(gathering)

        val invitation = GatheringInvitation(
            user = currentUser,
            gathering = savedGathering,
            status = InviteStatus.ACCEPTED
        )

        gatheringInvitationRepository.save(invitation)

        val participantInvitations = userRepository
            .findAllById(request.participantIds)
            .map { participant ->
                GatheringInvitation(
                    user = participant,
                    gathering = savedGathering,
                    status = InviteStatus.PENDING
                )
            }

        gatheringInvitationRepository.saveAll(participantInvitations)

        return gatheringDetailsAssembler.assemble(gathering)
    }

    private fun validateGatheringCreate(request: CreateGatheringRequest, currentUser: User) {

        if (request.endDate.isBefore(request.startDate)) {
            throw BadRequestException(GatheringExceptionMessages.INVALID_DATE_RANGE)
        }

        if (request.participantIds.contains(currentUser.id)) {
            throw BadRequestException(GatheringExceptionMessages.CREATOR_IN_PARTICIPANTS)
        }

        if (request.participantIds.isEmpty()) {
            throw BadRequestException(GatheringExceptionMessages.INVALID_PARTICIPANTS)
        }
    }


    @Transactional
    @PreAuthorize("@permissionService.isGatheringCreator(#gatheringId, authentication.principal.id)")
    override fun updateGathering(gatheringId: Long, request: UpdateGatheringRequest): GatheringDetailsDto {

        val gathering = gatheringRepository.findById(gatheringId)
            .orElseThrow { ResourceNotFoundException(GatheringExceptionMessages.GATHERING_NOT_FOUND) }

        validateGatheringUpdate(gathering, request)

        request.title?.let { gathering.title = it }
        request.description?.let { gathering.description = it }
        request.startDate?.let { gathering.startDate = it }
        request.endDate?.let { gathering.endDate = it }

        val updatedGathering = gatheringRepository.save(gathering)
        return gatheringDetailsAssembler.assemble(updatedGathering)
    }

    private fun validateGatheringUpdate(gathering: Gathering, request: UpdateGatheringRequest) {

        if (gathering.status == GatheringStatus.FINALIZED) {
            throw BadRequestException(GatheringExceptionMessages.CANNOT_EDIT_FINALIZED_GATHERING)
        }

        if (gathering.status == GatheringStatus.CANCELLED) {
            throw BadRequestException(GatheringExceptionMessages.CANNOT_EDIT_CANCELLED_GATHERING)
        }

        if (request.startDate != null || request.endDate != null) {
            val newStartDate = request.startDate ?: gathering.startDate
            val newEndDate = request.endDate ?: gathering.endDate

            if (newEndDate.isBefore(newStartDate)) {
                throw BadRequestException(GatheringExceptionMessages.INVALID_DATE_RANGE)
            }
        }
    }


    @Transactional
    @PreAuthorize("@permissionService.isGatheringCreator(#gatheringId, authentication.principal.id)")
    override fun cancelGathering(gatheringId: Long) {

        val gathering = gatheringRepository.findById(gatheringId)
            .orElseThrow { ResourceNotFoundException(GatheringExceptionMessages.GATHERING_NOT_FOUND) }

        if (gathering.status == GatheringStatus.CANCELLED) {
            throw BadRequestException(GatheringExceptionMessages.GATHERING_ALREADY_CANCELLED)
        }

        gathering.status = GatheringStatus.CANCELLED
        gatheringRepository.save(gathering)
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@permissionService.isGatheringParticipant(#gatheringId, authentication.principal.id)")
    override fun getGatheringDetails(gatheringId: Long): GatheringDetailsDto {

        val gathering = gatheringRepository.findById(gatheringId)
            .orElseThrow { ResourceNotFoundException(GatheringExceptionMessages.GATHERING_NOT_FOUND) }

        return gatheringDetailsAssembler.assemble(gathering)
    }

    @Transactional(readOnly = true)
    override fun getMyGatherings(): List<GatheringSummaryDto> {

        val currentUserId = authService.getCurrentUserId()

        val acceptedInvites = gatheringInvitationRepository
            .findAllByUserIdAndStatus(currentUserId, InviteStatus.ACCEPTED)

        return acceptedInvites.map { invite ->
            gatheringMapper.toGatheringSummaryDto(invite.gathering)
        }
    }
}