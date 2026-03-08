package mk.ukim.finki.iskacamebackend.dto.request.gathering

import java.time.LocalDateTime
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import mk.ukim.finki.iskacamebackend.common.GatheringExceptionMessages

data class CreateGatheringRequest(

    @field:NotBlank(message = GatheringExceptionMessages.TITLE_REQUIRED)
    @field:Size(max = 100, message = GatheringExceptionMessages.TITLE_MAX_LENGTH)
    val title: String,

    @field:Size(max = 500, message = GatheringExceptionMessages.DESCRIPTION_MAX_LENGTH)
    val description: String?,

    @field:NotNull(message = GatheringExceptionMessages.START_DATE_REQUIRED)
    val startDate: LocalDateTime,

    @field:NotNull(message = GatheringExceptionMessages.END_DATE_REQUIRED)
    val endDate: LocalDateTime,

    @field:NotEmpty(message = GatheringExceptionMessages.INVALID_PARTICIPANTS)
    val participantIds: List<Long>,
)