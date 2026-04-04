package mk.ukim.finki.iskacamebackend.repository

import jakarta.transaction.Transactional
import mk.ukim.finki.iskacamebackend.model.domain.VerificationToken
import mk.ukim.finki.iskacamebackend.model.enums.VerificationTokenPurpose
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface VerificationTokenRepository : JpaRepository<VerificationToken, Long> {
  fun findByToken(token: String): VerificationToken?

  fun findByTokenAndPurpose(token: String, purpose: VerificationTokenPurpose): VerificationToken?

  @Transactional
  fun deleteAllByExpiryDateBeforeOrUsedTrue(now: Instant): Int
}