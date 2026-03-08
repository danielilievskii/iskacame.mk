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
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Implementation of the VerificationTokenService.
 */
@Service
class VerificationTokenServiceImpl(
  private val verificationTokenRepository: VerificationTokenRepository,
  private val userRepository: UserRepository,
  private val verificationTokenConfig: VerificationTokenConfig
) : VerificationTokenService {

  override fun createVerificationToken(user: User): VerificationToken {
    var token: String
    var exists: Boolean

    do {
      token = TokenGenerator.generateVerificationToken()
      exists = verificationTokenRepository.findByToken(token) != null
    } while (exists)

    val expiry = Instant.now().plusMillis(verificationTokenConfig.expirationTime)

    val verificationToken = VerificationToken(
      token = token,
      user = user,
      expiryDate = expiry,
      used = false
    )

    return verificationTokenRepository.save(verificationToken)
  }

  @Transactional
  override fun verifyToken(user: User, token: String) {
    val verificationToken = verificationTokenRepository.findByToken(token)
      ?: throw ResourceNotFoundException(AuthExceptionMessages.VERIFICATION_TOKEN_NOT_FOUND)

    if (verificationToken.user.id != user.id) {
      throw ResourceNotFoundException(AuthExceptionMessages.VERIFICATION_TOKEN_NOT_FOUND)
    }

    if (verificationToken.used) {
      throw ConflictException(AuthExceptionMessages.VERIFICATION_TOKEN_USED)
    }

    if (verificationToken.expiryDate.isBefore(Instant.now())) {
      throw ResourceGoneException(AuthExceptionMessages.VERIFICATION_TOKEN_EXPIRED)
    }

    verificationToken.used = true
    verificationToken.user.emailVerified = true

    verificationTokenRepository.save(verificationToken)
    userRepository.save(verificationToken.user)
  }

  override fun cleanExpiredOrUsedTokens(): Int {
    return verificationTokenRepository.deleteAllByExpiryDateBeforeOrUsedTrue(Instant.now())
  }
}