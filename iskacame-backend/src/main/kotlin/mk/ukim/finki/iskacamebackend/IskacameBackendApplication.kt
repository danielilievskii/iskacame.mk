package mk.ukim.finki.iskacamebackend

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@SpringBootApplication
class IskacameBackendApplication

fun main(args: Array<String>) {
    val dotenv = dotenv {
        directory = "./"
        ignoreIfMissing = true
    }

    dotenv.entries().forEach {
        System.setProperty(it.key, it.value)
    }

    runApplication<IskacameBackendApplication>(*args)
}
