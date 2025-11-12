package mk.ukim.finki.iskacamebackend.exception

import mk.ukim.finki.iskacamebackend.common.ExceptionMessages
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class AuthenticationException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.FORBIDDEN)
class AccessDeniedException(message: String = ExceptionMessages.ACCESS_DENIED) : RuntimeException(message)