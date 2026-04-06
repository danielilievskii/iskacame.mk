package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.dto.request.RegisterDeviceTokenRequest
import mk.ukim.finki.iskacamebackend.model.domain.DeviceToken
import mk.ukim.finki.iskacamebackend.repository.DeviceTokenRepository
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import mk.ukim.finki.iskacamebackend.service.intf.PushNotificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient

@Service
class PushNotificationServiceImpl(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val authService: AuthService
) : PushNotificationService {

    private val logger = LoggerFactory.getLogger(PushNotificationServiceImpl::class.java)
    private val webClient = WebClient.builder()
        .baseUrl("https://exp.host")
        .build()

    @Transactional
    override fun registerToken(request: RegisterDeviceTokenRequest) {
        val currentUser = authService.getCurrentUser()

        val existing = deviceTokenRepository.findByToken(request.token)
        if (existing != null) {
            // Update ownership if token already registered by different user
            existing.user = currentUser
            existing.platform = request.platform
            deviceTokenRepository.save(existing)
            return
        }

        val deviceToken = DeviceToken(
            user = currentUser,
            token = request.token,
            platform = request.platform
        )
        deviceTokenRepository.save(deviceToken)
    }

    @Transactional
    override fun unregisterToken(token: String) {
        deviceTokenRepository.deleteByToken(token)
    }

    override fun sendPushNotification(
        recipientIds: List<Long>,
        title: String,
        body: String,
        data: Map<String, String>?
    ) {
        if (recipientIds.isEmpty()) return

        val tokens = deviceTokenRepository.findAllByUserIdIn(recipientIds)
        if (tokens.isEmpty()) return

        val messages = tokens.map { token ->
            buildMap {
                put("to", token.token)
                put("title", title)
                put("body", body)
                put("sound", "default")
                if (data != null) {
                    put("data", data)
                }
            }
        }

        try {
            webClient.post()
                .uri("/--/api/v2/push/send")
                .header("Content-Type", "application/json")
                .bodyValue(messages)
                .retrieve()
                .bodyToMono(String::class.java)
                .subscribe(
                    { logger.debug("Push notifications sent successfully") },
                    { error -> logger.error("Failed to send push notifications", error) }
                )
        } catch (e: Exception) {
            logger.error("Failed to send push notifications", e)
        }
    }
}
