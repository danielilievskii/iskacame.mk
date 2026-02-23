package mk.ukim.finki.iskacamebackend.assembler

import mk.ukim.finki.iskacamebackend.dto.response.GatheringDetailsDto
import mk.ukim.finki.iskacamebackend.mapper.GatheringInvitationMapper
import mk.ukim.finki.iskacamebackend.mapper.GatheringMapper
import mk.ukim.finki.iskacamebackend.mapper.PlaceMapper
import mk.ukim.finki.iskacamebackend.model.Gathering
import mk.ukim.finki.iskacamebackend.repository.GatheringInvitationRepository
import mk.ukim.finki.iskacamebackend.repository.GatheringPlaceRepository
import org.springframework.stereotype.Component

/**
 * Assembler responsible for constructing [GatheringDetailsDto] instances.

 */
@Component
class GatheringDetailsAssembler(
    private val gatheringInvitationRepository: GatheringInvitationRepository,
    private val gatheringPlaceRepository: GatheringPlaceRepository,
    private val gatheringMapper: GatheringMapper,
    private val gatheringInvitationMapper: GatheringInvitationMapper,
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

        val participants = gatheringInvitationRepository
            .findAllByGatheringId(gathering.id!!)
            .map { gatheringInvitationMapper.toParticipantDto(it) }

        val suggestedPlaces = gatheringPlaceRepository
            .findAllByGatheringId(gathering.id!!)
            .map { placeMapper.toPlaceDto(it.place) }

        return gatheringMapper
            .toGatheringDetailsDto(gathering)
            .copy(
                participants = participants,
                suggestedPlaces = suggestedPlaces
            )
    }
}