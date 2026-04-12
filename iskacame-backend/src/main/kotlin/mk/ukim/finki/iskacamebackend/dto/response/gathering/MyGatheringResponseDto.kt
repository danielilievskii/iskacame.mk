package mk.ukim.finki.iskacamebackend.dto.response.gathering

import mk.ukim.finki.iskacamebackend.model.enums.GatheringType

data class MyGatheringResponseDto(
    val types: Set<GatheringType>,
    val timeSlotIds: Set<Long>
)
