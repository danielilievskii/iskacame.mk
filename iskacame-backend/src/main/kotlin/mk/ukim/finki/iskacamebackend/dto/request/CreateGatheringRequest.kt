package mk.ukim.finki.iskacamebackend.dto.request

import java.time.LocalDateTime

data class CreateGatheringRequest(
    val title: String,
    val description: String?,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val participantIds: List<Long>,
)