package mk.ukim.finki.iskacamebackend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "verification.token")
data class VerificationTokenConfig(
  val expirationTime: Long = 86400000
)
