package mk.ukim.finki.iskacamebackend.dto.request.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ChangeEmailRequest(
  @field:NotBlank(message = "New email is required")
  @field:Email(message = "Invalid email address")
  val newEmail: String
)
