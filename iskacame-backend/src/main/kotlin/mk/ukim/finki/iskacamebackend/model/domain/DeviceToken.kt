package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity

@Entity
@Table(name = "device_tokens")
class DeviceToken(
    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User,

    @Column(name = "token", unique = true)
    var token: String,

    @Column(name = "platform")
    var platform: String? = null
) : BaseEntity<Long>()
