package mk.ukim.finki.iskacamebackend.listener

import mk.ukim.finki.iskacamebackend.events.MailEvent
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
  fun handleMailEvent(event: MailEvent) {
    val html = templateFactory.render(event.templateName, event.templateModel)
    mailService.sendEmail(event.to, event.subject, html)
  }
}