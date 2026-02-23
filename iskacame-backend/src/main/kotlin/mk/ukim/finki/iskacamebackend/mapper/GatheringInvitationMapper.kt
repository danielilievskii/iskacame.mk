package mk.ukim.finki.iskacamebackend.mapper

import mk.ukim.finki.iskacamebackend.dto.response.GatheringInviteDto
import mk.ukim.finki.iskacamebackend.dto.response.ParticipantDto
import mk.ukim.finki.iskacamebackend.model.GatheringInvitation

import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * Class for mapping between to [GatheringInvitation] entities and DTOs.
 */
@Mapper(componentModel = "spring", uses = [UserMapper::class])
interface GatheringInvitationMapper {

  /**
   * Maps a [GatheringInvitation] object to a [GatheringInviteDto] object.
   *
   * @param invite the GatheringInvitation entity object
   * @return the mapped GatheringInviteDto object
   */
  @Mapping(target = "gatheringCreator", source = "gathering.creator")
  @Mapping(target = "gatheringTitle", source = "gathering.title")
  fun toGatheringInviteDto(invite: GatheringInvitation): GatheringInviteDto

  /**
   * Maps a [GatheringInvitation] object to a [ParticipantDto] object.
   *
   * @param invite the GatheringInvitation entity object
   * @return the mapped ParticipantDto object
   */
  @Mapping(target = "inviteStatus", source = "status")
  fun toParticipantDto(invite: GatheringInvitation): ParticipantDto
}