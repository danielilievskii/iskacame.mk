package mk.ukim.finki.iskacamebackend.utils

import kotlin.random.Random

object TokenGenerator {
  fun generateVerificationToken(): String {
    val code = Random.nextInt(100000, 1000000)
    return code.toString()
  }
}
