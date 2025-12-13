package mk.ukim.finki.iskacamebackend.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Column(name = "name")
    var name: String,

    @Column(name = "username", unique = true, nullable = false, length = 30)
    var username: String,

    @Column(name = "email")
    var email: String,

    @Column(name = "password")
    var password: String,

    @Lob
    @Column(name = "profile_pic")
    var profilePic: ByteArray? = null,

    @Column(name = "is_verified")
    var isVerified: Boolean = false,
) : BaseEntity<Long>()
