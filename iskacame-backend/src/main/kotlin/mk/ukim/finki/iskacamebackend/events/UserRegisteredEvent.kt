package mk.ukim.finki.iskacamebackend.events

import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.model.domain.VerificationToken

data class UserRegisteredEvent(
  val user: User,
  val verificationToken: VerificationToken
)
