package mk.ukim.finki.iskacamebackend.exception.handler

import mk.ukim.finki.iskacamebackend.common.GlobalExceptionMessages
import mk.ukim.finki.iskacamebackend.exception.BadRequestException
import mk.ukim.finki.iskacamebackend.exception.StorageException
import mk.ukim.finki.iskacamebackend.exception.ConflictException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

  /**
   * Handles invalid method arguments.
   */
  @ExceptionHandler(IllegalArgumentException::class)
  fun handleIllegalArgument(exception: IllegalArgumentException): ResponseEntity<String> {

    val message = exception.message ?: GlobalExceptionMessages.INVALID_ARGUMENT

    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(message)
  }

  /**
   * Handles illegal state exceptions.
   */
  @ExceptionHandler(IllegalStateException::class)
  fun handleIllegalState(exception: IllegalStateException): ResponseEntity<String> {

    val message = exception.message ?: GlobalExceptionMessages.ILLEGAL_STATE

    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(message)
  }

  /**
   * Handles resource not found exceptions.
   */
  @ExceptionHandler(ResourceNotFoundException::class)
  fun handleResourceNotFound(exception: ResourceNotFoundException): ResponseEntity<String> {

    val message = exception.message ?: GlobalExceptionMessages.RESOURCE_NOT_FOUND

    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(message)
  }

  /**
   * Handles conflict exceptions.
   */
  @ExceptionHandler(ConflictException::class)
  fun handleConflict(exception: ConflictException): ResponseEntity<String> {

    val message = exception.message ?: GlobalExceptionMessages.CONFLICT

    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(message)
  }

  /**
   * Handles bad request exceptions.
   */
  @ExceptionHandler(BadRequestException::class)
  fun handleBadRequest(exception: BadRequestException): ResponseEntity<String> {

    val message = exception.message ?: GlobalExceptionMessages.BAD_REQUEST

    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(message)
  }

  /**
   * Handles all unexpected storage exceptions.
   */
  @ExceptionHandler(StorageException::class)
  fun handleStorage(exception: StorageException): ResponseEntity<String> {

    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(exception.message)
  }

  /**
   * Handles all unexpected exceptions not caught by specific handlers.
   */
  @ExceptionHandler(Exception::class)
  fun handleGlobal(): ResponseEntity<String> {

    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(GlobalExceptionMessages.INTERNAL_SERVER_ERROR)
  }

  /**
   * Handles validation errors from @Valid annotated DTOs.
   */
  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidationErrors(exception: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
    val errors = exception.bindingResult.fieldErrors
      .groupBy { it.field }
      .mapValues { (_, errs) ->
        errs.mapNotNull { it.defaultMessage }.joinToString("; ").ifBlank { "Invalid value" }
      }

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors)
  }
}