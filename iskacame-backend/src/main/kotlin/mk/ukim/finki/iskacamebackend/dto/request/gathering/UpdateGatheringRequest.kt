package mk.ukim.finki.iskacamebackend.dto.request.gathering

import jakarta.validation.constraints.Size
import mk.ukim.finki.iskacamebackend.common.GatheringExceptionMessages
import java.time.LocalDateTime

data class UpdateGatheringRequest(

    @field:Size(max = 100, message = GatheringExceptionMessages.TITLE_MAX_LENGTH)
    val title: String? = null,

    @field:Size(max = 500, message = GatheringExceptionMessages.DESCRIPTION_MAX_LENGTH)
    val description: String? = null,

    @field:Size(max = 200, message = GatheringExceptionMessages.LOCATION_MAX_LENGTH)
    val location: String? = null,

    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
)