package mk.ukim.finki.iskacamebackend.exception.handler

import mk.ukim.finki.iskacamebackend.exception.ConflictException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException::class)
  fun handleIllegalArgumentException(e: Exception): ResponseEntity<String> {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
  }

  @ExceptionHandler(IllegalStateException::class)
  fun handleIllegalStateException(e: Exception): ResponseEntity<String> {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
  }

  @ExceptionHandler(ResourceNotFoundException::class)
  fun handleResourceNotFoundException(e: ResourceNotFoundException): ResponseEntity<String> {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
  }

  @ExceptionHandler(ConflictException::class)
  fun handleConflictException(e: ConflictException): ResponseEntity<String> {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(e.message)
  }
}