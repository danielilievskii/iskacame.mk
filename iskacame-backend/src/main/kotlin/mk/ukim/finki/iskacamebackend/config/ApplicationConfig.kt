package mk.ukim.finki.iskacamebackend.config

import com.cloudinary.Cloudinary
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfig {

  @Bean
  fun cloudinary(
    @Value("\${cloudinary.cloud_name}") cloudName: String,
    @Value("\${cloudinary.api_key}") apiKey: String,
    @Value("\${cloudinary.api_secret}") apiSecret: String
  ): Cloudinary {

    val config = mapOf(
      "cloud_name" to cloudName,
      "api_key" to apiKey,
      "api_secret" to apiSecret
    )

    return Cloudinary(config)
  }
}