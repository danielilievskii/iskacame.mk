package mk.ukim.finki.iskacamebackend.dto.request

import jakarta.validation.constraints.NotBlank

data class RegisterDeviceTokenRequest(
    @field:NotBlank
    val token: String,
    val platform: String? = null
)
