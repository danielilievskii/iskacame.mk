package mk.ukim.finki.iskacamebackend.dto.response

import mk.ukim.finki.iskacamebackend.model.enums.NotificationType
import java.time.Instant

data class NotificationDto(
    val id: Long,
    val type: NotificationType,
    val title: String,
    val body: String,
    val gatheringId: Long?,
    val read: Boolean,
    val createdAt: Instant
)
