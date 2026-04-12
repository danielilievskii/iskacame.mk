package mk.ukim.finki.iskacamebackend.service.impl

import io.jsonwebtoken.JwtException
import mk.ukim.finki.iskacamebackend.common.AuthExceptionMessages
import mk.ukim.finki.iskacamebackend.common.GlobalExceptionMessages
import mk.ukim.finki.iskacamebackend.exception.CustomAuthenticationException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.mapper.UserMapper
import mk.ukim.finki.iskacamebackend.model.domain.RefreshToken
import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.repository.RefreshTokenRepository
import mk.ukim.finki.iskacamebackend.repository.UserRepository
import mk.ukim.finki.iskacamebackend.security.jwt.JwtService
import mk.ukim.finki.iskacamebackend.service.intf.RefreshTokenService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class RefreshTokenServiceImpl(
  private val refreshTokenRepository: RefreshTokenRepository,
  private val userRepository: UserRepository,
  private val userMapper: UserMapper,
  private val jwtService: JwtService
) : RefreshTokenService {

  override fun create(user: User, token: String): RefreshToken {
    val entity = RefreshToken(
      token = token,
      user = user,
      expiresAt = Instant.now().plusMillis(jwtService.getRefreshExpirationMs()),
      revoked = false
    )
    return refreshTokenRepository.save(entity)
  }

  @Transactional
  override fun rotate(oldToken: String): Pair<String, String> {

    try {
      jwtService.validateRefreshToken(oldToken)
    } catch (ex: JwtException) {
      throw CustomAuthenticationException(AuthExceptionMessages.INVALID_TOKEN)
    }

    val stored = refreshTokenRepository.findByToken(oldToken)
      ?: throw CustomAuthenticationException(AuthExceptionMessages.INVALID_TOKEN)

    if (stored.revoked) {
      throw CustomAuthenticationException(AuthExceptionMessages.INVALID_TOKEN)
    }

    if (stored.expiresAt.isBefore(Instant.now())) {
      throw CustomAuthenticationException(AuthExceptionMessages.TOKEN_EXPIRED)
    }

    val user = stored.user
    val freshUser = userRepository.findByEmail(user.email)
      ?: throw ResourceNotFoundException(GlobalExceptionMessages.USER_NOT_FOUND)

    val principal = userMapper.toUserPrincipal(freshUser)

    stored.revoked = true
    refreshTokenRepository.save(stored)

    val newAccessToken = jwtService.generateToken(principal)
    val newRefreshToken = jwtService.generateRefreshToken(principal)
    create(freshUser, newRefreshToken)

    return newAccessToken to newRefreshToken
  }

  @Transactional
  override fun revokeAllForUser(user: User) {
    refreshTokenRepository.revokeAllByUser(user)
  }

  @Transactional
  override fun revoke(token: String) {
    val stored = refreshTokenRepository.findByToken(token) ?: return
    if (!stored.revoked) {
      stored.revoked = true
      refreshTokenRepository.save(stored)
    }
  }

  @Transactional
  override fun cleanExpiredOrRevoked(): Int {
    return refreshTokenRepository.deleteAllExpiredOrRevoked(Instant.now())
  }
}
