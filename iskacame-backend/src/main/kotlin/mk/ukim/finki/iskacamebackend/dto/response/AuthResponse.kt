package mk.ukim.finki.iskacamebackend.dto.response

import mk.ukim.finki.iskacamebackend.dto.UserDto

data class AuthResponse(
  val token: String,
  val user: UserDto
)