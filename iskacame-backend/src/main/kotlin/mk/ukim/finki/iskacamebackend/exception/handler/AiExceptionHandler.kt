package mk.ukim.finki.iskacamebackend.exception.handler

import com.google.genai.errors.ClientException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AiExceptionHandler {

    @ExceptionHandler(ClientException::class)
    fun handleGeminiException(ex: ClientException): ResponseEntity<String> {
        return if (ex.message?.contains("429") == true) {
            ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("AI quota exceeded. Please try again later.")
        } else {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("AI service error: ${ex.message}")
        }
    }
}