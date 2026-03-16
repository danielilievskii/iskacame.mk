package mk.ukim.finki.iskacamebackend.dto.request.auth

import jakarta.validation.constraints.NotBlank

data class ResendTokenRequest (
  @field:NotBlank(message = "Email or username is required")
  val identifier: String
)