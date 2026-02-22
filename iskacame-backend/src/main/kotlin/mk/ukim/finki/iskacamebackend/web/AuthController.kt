package mk.ukim.finki.iskacamebackend.web

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import mk.ukim.finki.iskacamebackend.dto.request.auth.SignInRequest
import mk.ukim.finki.iskacamebackend.dto.request.auth.SignUpRequest
import mk.ukim.finki.iskacamebackend.dto.response.auth.AuthResponse
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
  private val authService: AuthService
) {

  @PostMapping(path = ["/signUp", "/register"])
  @Operation(summary = "Registers a new user")
  fun signUp(@Valid @RequestBody signUpRequest: SignUpRequest): ResponseEntity<Void> {

    authService.signUp(signUpRequest)
    return ResponseEntity.status(HttpStatus.CREATED).build()
  }

  @PostMapping(path = ["/signIn", "/login"])
  @Operation(summary = "Logs in a user")
  fun signIn(@Valid @RequestBody signInRequest: SignInRequest): ResponseEntity<AuthResponse> {

    val response = authService.signIn(signInRequest)
    return ResponseEntity.ok(response)
  }
}