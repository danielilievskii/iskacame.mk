package mk.ukim.finki.iskacamebackend.service.impl

import jakarta.mail.internet.MimeMessage
import mk.ukim.finki.iskacamebackend.service.intf.MailService
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

/**
 * Implementation of MailService
 */
@Service
class MailServiceImpl(
  private val mailSender: JavaMailSender,

  @param:Value("\${spring.mail.username}")
  private val mailUsername: String
) : MailService {

  override fun sendEmail(to: String, subject: String, content: String): CompletableFuture<Void> {

    val message: MimeMessage = mailSender.createMimeMessage()
    val helper = MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8")
    helper.setTo(to)
    helper.setSubject(subject)
    helper.setText(content, true)
    helper.setFrom(mailUsername)

    mailSender.send(message)
    return CompletableFuture.completedFuture(null)
  }
}