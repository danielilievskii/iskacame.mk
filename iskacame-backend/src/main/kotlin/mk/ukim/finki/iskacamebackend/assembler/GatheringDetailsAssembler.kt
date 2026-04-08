package mk.ukim.finki.iskacamebackend.assembler

import mk.ukim.finki.iskacamebackend.dto.response.gathering.*
import mk.ukim.finki.iskacamebackend.mapper.GatheringParticipationMapper
import mk.ukim.finki.iskacamebackend.mapper.GatheringMapper
import mk.ukim.finki.iskacamebackend.mapper.PlaceMapper
import mk.ukim.finki.iskacamebackend.model.domain.Gathering
import mk.ukim.finki.iskacamebackend.model.domain.GatheringResponse
import mk.ukim.finki.iskacamebackend.model.enums.GatheringStatus
import mk.ukim.finki.iskacamebackend.model.enums.PollStatus
import mk.ukim.finki.iskacamebackend.repository.*
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Assembler responsible for constructing [GatheringDetailsDto] instances.
 */
@Component
class GatheringDetailsAssembler(
    private val gatheringParticipationRepository: GatheringParticipationRepository,
    private val placeRepository: PlaceRepository,
    private val gatheringResponseRepository: GatheringResponseRepository,
    private val placePollRepository: PlacePollRepository,
    private val gatheringPlaceVoteRepository: GatheringPlaceVoteRepository,
    private val gatheringMapper: GatheringMapper,
    private val gatheringParticipationMapper: GatheringParticipationMapper,
    private val placeMapper: PlaceMapper,
    private val authService: AuthService,
    private val gatheringRepository: GatheringRepository,
    private val chatMessageRepository: ChatMessageRepository
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

        val suggestedPlaceDtos = placeRepository
            .findAllByGatheringId(gathering.id!!)
            .map { placeMapper.toPlaceDto(it) }

        val responses = gatheringResponseRepository
            .findAllByGatheringId(gathering.id!!)

        val timeSlotPreferenceDtos = assembleTimeSlotPreferences(responses)
        val typePreferenceDtos = assembleTypePreferences(responses)

        val currentUserId = authService.getCurrentUserId()
        val hasSubmittedResponse = gatheringResponseRepository
            .existsByGatheringIdAndUserId(gathering.id!!, currentUserId)

        val activePollDto = assemblePoll(gathering, suggestedPlaceDtos, currentUserId)

        val chatRoomId = gathering.chatRoom?.id
        val unseenMessagesCount = if (chatRoomId != null) {
            chatMessageRepository
                .countUnseenForUserGrouped(listOf(chatRoomId), currentUserId)
                .firstOrNull()?.unseenMessagesCount ?: 0
        } else 0

        return gatheringMapper
            .toGatheringDetailsDto(gathering)
            .copy(
                participants = participantDtos,
                suggestedPlaces = suggestedPlaceDtos,
                timeSlotPreferences = timeSlotPreferenceDtos,
                typePreferences = typePreferenceDtos,
                hasSubmittedResponse = hasSubmittedResponse,
                activePoll = activePollDto,
                unseenMessagesCount = unseenMessagesCount
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

    private fun assemblePoll(
        gathering: Gathering,
        suggestedPlaceDtos: List<PlaceDto>,
        currentUserId: Long
    ): PlacePollDto? {
        val gatheringId = gathering.id!!
        val poll = placePollRepository.findByGatheringId(gatheringId) ?: return null

        // Auto-end expired polls and finalize gathering
        if (poll.status == PollStatus.ACTIVE && poll.endsAt.isBefore(Instant.now())) {
            poll.status = PollStatus.ENDED
            placePollRepository.save(poll)

            if (gathering.status != GatheringStatus.FINALIZED && gathering.status != GatheringStatus.CANCELLED) {
                val votes = gatheringPlaceVoteRepository.findAllByGatheringId(gatheringId)
                val winningPlace = votes
                    .groupBy { it.place }
                    .maxByOrNull { it.value.size }
                    ?.key

                val responses = gatheringResponseRepository.findAllByGatheringId(gatheringId)
                val winningTimeSlot = responses
                    .flatMap { it.timeSlotPreferences }
                    .groupingBy { it }
                    .eachCount()
                    .maxByOrNull { it.value }
                    ?.key

                if (winningPlace != null) {
                    gathering.finalizedPlace = winningPlace
                }
                if (winningTimeSlot != null) {
                    gathering.finalizedTime = LocalDateTime.of(
                        winningTimeSlot.date,
                        LocalTime.of(winningTimeSlot.slot.startHour, 0)
                    )
                }
                gathering.status = GatheringStatus.FINALIZED
                gatheringRepository.save(gathering)
            }
        }

        val allVotes = gatheringPlaceVoteRepository.findAllByGatheringId(gatheringId)
        val voteCountByPlaceId = allVotes.groupBy { it.place.id!! }.mapValues { it.value.size }
        val myVotedPlaceIds = allVotes
            .filter { it.user.id == currentUserId }
            .mapNotNull { it.place.id }

        val placeOptions = suggestedPlaceDtos.map { placeDto ->
            PlacePollOptionDto(
                place = placeDto,
                voteCount = voteCountByPlaceId[placeDto.id] ?: 0
            )
        }

        return PlacePollDto(
            id = poll.id!!,
            status = poll.status,
            endsAt = poll.endsAt,
            createdAt = poll.createdAt!!,
            places = placeOptions,
            myVotedPlaceIds = myVotedPlaceIds
        )
    }
}