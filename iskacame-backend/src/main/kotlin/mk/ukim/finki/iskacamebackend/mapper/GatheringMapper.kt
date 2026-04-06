package mk.ukim.finki.iskacamebackend.mapper

import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringDetailsDto
import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringSummaryDto
import mk.ukim.finki.iskacamebackend.model.domain.Gathering

import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * Class for mapping between to [Gathering] entities and DTOs.
 */
@Mapper(componentModel = "spring", uses = [UserMapper::class])
interface GatheringMapper {

  /**
   * Maps a [Gathering] object to a [GatheringSummaryDto] object.
   *
   * @param gathering the Gathering entity object
   * @return the mapped GatheringSummaryDto object
   */
  @Mapping(target = "unseenMessagesCount", ignore = true)
  @Mapping(target = "chatRoomId", source = "chatRoom.id")
  fun toGatheringSummaryDto(gathering: Gathering): GatheringSummaryDto

  /**
   * Maps a [Gathering] object to a [GatheringDetailsDto] object.
   *
   * @param gathering the Gathering entity object
   * @return the mapped GatheringDetailsDto object
   */
  @Mapping(target = "creatorId", source = "creator.id")
  @Mapping(target = "chatRoomId", source = "chatRoom.id")
  @Mapping(target = "participants", ignore = true)
  @Mapping(target = "suggestedPlaces", ignore = true)
  @Mapping(target = "hasSubmittedResponse", ignore = true)
  @Mapping(target = "activePoll", ignore = true)
  fun toGatheringDetailsDto(gathering: Gathering): GatheringDetailsDto
}