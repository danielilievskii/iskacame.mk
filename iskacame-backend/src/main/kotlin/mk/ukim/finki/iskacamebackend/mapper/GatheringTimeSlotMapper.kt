package mk.ukim.finki.iskacamebackend.mapper

import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringTimeSlotDto
import mk.ukim.finki.iskacamebackend.model.domain.GatheringTimeSlot
import org.mapstruct.Mapper

/**
 * Class for mapping between [GatheringTimeSlot] entities and DTOs.
 */
@Mapper(componentModel = "spring")
interface GatheringTimeSlotMapper {

    /**
     * Maps a [GatheringTimeSlot] object to a [GatheringTimeSlotDto] object.
     *
     * @param timeSlot the GatheringTimeSlot entity object
     * @return the mapped GatheringTimeSlotDto object
     */
    fun toGatheringTimeSlotDto(timeSlot: GatheringTimeSlot): GatheringTimeSlotDto

    /**
     * Maps a list of [GatheringTimeSlot] objects to a list of [GatheringTimeSlotDto] objects.
     *
     * @param timeSlots the list of GatheringTimeSlot entity objects
     * @return the list of mapped GatheringTimeSlotDto objects
     */
    fun toGatheringTimeSlotDtoList(timeSlots: List<GatheringTimeSlot>): List<GatheringTimeSlotDto> =
        timeSlots.map { toGatheringTimeSlotDto(it) }

}