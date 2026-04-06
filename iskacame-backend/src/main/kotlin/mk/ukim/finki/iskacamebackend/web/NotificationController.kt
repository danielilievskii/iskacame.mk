package mk.ukim.finki.iskacamebackend.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import mk.ukim.finki.iskacamebackend.dto.response.NotificationDto
import mk.ukim.finki.iskacamebackend.service.intf.NotificationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Manages user notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    @GetMapping
    @Operation(summary = "Get all notifications for the current user")
    fun getNotifications(): ResponseEntity<List<NotificationDto>> {
        val notifications = notificationService.getNotifications()
        return ResponseEntity.ok(notifications)
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    fun markAsRead(@PathVariable id: Long): ResponseEntity<Void> {
        notificationService.markAsRead(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    fun markAllAsRead(): ResponseEntity<Void> {
        notificationService.markAllAsRead()
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    fun getUnreadCount(): ResponseEntity<Long> {
        val count = notificationService.getUnreadCount()
        return ResponseEntity.ok(count)
    }
}
