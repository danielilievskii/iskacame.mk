package mk.ukim.finki.iskacamebackend.dto.response.user

data class UserSearchDto (
    val id: Long,
    val name: String,
    val username: String,
    val avatarUrl: String?
)