package mk.ukim.finki.iskacamebackend.events

data class MailEvent(
  val to: String,
  val subject: String,
  val templateName: String,
  val templateModel: Map<String, Any> = emptyMap()
)
