package mk.ukim.finki.iskacamebackend.dto.response.gathering

import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto
import mk.ukim.finki.iskacamebackend.model.enums.GatheringStatus
import java.time.LocalDateTime

data class GatheringDetailsDto(
    val id: Long,
    val creatorId: Long,
    val title: String,
    val description: String?,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val status: GatheringStatus,
    val finalizedTime: LocalDateTime?,
    val finalizedPlace: PlaceDto?,
    val participants: List<ParticipantDto>?,
    val suggestedPlaces: List<PlaceDto>?,
    val chatRoomId: Long,
)

data class ParticipantDto(
    val user: UserDto,
    val participationStatus: String
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