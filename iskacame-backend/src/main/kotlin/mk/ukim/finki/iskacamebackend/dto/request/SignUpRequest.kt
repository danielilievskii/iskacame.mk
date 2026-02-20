package mk.ukim.finki.iskacamebackend.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class SignUpRequest(
  @field:NotBlank(message = "Name is required")
  val name: String,

  @field:NotBlank(message = "Username is required")
  @field:Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters long")
  @field:Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "Username can only contain letters, numbers, underscores and dashes")
  val username: String,

  @field:NotBlank(message = "Email is required")
  @field:Email(message = "Invalid email address")
  val email: String,

  @field:NotBlank(message = "Password is required")
  @field:Size(min = 6, message = "Password must be at least 6 characters long")
  val password: String
)