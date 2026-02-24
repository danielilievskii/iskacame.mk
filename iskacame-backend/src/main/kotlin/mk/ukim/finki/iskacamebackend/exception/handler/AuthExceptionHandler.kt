package mk.ukim.finki.iskacamebackend.exception.handler

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import mk.ukim.finki.iskacamebackend.common.AuthExceptionMessages
import mk.ukim.finki.iskacamebackend.common.GlobalExceptionMessages
import mk.ukim.finki.iskacamebackend.exception.CustomAccessDeniedException
import mk.ukim.finki.iskacamebackend.exception.CustomAuthenticationException
import mk.ukim.finki.iskacamebackend.exception.VerificationTokenExpiredException
import mk.ukim.finki.iskacamebackend.exception.VerificationTokenNotFoundException
import mk.ukim.finki.iskacamebackend.exception.VerificationTokenUsedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthExceptionHandler {

  /**
   * Handles expired JWT tokens.
   */
  @ExceptionHandler(ExpiredJwtException::class)
  fun handleExpiredJwt(): ResponseEntity<String> {

    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(AuthExceptionMessages.TOKEN_EXPIRED)
  }

  /**
   * Handles invalid JWT signatures.
   */
  @ExceptionHandler(SignatureException::class)
  fun handleInvalidSignature(): ResponseEntity<String> {

    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(AuthExceptionMessages.INVALID_SIGNATURE)
  }

  /**
   * Handles malformed JWT tokens.
   * (Corrupted token, incorrect format, encoding issues).
   */
  @ExceptionHandler(MalformedJwtException::class)
  fun handleMalformedJwt(): ResponseEntity<String> {

    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(AuthExceptionMessages.INVALID_TOKEN)
  }

  /**
   * Handles unsupported JWT types.
   */
  @ExceptionHandler(UnsupportedJwtException::class)
  fun handleUnsupportedJwt(): ResponseEntity<String> {

    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(AuthExceptionMessages.UNSUPPORTED_TOKEN)
  }

  /**
   * Catch-all handler for any other JWT-related exceptions.
   */
  @ExceptionHandler(JwtException::class)
  fun handleGenericJwt(): ResponseEntity<String> {

    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(AuthExceptionMessages.JWT_ERROR)
  }

  /**
   * Handles invalid username or password during login.
   */
  @ExceptionHandler(BadCredentialsException::class)
  fun handleBadCredentials(): ResponseEntity<String> {

    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(AuthExceptionMessages.INVALID_CREDENTIALS)
  }

  /**
   * Handles username not found exceptions.
   */
  @ExceptionHandler(UsernameNotFoundException::class)
  fun handleUsernameNotFoundException(): ResponseEntity<String> {

    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(GlobalExceptionMessages.USER_NOT_FOUND)
  }

  /**
   * Handles generic Spring Security authentication failures.
   */
  @ExceptionHandler(AuthenticationException::class)
  fun handleAuthentication(): ResponseEntity<String> {

    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(AuthExceptionMessages.AUTHENTICATION_ERROR)
  }

  /**
   * Handles attempts to access resources with disabled accounts.
   */
  @ExceptionHandler(DisabledException::class)
  fun handleDisabled(): ResponseEntity<String> {

    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(AuthExceptionMessages.ACCOUNT_DISABLED)
  }

  /**
   * Handles custom authentication exceptions from the application.
   */
  @ExceptionHandler(CustomAuthenticationException::class)
  fun handleCustomAuthentication(exception: CustomAuthenticationException): ResponseEntity<String> {

    val message = exception.message ?: AuthExceptionMessages.AUTHENTICATION_ERROR

    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(message)
  }

  /**
   * Handles custom access denied exceptions from the application.
   */
  @ExceptionHandler(CustomAccessDeniedException::class)
  fun handleCustomAccessDenied(exception: CustomAccessDeniedException): ResponseEntity<String> {

    val message = exception.message ?: AuthExceptionMessages.ACCESS_DENIED

    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(message)
  }

  /**
   * Handles invalid verification token for the application.
   */
  @ExceptionHandler(VerificationTokenNotFoundException::class)
  fun handleVerificationTokenNotFound(ex: VerificationTokenNotFoundException): ResponseEntity<String> {

    val message = ex.message ?: AuthExceptionMessages.VERIFICATION_TOKEN_NOT_FOUND

    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(message)
  }

  /**
   * Handles invalid verification token for the application.
   */
  @ExceptionHandler(VerificationTokenExpiredException::class)
  fun handleVerificationTokenExpired(ex: VerificationTokenExpiredException): ResponseEntity<String> {

    val message = ex.message ?: AuthExceptionMessages.VERIFICATION_TOKEN_EXPIRED

    return ResponseEntity
      .status(HttpStatus.GONE)
      .body(message)
  }

  /**
   * Handles invalid verification token for the application.
   */
  @ExceptionHandler(VerificationTokenUsedException::class)
  fun handleVerificationTokenAlreadyUsed(ex: VerificationTokenUsedException): ResponseEntity<String> {

    val message = ex.message ?: AuthExceptionMessages.VERIFICATION_TOKEN_USED

    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(message)
  }
}
