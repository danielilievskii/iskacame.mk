package mk.ukim.finki.iskacamebackend.utils

import org.springframework.stereotype.Component
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine
import java.util.Locale

@Component
class TemplateFactory(
  private val templateEngine: SpringTemplateEngine
) {
  fun render(templateName: String, templateModel: Map<String, Any>): String {
    val ctx = Context(Locale.ENGLISH)
    templateModel.forEach { (k, v) -> ctx.setVariable(k, v) }
    return templateEngine.process(templateName, ctx)
  }
}