package mk.ukim.finki.iskacamebackend.dto.request.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class VerifyTokenRequest(
  @field:Email(message = "Invalid email address")
  @field:NotBlank(message = "Email is required")
  val email: String,

  @field:NotBlank
  @field:Pattern(regexp = "\\d{6}", message = "Code must be 6 digits")
  val token: String
)
