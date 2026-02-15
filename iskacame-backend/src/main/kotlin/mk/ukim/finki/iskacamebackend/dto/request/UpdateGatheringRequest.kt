package mk.ukim.finki.iskacamebackend.dto.request

import java.time.LocalDateTime

data class UpdateGatheringRequest(
    val title: String?,
    val description: String?,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?
)