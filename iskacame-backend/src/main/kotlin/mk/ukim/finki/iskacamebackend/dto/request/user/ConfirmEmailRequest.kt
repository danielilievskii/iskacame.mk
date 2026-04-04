package mk.ukim.finki.iskacamebackend.dto.request.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class ConfirmEmailRequest(
  @field:NotBlank(message = "Email is required")
  @field:Email(message = "Invalid email address")
  val newEmail: String,

  @field:NotBlank(message = "Token is required")
  @field:Pattern(regexp = "\\d{6}", message = "Code must be 6 digits")
  val token: String
)
