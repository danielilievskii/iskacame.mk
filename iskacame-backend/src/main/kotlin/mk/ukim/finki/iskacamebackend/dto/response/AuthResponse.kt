package mk.ukim.finki.iskacamebackend.dto.response

import mk.ukim.finki.iskacamebackend.dto.UserDTO

data class AuthResponse(
  val token: String,
  val user: UserDTO
)