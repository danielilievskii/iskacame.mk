package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.assembler.GatheringDetailsAssembler
import mk.ukim.finki.iskacamebackend.assembler.GatheringSummaryAssembler
import mk.ukim.finki.iskacamebackend.common.GatheringExceptionMessages
import mk.ukim.finki.iskacamebackend.dto.request.gathering.CreateGatheringRequest
import mk.ukim.finki.iskacamebackend.dto.request.gathering.UpdateGatheringRequest
import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringDetailsDto
import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringSummaryDto
import mk.ukim.finki.iskacamebackend.exception.BadRequestException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.model.domain.GatheringParticipation
import mk.ukim.finki.iskacamebackend.model.domain.Gathering
import mk.ukim.finki.iskacamebackend.model.domain.GatheringTimeSlot
import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.model.enums.GatheringStatus
import mk.ukim.finki.iskacamebackend.model.enums.ParticipationStatus
import mk.ukim.finki.iskacamebackend.model.enums.TimeSlot
import mk.ukim.finki.iskacamebackend.repository.*
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import mk.ukim.finki.iskacamebackend.service.intf.ChatService
import mk.ukim.finki.iskacamebackend.service.intf.GatheringService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Implementation of GatheringService
 */
@Service
class GatheringServiceImpl(
    private val gatheringRepository: GatheringRepository,
    private val gatheringParticipationRepository: GatheringParticipationRepository,
    private val gatheringTimeSlotRepository: GatheringTimeSlotRepository,
    private val userRepository: UserRepository,
    private val authService: AuthService,
    private val chatService: ChatService,
    private val gatheringSummaryAssembler: GatheringSummaryAssembler,
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
        chatService.createChatRoom(savedGathering)

        val creatorParticipation = GatheringParticipation(
            user = currentUser,
            gathering = savedGathering,
            status = ParticipationStatus.JOINED
        )
        gatheringParticipationRepository.save(creatorParticipation)

        val invitedParticipations = userRepository
            .findAllById(request.participantIds)
            .map { participant ->
                GatheringParticipation(
                    user = participant,
                    gathering = savedGathering,
                    status = ParticipationStatus.INVITED
                )
            }
        gatheringParticipationRepository.saveAll(invitedParticipations)

        val timeSlots: List<Pair<LocalDate, TimeSlot>> = generateTimeSlots(request.startDate, request.endDate)

        val gatheringTimeSlots = timeSlots
            .map { (date, slot) ->
                GatheringTimeSlot(
                    gathering = savedGathering,
                    date = date,
                    slot = slot
                )
            }
        gatheringTimeSlotRepository.saveAll(gatheringTimeSlots)

        return gatheringDetailsAssembler.assemble(savedGathering)
    }

    @Transactional
    @PreAuthorize("@permissionService.isGatheringCreator(#gatheringId, authentication.principal.id)")
    override fun updateGathering(gatheringId: Long, request: UpdateGatheringRequest): GatheringDetailsDto {

        val gathering = getGatheringById(gatheringId)

        validateGatheringUpdate(gathering, request)

        request.title?.let { gathering.title = it }
        request.description?.let { gathering.description = it }
        request.startDate?.let { gathering.startDate = it }
        request.endDate?.let { gathering.endDate = it }

        val updatedGathering = gatheringRepository.save(gathering)
        return gatheringDetailsAssembler.assemble(updatedGathering)
    }

    @Transactional
    @PreAuthorize("@permissionService.isGatheringCreator(#gatheringId, authentication.principal.id)")
    override fun cancelGathering(gatheringId: Long) {

        val gathering = getGatheringById(gatheringId)

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

        val participations = gatheringParticipationRepository
            .findAllByUserIdAndStatus(currentUserId, ParticipationStatus.JOINED)

        val gatherings = participations.map { it.gathering }

        return gatheringSummaryAssembler.assembleAll(gatherings, currentUserId)
    }

    /**
     * Validates the gathering creation request.
     * Ensures the date range is valid, the participant list is not empty,
     * and the creator is not included in the participant list.
     *
     * @param request the gathering creation request
     * @param currentUser the user creating the gathering
     * @throws BadRequestException if any validation rule is violated
     */
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

    /**
     * Validates the gathering update request.
     * Ensures the gathering is not finalized or cancelled, and that
     * the updated date range is valid if new dates are provided.
     *
     * @param gathering the existing gathering to be updated
     * @param request the gathering update request
     * @throws BadRequestException if any validation rule is violated
     */
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

    /**
     * Generates a list of time slots between the given start and end date-times.
     *
     * Iterates over each day in the range [startDateTime, endDateTime] and includes
     * only the [TimeSlot]s that overlap with the gathering's time window.
     *
     * @param startDateTime The start of the gathering (inclusive).
     * @param endDateTime The end of the gathering (exclusive at slot boundary).
     * @return A list of [LocalDate] to [TimeSlot] pairs representing all overlapping slots,
     * ordered chronologically.
     */
    private fun generateTimeSlots(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): List<Pair<LocalDate, TimeSlot>> {

        val timeSlots = mutableListOf<Pair<LocalDate, TimeSlot>>()

        var currentDate = startDateTime.toLocalDate()
        val endDate = endDateTime.toLocalDate()

        while (!currentDate.isAfter(endDate)) {

            for (slot in TimeSlot.entries) {
                val slotStartDateTime = currentDate.atTime(slot.startHour, 0)
                val slotEndDateTime = currentDate.atTime(slot.endHour, 0)

                val slotStartsBeforeGatheringEnds = slotStartDateTime.isBefore(endDateTime)
                val slotEndsAfterGatheringStarts = slotEndDateTime.isAfter(startDateTime)

                if (slotStartsBeforeGatheringEnds && slotEndsAfterGatheringStarts) {
                    timeSlots.add(Pair(currentDate, slot))
                }
            }
            currentDate = currentDate.plusDays(1)
        }

        return timeSlots
    }
}