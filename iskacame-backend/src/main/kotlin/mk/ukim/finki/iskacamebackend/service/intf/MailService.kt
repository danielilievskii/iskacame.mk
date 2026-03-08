package mk.ukim.finki.iskacamebackend.service.intf

import java.util.concurrent.CompletableFuture

interface MailService {
  fun sendEmail(to: String, subject: String, content: String) : CompletableFuture<Void>
}