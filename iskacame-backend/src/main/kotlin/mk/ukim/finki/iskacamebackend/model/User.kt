import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.BaseEntity

@Entity
@Table(name = "users")
class User(
    @Column(name = "name")
    val name: String,

    @Column(name = "username", unique = true, nullable = false, length = 30)
    val username: String,

    @Column(name = "email")
    val email: String,

    @Column(name = "password")
    val password: String,

    @Lob
    @Column(name = "profile_pic")
    val profilePic: ByteArray? = null,

    @Column(name = "is_verified")
    val isVerified: Boolean = false,
) : BaseEntity<Long>()
