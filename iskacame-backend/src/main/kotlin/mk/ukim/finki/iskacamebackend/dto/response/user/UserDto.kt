package mk.ukim.finki.iskacamebackend.dto.response.user

data class UserDto (
  val id: Long,
  val name: String,
  val username: String,
  val avatarUrl: String?,
  val phone: String?
)