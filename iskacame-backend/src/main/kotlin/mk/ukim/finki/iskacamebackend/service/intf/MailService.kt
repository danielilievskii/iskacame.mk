package mk.ukim.finki.iskacamebackend.service.intf

import java.util.concurrent.CompletableFuture

interface MailService {
  /**
   * Sends an email asynchronously.
   * Builds and dispatches a MIME message with the specified recipient, subject, and content.
   *
   * @param to The recipient's email address
   * @param subject The subject line of the email
   * @param content The body of the email (HTML supported)
   * @return A CompletableFuture that completes once the email has been sent
   */
  fun sendEmail(to: String, subject: String, content: String) : CompletableFuture<Void>
}