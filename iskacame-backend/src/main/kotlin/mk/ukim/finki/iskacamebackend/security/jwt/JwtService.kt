package mk.ukim.finki.iskacamebackend.security.jwt

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import mk.ukim.finki.iskacamebackend.security.principal.UserPrincipal
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date
import javax.crypto.SecretKey

/**
 * Provides utilities for generating, parsing, and validating JWT tokens.
 * Uses HMAC SHA signing algorithm with a secret key defined in application properties.
 */
@Component
class JwtService {

  @Value("\${jwt.secret}")
  private lateinit var jwtSecret: String

  @Value("\${jwt.expiration.ms}")
  private var jwtExpirationMs: Long = 86400000

  @Value("\${jwt.refresh.expiration.ms:2592000000}")
  private var jwtRefreshExpirationMs: Long = 2592000000

  private companion object {
    const val TOKEN_TYPE_CLAIM = "type"
    const val ACCESS_TOKEN_TYPE = "access"
    const val REFRESH_TOKEN_TYPE = "refresh"
  }

  fun getRefreshExpirationMs(): Long = jwtRefreshExpirationMs

  /**
   * Returns the signing key for HMAC SHA token verification.
   * Decodes the Base64-encoded secret and generates a `Key` instance.
   */
  private fun getSigningKey(): Key {

    val keyBytes = Decoders.BASE64.decode(jwtSecret)
    return Keys.hmacShaKeyFor(keyBytes)
  }


  /**
   * Generates a short-lived access JWT token for the given authenticated user.
   */
  fun generateToken(userPrincipal: UserPrincipal): String {
    val now = Date()
    val expiryDate = Date(now.time + jwtExpirationMs)

    return Jwts.builder()
      .subject(userPrincipal.email)
      .issuedAt(now)
      .expiration(expiryDate)
      .claim("userId", userPrincipal.id)
      .claim("roles", userPrincipal.authorities)
      .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
      .signWith(getSigningKey())
      .compact()
  }

  /**
   * Generates a long-lived refresh JWT token for the given authenticated user.
   * The token carries only the subject and a `type=refresh` claim so it can be
   * distinguished from access tokens at validation time.
   */
  fun generateRefreshToken(userPrincipal: UserPrincipal): String {
    val now = Date()
    val expiryDate = Date(now.time + jwtRefreshExpirationMs)

    return Jwts.builder()
      .subject(userPrincipal.email)
      .issuedAt(now)
      .expiration(expiryDate)
      .claim("userId", userPrincipal.id)
      .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
      .signWith(getSigningKey())
      .compact()
  }

  /**
   * Extracts the email from a JWT token.
   */
  fun getEmailFromToken(token: String): String {

    val claims = Jwts.parser()
      .verifyWith(getSigningKey() as SecretKey)
      .build()
      .parseSignedClaims(token)
      .payload

    return claims.subject
  }

  /**
   * Validates a JWT token's signature and structure.
   */
  fun validateToken(token: String) {

    Jwts.parser()
      .verifyWith(getSigningKey() as SecretKey)
      .build()
      .parseSignedClaims(token)
  }

  /**
   * Validates a token and asserts that it is a refresh token (has `type=refresh` claim).
   *
   * @throws JwtException when the signature/structure is invalid or the token is not a refresh token
   */
  fun validateRefreshToken(token: String) {
    val claims = Jwts.parser()
      .verifyWith(getSigningKey() as SecretKey)
      .build()
      .parseSignedClaims(token)
      .payload

    val type = claims[TOKEN_TYPE_CLAIM] as? String
    if (type != REFRESH_TOKEN_TYPE) {
      throw JwtException("Provided token is not a refresh token")
    }
  }
}
