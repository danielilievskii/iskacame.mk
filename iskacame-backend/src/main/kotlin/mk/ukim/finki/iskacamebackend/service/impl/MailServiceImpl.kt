package mk.ukim.finki.iskacamebackend.service.impl

import jakarta.mail.internet.MimeMessage
import mk.ukim.finki.iskacamebackend.service.intf.MailService
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class MailServiceImpl(
  private val mailSender: JavaMailSender,
) : MailService {
  override fun sendEmail(to: String, subject: String, content: String): CompletableFuture<Void> {
    val message: MimeMessage = mailSender.createMimeMessage()
    val helper = MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8")
    helper.setTo(to)
    helper.setSubject(subject)
    helper.setText(content, true)
    helper.setFrom("iskacame@gmail.com")
    mailSender.send(message)
    return CompletableFuture.completedFuture(null)
  }
}