package mk.ukim.finki.iskacamebackend.dto.request.gathering

import jakarta.validation.constraints.Min

data class CreatePollRequest(
    @field:Min(1)
    val durationMinutes: Int
)
