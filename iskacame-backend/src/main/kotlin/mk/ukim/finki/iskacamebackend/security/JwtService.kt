package mk.ukim.finki.iskacamebackend.security

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date

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

  /**
   * Returns the signing key for HMAC SHA token verification.
   * Decodes the Base64-encoded secret and generates a `Key` instance.
   *
   * @return the secret key for signing and verifying JWT tokens
   */
  private fun getSigningKey(): Key {

    val keyBytes = Decoders.BASE64.decode(jwtSecret)
    return Keys.hmacShaKeyFor(keyBytes)
  }


  /**
   * Generates a JWT token for the given authenticated user.
   * The token contains subject (username), `userId` and `email`
   * as custom claims `issuedAt` and `expiration` timestamps
   *
   * @param userPrincipal the authenticated user's details
   * @return a signed JWT token as a `String`
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
      .signWith(getSigningKey())
      .compact()
  }

  /**
   * Extracts the email from a JWT token by parsing it using the signing key.
   *
   * @param token the JWT token string
   * @return the user's email contained in the token
   * @throws JwtException if the token is invalid or cannot be parsed
   */
  fun getEmailFromToken(token: String): String {

    val claims = Jwts.parser()
      .verifyWith(getSigningKey() as javax.crypto.SecretKey)
      .build()
      .parseSignedClaims(token)
      .payload

    return claims.subject
  }

  /**
   * Validates a JWT token's signature and structure by verifying that
   * the token is correctly signed with the configured secret key and
   * a valid structure and has not been tampered with.
   *
   * @param token the JWT token string
   * @throws JwtException if the token is invalid, expired, or unsupported
   */
  fun validateToken(token: String) {

    Jwts.parser()
      .verifyWith(getSigningKey() as javax.crypto.SecretKey)
      .build()
      .parseSignedClaims(token)
  }
}