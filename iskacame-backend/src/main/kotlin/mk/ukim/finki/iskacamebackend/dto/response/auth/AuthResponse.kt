package mk.ukim.finki.iskacamebackend.dto.response.auth

import mk.ukim.finki.iskacamebackend.dto.response.user.UserDto

data class AuthResponse(
  val token: String,
  val user: UserDto
)