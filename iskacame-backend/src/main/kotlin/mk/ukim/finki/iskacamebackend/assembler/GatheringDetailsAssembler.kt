package mk.ukim.finki.iskacamebackend.assembler

import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringDetailsDto
import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringTimeSlotPreferenceDto
import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringTypePreferenceDto
import mk.ukim.finki.iskacamebackend.mapper.GatheringParticipationMapper
import mk.ukim.finki.iskacamebackend.mapper.GatheringMapper
import mk.ukim.finki.iskacamebackend.mapper.PlaceMapper
import mk.ukim.finki.iskacamebackend.model.domain.Gathering
import mk.ukim.finki.iskacamebackend.model.domain.GatheringResponse
import mk.ukim.finki.iskacamebackend.repository.GatheringParticipationRepository
import mk.ukim.finki.iskacamebackend.repository.GatheringPlaceRepository
import mk.ukim.finki.iskacamebackend.repository.GatheringResponseRepository
import org.springframework.stereotype.Component

/**
 * Assembler responsible for constructing [GatheringDetailsDto] instances.
 */
@Component
class GatheringDetailsAssembler(
    private val gatheringParticipationRepository: GatheringParticipationRepository,
    private val gatheringPlaceRepository: GatheringPlaceRepository,
    private val gatheringResponseRepository: GatheringResponseRepository,
    private val gatheringMapper: GatheringMapper,
    private val gatheringParticipationMapper: GatheringParticipationMapper,
    private val placeMapper: PlaceMapper
) {

    /**
     * Assembles a [GatheringDetailsDto] from a [Gathering] entity,
     * including participants and suggested places.
     *
     * @param gathering the gathering entity to construct the DTO from
     * @return fully populated [GatheringDetailsDto]
     */
    fun assemble(gathering: Gathering): GatheringDetailsDto {

        val participantDtos = gatheringParticipationRepository
            .findAllByGatheringId(gathering.id!!)
            .map { gatheringParticipationMapper.toParticipantDto(it) }

        val suggestedPlaceDtos = gatheringPlaceRepository
            .findAllByGatheringId(gathering.id!!)
            .map { placeMapper.toPlaceDto(it.place) }

        val responses = gatheringResponseRepository
            .findAllByGatheringId(gathering.id!!)

        val timeSlotPreferenceDtos = assembleTimeSlotPreferences(responses)
        val typePreferenceDtos = assembleTypePreferences(responses)

        return gatheringMapper
            .toGatheringDetailsDto(gathering)
            .copy(
                participants = participantDtos,
                suggestedPlaces = suggestedPlaceDtos,
                timeSlotPreferences = timeSlotPreferenceDtos,
                typePreferences = typePreferenceDtos
            )
    }

    /**
     * Aggregates time slot preferences across all gathering responses.
     *
     * Groups responses by time slot, collecting the IDs of all participants
     * who selected each slot. Slots with no preferences are excluded since
     * they never appear in any response.
     *
     * @param responses All submitted responses for the gathering.
     * @return A list of [GatheringTimeSlotPreferenceDto], each containing the
     * slot details and the IDs of participants who preferred it.
     */
    private fun assembleTimeSlotPreferences(
        responses: List<GatheringResponse>
    ): List<GatheringTimeSlotPreferenceDto> =

        responses
            .asSequence()
            .flatMap { response ->
                response.timeSlotPreferences.map { timeSlot -> timeSlot to response.user.id!! }
            }
            .groupBy({ it.first }, { it.second })
            .map { (timeSlot, participantIds) ->
                GatheringTimeSlotPreferenceDto(
                    id = timeSlot.id!!,
                    date = timeSlot.date,
                    slot = timeSlot.slot,
                    preferredByParticipants = participantIds
                )
            }

    /**
     * Aggregates type preferences across all gathering responses.
     *
     * Groups responses by gathering type, collecting the IDs of all participants
     * who selected each type. Types with no preferences are excluded since
     * they never appear in any response.
     *
     * @param responses All submitted responses for the gathering.
     * @return A list of [GatheringTypePreferenceDto], each containing the
     * type and the IDs of participants who preferred it.
     */
    private fun assembleTypePreferences(
        responses: List<GatheringResponse>
    ): List<GatheringTypePreferenceDto> =

        responses
            .asSequence()
            .flatMap { response ->
                response.typePreferences.map { type -> type to response.user.id!! }
            }
            .groupBy({ it.first }, { it.second })
            .map { (type, participantIds) ->
                GatheringTypePreferenceDto(
                    type = type,
                    preferredByParticipants = participantIds
                )
            }
}