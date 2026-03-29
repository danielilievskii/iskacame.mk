package mk.ukim.finki.iskacamebackend.dto.request.gathering

import mk.ukim.finki.iskacamebackend.model.enums.GatheringType

data class SubmitGatheringResponseRequest(
    val types: Set<GatheringType>,
    val timeSlotIds: Set<Long>
)