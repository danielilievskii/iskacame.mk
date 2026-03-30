package mk.ukim.finki.iskacamebackend.mapper

import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringInvitationDto
import mk.ukim.finki.iskacamebackend.dto.response.gathering.ParticipantDto
import mk.ukim.finki.iskacamebackend.model.domain.GatheringParticipation

import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * Class for mapping between to [GatheringParticipation] entities and DTOs.
 */
@Mapper(componentModel = "spring", uses = [UserMapper::class])
interface GatheringParticipationMapper {

  /**
   * Maps a [GatheringParticipation] object to a [GatheringInvitationDto] object.
   *
   * @param participation the GatheringParticipation entity object
   * @return the mapped GatheringInviteDto object
   */
  @Mapping(target = "gatheringId", source = "gathering.id")
  @Mapping(target = "gatheringCreator", source = "gathering.creator")
  @Mapping(target = "gatheringTitle", source = "gathering.title")
  fun toGatheringInvitationDto(participation: GatheringParticipation): GatheringInvitationDto

  /**
   * Maps a [GatheringParticipation] object to a [ParticipantDto] object.
   *
   * @param participation the GatheringParticipation entity object
   * @return the mapped ParticipantDto object
   */
  @Mapping(target = "participationStatus", source = "status")
  fun toParticipantDto(participation: GatheringParticipation): ParticipantDto
}