package mk.ukim.finki.iskacamebackend.model.domain

import jakarta.persistence.*
import mk.ukim.finki.iskacamebackend.model.base.BaseEntity
import java.time.Instant

@Entity
@Table(name = "verification_tokens", indexes = [Index(columnList = "token")])
class VerificationToken(
  @Column(name = "token", nullable = false, unique = true, length = 128)
  var token: String,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  var user: User,

  @Column(name = "expiry_date", nullable = false)
  var expiryDate: Instant,

  @Column(name = "used", nullable = false)
  var used: Boolean = false
) : BaseEntity<Long>()