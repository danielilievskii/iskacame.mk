package mk.ukim.finki.iskacamebackend.events

import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.model.domain.VerificationToken

data class PasswordResetEvent(
  val user: User,
  val verificationToken: VerificationToken
)
