package mk.ukim.finki.iskacamebackend.mapper

import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringImageDto
import mk.ukim.finki.iskacamebackend.model.domain.GatheringImage

import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * Class for mapping between to [GatheringImage] entities and DTOs.
 */
@Mapper(componentModel = "spring", uses = [UserMapper::class])
interface GatheringImageMapper {

  /**
   * Maps a [GatheringImage] object to a [GatheringImageDto] object.
   *
   * @param gatheringImage the GatheringImage entity object
   * @return the mapped GatheringImageDto object
   */
  @Mapping(target = "uploadedAt", source = "createdAt")
  @Mapping(target = "gatheringId", source = "gathering.id")
  fun toGatheringImageDto(gatheringImage: GatheringImage): GatheringImageDto
}