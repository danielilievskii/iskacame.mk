package mk.ukim.finki.iskacamebackend.mapper

import mk.ukim.finki.iskacamebackend.dto.response.GatheringSummaryDto
import mk.ukim.finki.iskacamebackend.model.Gathering

import org.mapstruct.Mapper

/**
 * Class for mapping between to [Gathering] entities and DTOs.
 */
@Mapper(componentModel = "spring")
interface GatheringMapper {

  /**
   * Maps a [Gathering] object to a [GatheringSummaryDto] object.
   *
   * @param gathering the Gathering entity object
   * @return the mapped GatheringSummaryDto object
   */
  fun toGatheringSummaryDto(gathering: Gathering): GatheringSummaryDto
}