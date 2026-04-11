package mk.ukim.finki.iskacamebackend.service.intf

import mk.ukim.finki.iskacamebackend.model.domain.RefreshToken
import mk.ukim.finki.iskacamebackend.model.domain.User

interface RefreshTokenService {

  /**
   * Persists a new refresh token for the given user.
   */
  fun create(user: User, token: String): RefreshToken

  /**
   * Validates the stored refresh token, rotates it (revokes the old one and issues a new one),
   * and returns both the new access token and refresh token in an [AuthService]-compatible response.
   *
   * @throws mk.ukim.finki.iskacamebackend.exception.CustomAuthenticationException when the token is missing,
   * revoked, expired, or does not belong to an existing user
   */
  fun rotate(oldToken: String): Pair<String, String>

  /**
   * Revokes all active refresh tokens for the given user (e.g. on logout).
   */
  fun revokeAllForUser(user: User)

  /**
   * Revokes a single refresh token by its value. No-op if the token does not exist.
   */
  fun revoke(token: String)
}
