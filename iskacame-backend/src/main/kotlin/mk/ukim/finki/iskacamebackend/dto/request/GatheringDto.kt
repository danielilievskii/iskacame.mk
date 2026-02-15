package mk.ukim.finki.iskacamebackend.dto.response

import mk.ukim.finki.iskacamebackend.dto.UserDto
import mk.ukim.finki.iskacamebackend.model.enums.GatheringStatus
import java.time.LocalDateTime

data class GatheringDto(
    val id: Long,
    val creator: UserDto,
    val title: String,
    val description: String?,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val status: GatheringStatus,
    val finalizedTime: LocalDateTime?,
    val finalizedPlace: PlaceDto?,
    val participants: List<ParticipantDto>,
    val suggestedPlaces: List<PlaceDto>
)

data class ParticipantDto(
    val user: UserDto,
    val inviteStatus: String
)

data class PlaceDto(
    val id: Long,
    val name: String,
    val address: String?,
    val longitude: Double?,
    val latitude: Double?,
    val type: String,
    val priceLevel: String,
    val link: String?
)