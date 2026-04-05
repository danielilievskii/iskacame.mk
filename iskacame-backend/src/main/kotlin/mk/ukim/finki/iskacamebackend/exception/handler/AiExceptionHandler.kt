package mk.ukim.finki.iskacamebackend.exception.handler

import com.google.genai.errors.ClientException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AiExceptionHandler {

    @ExceptionHandler(ClientException::class)
    fun handleGeminiException(ex: ClientException): ResponseEntity<String> {

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ex.message)
    }
}