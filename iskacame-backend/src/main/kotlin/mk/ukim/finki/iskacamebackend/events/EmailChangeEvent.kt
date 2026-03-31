package mk.ukim.finki.iskacamebackend.events

import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.model.domain.VerificationToken

data class EmailChangeEvent(
  val user: User,
  val newEmail: String,
  val verificationToken: VerificationToken
)
