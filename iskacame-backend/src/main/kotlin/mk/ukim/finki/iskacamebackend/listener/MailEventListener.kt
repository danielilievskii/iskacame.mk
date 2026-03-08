package mk.ukim.finki.iskacamebackend.listener

import mk.ukim.finki.iskacamebackend.events.UserRegisteredEvent
import mk.ukim.finki.iskacamebackend.utils.TemplateFactory
import mk.ukim.finki.iskacamebackend.service.intf.MailService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class MailEventListener(
  private val templateFactory: TemplateFactory,
  private val mailService: MailService
) {
  @Async
  @EventListener
  fun onUserRegistered(event: UserRegisteredEvent) {
    val templateModel = mapOf(
      "name" to event.user.name,
      "verificationCode" to event.verificationToken.token
    )

    val html = templateFactory.render("verification-email", templateModel)

    mailService.sendEmail(event.user.email, "Iskacame.mk verification code", html)
  }
}