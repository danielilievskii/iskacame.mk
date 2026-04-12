package mk.ukim.finki.iskacamebackend.service.intf

import mk.ukim.finki.iskacamebackend.dto.response.NotificationDto
import mk.ukim.finki.iskacamebackend.model.domain.Gathering
import mk.ukim.finki.iskacamebackend.model.enums.NotificationType

interface NotificationService {

    fun getNotifications(): List<NotificationDto>

    fun markAsRead(notificationId: Long)

    fun markAllAsRead()

    fun getUnreadCount(): Long

    fun createNotification(recipientId: Long, type: NotificationType, gathering: Gathering, title: String, body: String)

    fun notifyGatheringParticipants(gatheringId: Long, type: NotificationType, title: String, body: String)
}
