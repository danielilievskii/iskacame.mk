package mk.ukim.finki.iskacamebackend.exception.handler

import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.web.bind.annotation.ControllerAdvice

@ControllerAdvice
class WebSocketExceptionHandler {

    @MessageExceptionHandler(AccessDeniedException::class)
    @SendToUser("/queue/errors")
    fun handleAccessDenied(ex: AccessDeniedException): WebSocketErrorResponse {

        return WebSocketErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            message = ex.message ?: "Access denied"
        )
    }

    @MessageExceptionHandler(ResourceNotFoundException::class)
    @SendToUser("/queue/errors")
    fun handleNotFound(ex: ResourceNotFoundException): WebSocketErrorResponse {

        return WebSocketErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = ex.message ?: "Resource not found"
        )
    }

    @MessageExceptionHandler(Exception::class)
    @SendToUser("/queue/errors")
    fun handleGeneral(ex: Exception): WebSocketErrorResponse {

        return WebSocketErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "An unexpected error occurred"
        )
    }
}

data class WebSocketErrorResponse(
    val status: Int,
    val message: String
)
