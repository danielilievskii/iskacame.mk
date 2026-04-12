package mk.ukim.finki.iskacamebackend.dto.request.gathering

import jakarta.validation.constraints.NotEmpty

data class CastVoteRequest(
    @field:NotEmpty
    val placeIds: List<Long>
)
