package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.dto.response.NotificationDto
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.mapper.NotificationMapper
import mk.ukim.finki.iskacamebackend.model.domain.Gathering
import mk.ukim.finki.iskacamebackend.model.domain.Notification
import mk.ukim.finki.iskacamebackend.model.enums.NotificationType
import mk.ukim.finki.iskacamebackend.model.enums.ParticipationStatus
import mk.ukim.finki.iskacamebackend.repository.GatheringParticipationRepository
import mk.ukim.finki.iskacamebackend.repository.NotificationRepository
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import mk.ukim.finki.iskacamebackend.service.intf.GatheringService
import mk.ukim.finki.iskacamebackend.service.intf.NotificationService
import mk.ukim.finki.iskacamebackend.service.intf.UserService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationServiceImpl(
    private val notificationRepository: NotificationRepository,
    private val gatheringParticipationRepository: GatheringParticipationRepository,
    private val authService: AuthService,
    private val userService: UserService,
    @Lazy private val gatheringService: GatheringService,
    private val notificationMapper: NotificationMapper
) : NotificationService {

    @Transactional(readOnly = true)
    override fun getNotifications(): List<NotificationDto> {
        val currentUserId = authService.getCurrentUserId()
        return notificationRepository
            .findAllByRecipientIdOrderByCreatedAtDesc(currentUserId)
            .map(notificationMapper::toNotificationDto)
    }

    @Transactional
    override fun markAsRead(notificationId: Long) {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { ResourceNotFoundException("Notification not found.") }
        notification.read = true
        notificationRepository.save(notification)
    }

    @Transactional
    override fun markAllAsRead() {
        val currentUserId = authService.getCurrentUserId()
        val unread = notificationRepository.findAllByRecipientIdAndReadFalse(currentUserId)
        unread.forEach { it.read = true }
        notificationRepository.saveAll(unread)
    }

    @Transactional(readOnly = true)
    override fun getUnreadCount(): Long {
        val currentUserId = authService.getCurrentUserId()
        return notificationRepository.countByRecipientIdAndReadFalse(currentUserId)
    }

    @Transactional
    override fun createNotification(
        recipientId: Long,
        type: NotificationType,
        gathering: Gathering,
        title: String,
        body: String
    ) {
        val recipient = userService.getUserById(recipientId)
        val notification = Notification(
            recipient = recipient,
            type = type,
            gathering = gathering,
            title = title,
            body = body
        )
        notificationRepository.save(notification)
    }

    @Transactional
    override fun notifyGatheringParticipants(
        gatheringId: Long,
        type: NotificationType,
        title: String,
        body: String
    ) {
        val gathering = gatheringService.getGatheringById(gatheringId)

        val participations = gatheringParticipationRepository
            .findAllByGatheringIdAndStatus(gatheringId, ParticipationStatus.JOINED)

        val notifications = participations.map { participation ->
            Notification(
                recipient = participation.user,
                type = type,
                gathering = gathering,
                title = title,
                body = body
            )
        }
        notificationRepository.saveAll(notifications)
    }
}
