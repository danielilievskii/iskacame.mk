package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity
import mk.ukim.finki.iskacamebackend.model.enums.UserRole
import java.time.Instant

@Entity
@Table(name = "users")
class User(
    @Column(name = "name")
    var name: String,

    @Column(name = "username", unique = true, nullable = false, length = 30)
    var username: String,

    @Column(name = "email", unique = true, nullable = false)
    var email: String,

    @Column(name = "password")
    var password: String,

    @Column(name = "phone")
    var phone: String? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var roles: MutableSet<UserRole>,

    @Embedded
    var avatar: AvatarImage? = null,

    @Column(name = "email_verified")
    var emailVerified: Boolean = false,

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,

    @Column(name = "disabled_at")
    var disabledAt: Instant? = null
    ) : BaseEntity<Long>()
