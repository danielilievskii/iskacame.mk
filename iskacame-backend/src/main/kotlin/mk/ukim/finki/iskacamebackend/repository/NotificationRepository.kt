package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {

    fun findAllByRecipientIdOrderByCreatedAtDesc(recipientId: Long): List<Notification>

    fun findAllByRecipientIdAndReadFalse(recipientId: Long): List<Notification>

    fun countByRecipientIdAndReadFalse(recipientId: Long): Long
}
