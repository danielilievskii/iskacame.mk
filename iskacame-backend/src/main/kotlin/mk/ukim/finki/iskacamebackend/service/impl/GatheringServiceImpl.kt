package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.common.GatheringExceptionMessages
import mk.ukim.finki.iskacamebackend.dto.request.CreateGatheringRequest
import mk.ukim.finki.iskacamebackend.dto.request.UpdateGatheringRequest
import mk.ukim.finki.iskacamebackend.dto.response.GatheringDto
import mk.ukim.finki.iskacamebackend.dto.response.ParticipantDto
import mk.ukim.finki.iskacamebackend.dto.response.PlaceDto
import mk.ukim.finki.iskacamebackend.exception.BadRequestException
import mk.ukim.finki.iskacamebackend.exception.CustomAccessDeniedException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.mapper.UserMapper
import mk.ukim.finki.iskacamebackend.model.Gathering
import mk.ukim.finki.iskacamebackend.model.GatheringPlace
import mk.ukim.finki.iskacamebackend.model.UserGatheringInvite
import mk.ukim.finki.iskacamebackend.model.enums.GatheringStatus
import mk.ukim.finki.iskacamebackend.model.enums.InviteStatus
import mk.ukim.finki.iskacamebackend.repository.*
import mk.ukim.finki.iskacamebackend.service.AuthService
import mk.ukim.finki.iskacamebackend.service.GatheringService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GatheringServiceImpl(
    private val gatheringRepository: GatheringRepository,
    private val userGatheringInviteRepository: UserGatheringInviteRepository,
    private val userRepository: UserRepository,
    private val placeRepository: PlaceRepository,
    private val gatheringPlaceRepository: GatheringPlaceRepository,
    private val authService: AuthService,
    private val userMapper: UserMapper
) : GatheringService {

    @Transactional
    override fun createGathering(request: CreateGatheringRequest): GatheringDto {
        val currentUser = authService.getCurrentUser()

        // Validate date range
        if (request.startDate != null && request.endDate != null && request.endDate.isBefore(request.startDate)) {
            throw BadRequestException(GatheringExceptionMessages.INVALID_DATE_RANGE)
        }

        // Validate participants
        if (request.participantIds.isEmpty()) {
            throw BadRequestException(GatheringExceptionMessages.INVALID_PARTICIPANTS)
        }

        // Create gathering
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

        // Create invites for participants
        val participants = userRepository.findAllById(request.participantIds)
        participants.forEach { participant ->
            val invite = UserGatheringInvite(
                user = participant,
                gathering = savedGathering,
                status = if (participant.id == currentUser.id) InviteStatus.ACCEPTED else InviteStatus.PENDING
            )
            userGatheringInviteRepository.save(invite)
        }

        // Add suggested places if provided
        if (!request.suggestedPlaceIds.isNullOrEmpty()) {
            val suggestedPlaces = placeRepository.findAllById(request.suggestedPlaceIds)
            suggestedPlaces.forEach { place ->
                val gatheringPlace = GatheringPlace(
                    gathering = savedGathering,
                    place = place,
                    suggestedBy = currentUser
                )
                gatheringPlaceRepository.save(gatheringPlace)
            }
        }

        return mapToGatheringDto(savedGathering)
    }

    @Transactional
    override fun updateGathering(gatheringId: Long, request: UpdateGatheringRequest): GatheringDto {
        val gathering = gatheringRepository.findById(gatheringId)
            .orElseThrow { ResourceNotFoundException(GatheringExceptionMessages.GATHERING_NOT_FOUND) }

        val currentUser = authService.getCurrentUser()

        // Check if current user is the creator
        if (gathering.creator.id != currentUser.id) {
            throw CustomAccessDeniedException(GatheringExceptionMessages.NOT_GATHERING_CREATOR)
        }

        // Check if gathering can be edited
        if (gathering.status == GatheringStatus.FINALIZED) {
            throw BadRequestException(GatheringExceptionMessages.CANNOT_EDIT_FINALIZED_GATHERING)
        }

        if (gathering.status == GatheringStatus.CANCELLED) {
            throw BadRequestException(GatheringExceptionMessages.CANNOT_EDIT_CANCELLED_GATHERING)
        }

        // Validate date range if both dates are being updated
        val newStartDate = request.startDate ?: gathering.startDate
        val newEndDate = request.endDate ?: gathering.endDate
        if (newStartDate != null && newEndDate != null && newEndDate.isBefore(newStartDate)) {
            throw BadRequestException(GatheringExceptionMessages.INVALID_DATE_RANGE)
        }

        // Update fields
        request.title?.let { gathering.title = it }
        request.description?.let { gathering.description = it }
        request.startDate?.let { gathering.startDate = it }
        request.endDate?.let { gathering.endDate = it }

        val updatedGathering = gatheringRepository.save(gathering)
        return mapToGatheringDto(updatedGathering)
    }

    @Transactional
    override fun cancelGathering(gatheringId: Long) {
        val gathering = gatheringRepository.findById(gatheringId)
            .orElseThrow { ResourceNotFoundException(GatheringExceptionMessages.GATHERING_NOT_FOUND) }

        val currentUser = authService.getCurrentUser()

        // Check if current user is the creator
        if (gathering.creator.id != currentUser.id) {
            throw CustomAccessDeniedException(GatheringExceptionMessages.NOT_GATHERING_CREATOR)
        }

        // Check if gathering is already cancelled
        if (gathering.status == GatheringStatus.CANCELLED) {
            throw BadRequestException(GatheringExceptionMessages.GATHERING_ALREADY_CANCELLED)
        }

        gathering.status = GatheringStatus.CANCELLED
        gatheringRepository.save(gathering)
    }

    @Transactional(readOnly = true)
    override fun getGatheringDetails(gatheringId: Long): GatheringDto {
        val gathering = gatheringRepository.findById(gatheringId)
            .orElseThrow { ResourceNotFoundException(GatheringExceptionMessages.GATHERING_NOT_FOUND) }

        val currentUser = authService.getCurrentUser()

        // Check if current user is a participant
        val invite = userGatheringInviteRepository.findByGatheringIdAndUserId(gatheringId, currentUser.id!!)
            ?: throw CustomAccessDeniedException(GatheringExceptionMessages.USER_NOT_PARTICIPANT)

        return mapToGatheringDto(gathering)
    }

    @Transactional(readOnly = true)
    override fun getMyGatherings(): List<GatheringDto> {
        val currentUser = authService.getCurrentUser()
        val invites = userGatheringInviteRepository.findAllByUserId(currentUser.id!!)

        return invites.map { invite ->
            mapToGatheringDto(invite.gathering)
        }
    }

    private fun mapToGatheringDto(gathering: Gathering): GatheringDto {
        val invites = userGatheringInviteRepository.findAllByGatheringId(gathering.id!!)
        val participants = invites.map { invite ->
            ParticipantDto(
                user = userMapper.toUserDto(invite.user),
                inviteStatus = invite.status.name
            )
        }

        // Get suggested places
        val gatheringPlaces = gatheringPlaceRepository.findAllByGatheringId(gathering.id!!)
        val suggestedPlaces = gatheringPlaces.map { gp ->
            PlaceDto(
                id = gp.place.id!!,
                name = gp.place.name,
                address = gp.place.address,
                longitude = gp.place.longitude,
                latitude = gp.place.latitude,
                type = gp.place.type.name,
                priceLevel = gp.place.priceLevel.name,
                link = gp.place.link
            )
        }

        val finalizedPlaceDto = gathering.finalizedPlace?.let { place ->
            PlaceDto(
                id = place.id!!,
                name = place.name,
                address = place.address,
                longitude = place.longitude,
                latitude = place.latitude,
                type = place.type.name,
                priceLevel = place.priceLevel.name,
                link = place.link
            )
        }

        return GatheringDto(
            id = gathering.id!!,
            creator = userMapper.toUserDto(gathering.creator),
            title = gathering.title,
            description = gathering.description,
            startDate = gathering.startDate,
            endDate = gathering.endDate,
            status = gathering.status,
            finalizedTime = gathering.finalizedTime,
            finalizedPlace = finalizedPlaceDto,
            participants = participants,
            suggestedPlaces = suggestedPlaces
        )
    }
}