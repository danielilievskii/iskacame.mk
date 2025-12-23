package mk.ukim.finki.iskacamebackend.common

object JWTConstants {
    val SECRET_KEY: String = System.getenv("JWT_SECRET_KEY")
    const val EXPIRATION_TIME: Long = 86_400_000L // 1 day
    const val BEARER_TOKEN_HEADER = "Authorization"
    const val TOKEN_PREFIX = "Bearer "
    const val INVITE_TOKEN_HEADER = "Invite-Token"
}