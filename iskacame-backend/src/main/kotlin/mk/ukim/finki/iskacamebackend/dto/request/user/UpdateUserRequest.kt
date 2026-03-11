package mk.ukim.finki.iskacamebackend.dto.request.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateUserRequest(
  @field:NotBlank(message = "Name is required")
  val name: String,

  @field:NotBlank(message = "Username is required")
  @field:Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters long")
  @field:Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "Username can only contain letters, numbers, underscores and dashes")
  val username: String,

  @field:Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be in valid international format (E.164)")
  val phone: String? = null,
)
