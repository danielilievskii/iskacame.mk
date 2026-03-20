package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.common.GatheringResponseExceptionMessages
import mk.ukim.finki.iskacamebackend.dto.request.gathering.SubmitGatheringResponseRequest
import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringResponseOptionsDto
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.mapper.GatheringTimeSlotMapper
import mk.ukim.finki.iskacamebackend.model.domain.GatheringResponse
import mk.ukim.finki.iskacamebackend.model.enums.GatheringType
import mk.ukim.finki.iskacamebackend.repository.GatheringResponseRepository
import mk.ukim.finki.iskacamebackend.repository.GatheringTimeSlotRepository
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import mk.ukim.finki.iskacamebackend.service.intf.GatheringResponseService
import mk.ukim.finki.iskacamebackend.service.intf.GatheringService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of GatheringResponseService
 */
@Service
class GatheringResponseServiceImpl(
    private val authService: AuthService,
    private val gatheringResponseRepository: GatheringResponseRepository,
    private val gatheringTimeSlotRepository: GatheringTimeSlotRepository,
    private val gatheringService: GatheringService,
    private val gatheringTimeSlotMapper: GatheringTimeSlotMapper
    ) : GatheringResponseService {

    @Transactional(readOnly = true)
    @PreAuthorize("@permissionService.isGatheringParticipant(#gatheringId, authentication.principal.id)")
    override fun getGatheringResponseOptions(gatheringId: Long): GatheringResponseOptionsDto {

        val types = GatheringType.entries.toSet()

        val timeSlotDtos = gatheringTimeSlotRepository
            .findAllByGatheringId(gatheringId)
            .map { gatheringTimeSlotMapper.toGatheringTimeSlotDto(it) }

        return GatheringResponseOptionsDto(
            types = types,
            timeSlots = timeSlotDtos
        )
    }

    @Transactional
    @PreAuthorize("@permissionService.isGatheringParticipant(#gatheringId, authentication.principal.id)")
    override fun submitGatheringResponse(
        gatheringId: Long,
        request: SubmitGatheringResponseRequest
    ) {
        val currentUser = authService.getCurrentUser()
        val gathering = gatheringService.getGatheringById(gatheringId)

        val responseExists =
            gatheringResponseRepository.existsByGatheringIdAndUserId(gatheringId, currentUser.id!!)

        if (responseExists) {
            throw IllegalStateException(GatheringResponseExceptionMessages.RESPONSE_SUBMITTED)
        }

        val timeSlotPreferences = gatheringTimeSlotRepository.findAllById(request.timeSlotIds)

        val response = GatheringResponse(
            user = currentUser,
            gathering = gathering,
            typePreferences = request.types.toMutableSet(),
            timeSlotPreferences = timeSlotPreferences.toMutableSet(),
        )

        gatheringResponseRepository.save(response)
    }

    @Transactional
    @PreAuthorize("@permissionService.isGatheringParticipant(#gatheringId, authentication.principal.id)")
    override fun updateGatheringResponse(
        gatheringId: Long,
        request: SubmitGatheringResponseRequest
    ) {
        val currentUser = authService.getCurrentUser()

        val response = gatheringResponseRepository.findByGatheringIdAndUserId(gatheringId, currentUser.id!!)
            ?: throw ResourceNotFoundException(GatheringResponseExceptionMessages.RESPONSE_NOT_FOUND)

        val timeSlotPreferences = gatheringTimeSlotRepository.findAllById(request.timeSlotIds)

        response.typePreferences.apply {
            clear()
            addAll(request.types)
        }

        response.timeSlotPreferences.apply {
            clear()
            addAll(timeSlotPreferences)
        }

        gatheringResponseRepository.save(response)
    }
}