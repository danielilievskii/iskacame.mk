package mk.ukim.finki.iskacamebackend.dto.request

data class SignUpRequest(
  val name: String,
  val username: String,
  val email: String,
  val password: String
)