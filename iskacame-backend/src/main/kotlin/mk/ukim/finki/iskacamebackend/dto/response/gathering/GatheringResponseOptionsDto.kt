package mk.ukim.finki.iskacamebackend.dto.response.gathering

import mk.ukim.finki.iskacamebackend.model.enums.GatheringType
import mk.ukim.finki.iskacamebackend.model.enums.TimeSlot
import java.time.LocalDate

data class GatheringResponseOptionsDto(
    val types: Set<GatheringType>,
    val timeSlots: List<GatheringTimeSlotDto>
)

data class GatheringTimeSlotDto(
    val id: Long,
    val date: LocalDate,
    val slot: TimeSlot
)