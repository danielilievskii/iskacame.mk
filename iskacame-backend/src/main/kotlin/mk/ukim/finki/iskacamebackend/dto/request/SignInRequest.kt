package mk.ukim.finki.iskacamebackend.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SignInRequest (
  @field:Email(message = "Invalid email address")
  @field:NotBlank(message = "Email is required")
  val email: String,

  @field:NotBlank(message = "Password is required")
  val password: String
)