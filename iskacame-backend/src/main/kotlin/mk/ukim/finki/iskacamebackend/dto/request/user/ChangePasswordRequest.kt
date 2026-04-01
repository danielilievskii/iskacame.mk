package mk.ukim.finki.iskacamebackend.dto.request.user

import jakarta.validation.constraints.NotBlank

data class ChangePasswordRequest(
  @field:NotBlank(message = "Current password is required")
  val currentPassword: String
)