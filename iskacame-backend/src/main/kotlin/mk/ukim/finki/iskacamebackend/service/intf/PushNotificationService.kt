package mk.ukim.finki.iskacamebackend.service.intf

import mk.ukim.finki.iskacamebackend.dto.request.RegisterDeviceTokenRequest

interface PushNotificationService {

    fun registerToken(request: RegisterDeviceTokenRequest)

    fun unregisterToken(token: String)

    fun sendPushNotification(recipientIds: List<Long>, title: String, body: String, data: Map<String, String>? = null)
}
