package mk.ukim.finki.iskacamebackend.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mk.ukim.finki.iskacamebackend.dto.request.RegisterDeviceTokenRequest
import mk.ukim.finki.iskacamebackend.service.intf.PushNotificationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/device-tokens")
@Tag(name = "Device Tokens", description = "Manages push notification device tokens")
class DeviceTokenController(
    private val pushNotificationService: PushNotificationService
) {

    @PostMapping
    @Operation(summary = "Register a device token for push notifications")
    fun registerToken(
        @Valid @RequestBody request: RegisterDeviceTokenRequest
    ): ResponseEntity<Void> {
        pushNotificationService.registerToken(request)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping
    @Operation(summary = "Unregister a device token")
    fun unregisterToken(@RequestParam token: String): ResponseEntity<Void> {
        pushNotificationService.unregisterToken(token)
        return ResponseEntity.noContent().build()
    }
}
