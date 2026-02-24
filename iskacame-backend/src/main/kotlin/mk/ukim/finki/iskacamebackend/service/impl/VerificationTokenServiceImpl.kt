package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.common.AuthExceptionMessages
import mk.ukim.finki.iskacamebackend.config.VerificationTokenConfig
import mk.ukim.finki.iskacamebackend.exception.ConflictException
import mk.ukim.finki.iskacamebackend.exception.ResourceGoneException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.model.domain.VerificationToken
import mk.ukim.finki.iskacamebackend.repository.UserRepository
import mk.ukim.finki.iskacamebackend.repository.VerificationTokenRepository
import mk.ukim.finki.iskacamebackend.service.intf.VerificationTokenService
import mk.ukim.finki.iskacamebackend.utils.TokenGenerator
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class VerificationTokenServiceImpl(
  private val verificationTokenRepository: VerificationTokenRepository,
  private val userRepository: UserRepository,
  private val verificationTokenConfig: VerificationTokenConfig
) : VerificationTokenService {

  override fun createVerificationToken(user: User): VerificationToken {
    val token = TokenGenerator.generateVerificationToken()
    val expiry = Instant.now().plusMillis(verificationTokenConfig.expirationTime)

    val verificationToken = VerificationToken(
      token = token,
      user = user,
      expiryDate = expiry,
      used = false
    )

    return verificationTokenRepository.save(verificationToken)
  }

  override fun verifyToken(token: String) {
    val verificationToken = verificationTokenRepository.findByToken(token)
      ?: throw ResourceNotFoundException(AuthExceptionMessages.VERIFICATION_TOKEN_NOT_FOUND)

    if (verificationToken.used) throw ConflictException(AuthExceptionMessages.VERIFICATION_TOKEN_USED)

    if (verificationToken.expiryDate.isBefore(Instant.now())) throw ResourceGoneException(AuthExceptionMessages.VERIFICATION_TOKEN_EXPIRED)

    verificationToken.used = true
    verificationToken.user.emailVerified = true

    verificationTokenRepository.save(verificationToken)
    userRepository.save(verificationToken.user)
  }

  override fun cleanExpiredOrUsedTokens(): Int {
    return verificationTokenRepository.deleteAllByExpiryDateBeforeOrUsedTrue(Instant.now())
  }
}