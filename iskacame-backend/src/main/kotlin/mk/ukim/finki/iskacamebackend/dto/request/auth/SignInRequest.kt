package mk.ukim.finki.iskacamebackend.dto.request.auth

import jakarta.validation.constraints.NotBlank

data class SignInRequest (
  @field:NotBlank(message = "Email or username is required")
  val identifier: String,

  @field:NotBlank(message = "Password is required")
  val password: String
)