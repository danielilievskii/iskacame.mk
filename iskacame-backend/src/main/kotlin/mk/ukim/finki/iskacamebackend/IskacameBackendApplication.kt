package mk.ukim.finki.iskacamebackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class IskacameBackendApplication

fun main(args: Array<String>) {
    runApplication<IskacameBackendApplication>(*args)
}
