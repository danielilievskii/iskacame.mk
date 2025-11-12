package mk.ukim.finki.iskacamebackend.exception.handler

import mk.ukim.finki.iskacamebackend.exception.AuthenticationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthExceptionHandler {

  @ExceptionHandler(AuthenticationException::class)
  fun handleAuthenticationException(e: AuthenticationException): ResponseEntity<String> {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.message)
  }

  @ExceptionHandler(BadCredentialsException::class)
  fun handleBadCredentials(e: BadCredentialsException): ResponseEntity<String> {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.message)
  }

  @ExceptionHandler(mk.ukim.finki.iskacamebackend.exception.AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<String> {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
  }

  @ExceptionHandler(UsernameNotFoundException::class)
  fun handleUsernameNotFoundException(e: UsernameNotFoundException): ResponseEntity<String> {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
  }
}
