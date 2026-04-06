package mk.ukim.finki.iskacamebackend.mapper

import mk.ukim.finki.iskacamebackend.dto.response.NotificationDto
import mk.ukim.finki.iskacamebackend.model.domain.Notification
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface NotificationMapper {

    @Mapping(target = "gatheringId", source = "gathering.id")
    fun toNotificationDto(notification: Notification): NotificationDto
}
