package mk.ukim.finki.iskacamebackend.dto.response.gathering

import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto
import mk.ukim.finki.iskacamebackend.model.enums.GatheringStatus
import mk.ukim.finki.iskacamebackend.model.enums.GatheringType
import mk.ukim.finki.iskacamebackend.model.enums.PriceLevel
import mk.ukim.finki.iskacamebackend.model.enums.TimeSlot
import java.time.LocalDate
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
    val timeSlotPreferences: List<GatheringTimeSlotPreferenceDto>?,
    val typePreferences: List<GatheringTypePreferenceDto>?,
    val hasSubmittedResponse: Boolean = false,
    val activePoll: PlacePollDto? = null
)

data class ParticipantDto(
    val user: UserDto,
    val participationStatus: String
)

data class GatheringTimeSlotPreferenceDto(
    val id: Long,
    val date: LocalDate,
    val slot: TimeSlot,
    val preferredByParticipants: List<Long>
)

data class GatheringTypePreferenceDto(
    val type: GatheringType,
    val preferredByParticipants: List<Long>
)

data class PlaceDto(
    val id: Long?,
    val name: String,
    val address: String?,
    val longitude: Double?,
    val latitude: Double?,
    val type: String,
    val priceLevel: PriceLevel,
)