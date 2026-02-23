package mk.ukim.finki.iskacamebackend.mapper

import mk.ukim.finki.iskacamebackend.dto.response.GatheringDetailsDto
import mk.ukim.finki.iskacamebackend.dto.response.GatheringSummaryDto
import mk.ukim.finki.iskacamebackend.model.Gathering

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
  fun toGatheringSummaryDto(gathering: Gathering): GatheringSummaryDto

  /**
   * Maps a [Gathering] object to a [GatheringDetailsDto] object.
   *
   * @param gathering the Gathering entity object
   * @return the mapped GatheringDetailsDto object
   */
  @Mapping(target = "creatorId", source = "creator.id")
  @Mapping(target = "participants", ignore = true)
  @Mapping(target = "suggestedPlaces", ignore = true)
  fun toGatheringDetailsDto(gathering: Gathering): GatheringDetailsDto
}