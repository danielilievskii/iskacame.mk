package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity
import mk.ukim.finki.iskacamebackend.model.enums.NotificationType

@Entity
@Table(name = "notifications")
class Notification(
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    var recipient: User,

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    var type: NotificationType,

    @ManyToOne
    @JoinColumn(name = "gathering_id")
    var gathering: Gathering? = null,

    @Column(name = "title")
    var title: String,

    @Column(name = "body")
    var body: String,

    @Column(name = "is_read")
    var read: Boolean = false
) : BaseEntity<Long>()
