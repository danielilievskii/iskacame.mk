package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity
import java.time.Instant

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(

  @Column(name = "token", unique = true, nullable = false, length = 512)
  var token: String,

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  var user: User,

  @Column(name = "expires_at", nullable = false)
  var expiresAt: Instant,

  @Column(name = "revoked", nullable = false)
  var revoked: Boolean = false

) : BaseEntity<Long>()
