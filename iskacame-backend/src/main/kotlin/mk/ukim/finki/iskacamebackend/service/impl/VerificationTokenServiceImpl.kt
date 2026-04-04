package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.common.AuthExceptionMessages
import mk.ukim.finki.iskacamebackend.config.VerificationTokenConfig
import mk.ukim.finki.iskacamebackend.exception.ConflictException
import mk.ukim.finki.iskacamebackend.exception.ResourceGoneException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.model.domain.VerificationToken
import mk.ukim.finki.iskacamebackend.model.enums.VerificationTokenPurpose
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
  private val verificationTokenConfig: VerificationTokenConfig
) : VerificationTokenService {

  override fun createVerificationToken(user: User, purpose: VerificationTokenPurpose): VerificationToken {
    var token: String
    var exists: Boolean

    do {
      token = TokenGenerator.generateVerificationToken()
      exists = verificationTokenRepository.findByTokenAndPurpose(token, purpose) != null
    } while (exists)

    val expiry = Instant.now().plusMillis(verificationTokenConfig.expirationTime)

    val verificationToken = VerificationToken(
      token = token,
      user = user,
      expiryDate = expiry,
      used = false,
      purpose = purpose
    )

    return verificationTokenRepository.save(verificationToken)
  }

  @Transactional
  override fun consumeToken(user: User, token: String, purpose: VerificationTokenPurpose) {
    val verificationToken = verificationTokenRepository.findByTokenAndPurpose(token, purpose)
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

    verificationTokenRepository.save(verificationToken)
  }

  override fun cleanExpiredOrUsedTokens(): Int {
    return verificationTokenRepository.deleteAllByExpiryDateBeforeOrUsedTrue(Instant.now())
  }
}