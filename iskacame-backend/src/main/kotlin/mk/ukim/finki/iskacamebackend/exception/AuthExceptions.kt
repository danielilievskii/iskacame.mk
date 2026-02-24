package mk.ukim.finki.iskacamebackend.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class CustomAuthenticationException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.FORBIDDEN)
class CustomAccessDeniedException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.NOT_FOUND)
class VerificationTokenNotFoundException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.GONE)
class VerificationTokenExpiredException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.CONFLICT)
class VerificationTokenUsedException(message: String) : RuntimeException(message)