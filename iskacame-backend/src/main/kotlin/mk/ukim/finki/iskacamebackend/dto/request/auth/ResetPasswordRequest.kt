package mk.ukim.finki.iskacamebackend.dto.request.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ResetPasswordRequest(
  @field:NotBlank(message = "Email or username is required")
  val identifier: String,

  @field:NotBlank(message = "Token is required")
  @field:Pattern(regexp = "\\d{6}", message = "Code must be 6 digits")
  val token: String,

  @field:NotBlank(message = "Password is required")
  @field:Size(min = 6, message = "Password must be at least 6 characters long")
  val newPassword: String
)
