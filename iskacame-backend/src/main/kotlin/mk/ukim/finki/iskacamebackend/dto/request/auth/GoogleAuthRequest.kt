package mk.ukim.finki.iskacamebackend.dto.request.auth

import jakarta.validation.constraints.NotBlank

data class GoogleAuthRequest(
  @field:NotBlank(message = "Authorization code is required")
  val code: String,

  @field:NotBlank(message = "Redirect URI is required")
  val redirectUri: String
)
