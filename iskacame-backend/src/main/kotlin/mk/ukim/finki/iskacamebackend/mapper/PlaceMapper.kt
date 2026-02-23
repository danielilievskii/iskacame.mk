package mk.ukim.finki.iskacamebackend.mapper

import mk.ukim.finki.iskacamebackend.dto.response.PlaceDto
import mk.ukim.finki.iskacamebackend.model.Place

import org.mapstruct.Mapper

/**
 * Class for mapping between to [Place] entities and DTOs.
 */
@Mapper(componentModel = "spring")
interface PlaceMapper {

  /**
   * Maps a [Place] object to a [PlaceDto] object.
   *
   * @param place the Place entity object
   * @return the mapped PlaceDto object
   */
  fun toPlaceDto(place: Place): PlaceDto
}