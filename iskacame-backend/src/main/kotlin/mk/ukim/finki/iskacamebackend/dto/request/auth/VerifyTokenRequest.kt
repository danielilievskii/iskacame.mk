package mk.ukim.finki.iskacamebackend.dto.request.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class VerifyTokenRequest(
  @field:NotBlank(message = "Email or username is required")
  val identifier: String,

  @field:NotBlank
  @field:Pattern(regexp = "\\d{6}", message = "Code must be 6 digits")
  val token: String
)
