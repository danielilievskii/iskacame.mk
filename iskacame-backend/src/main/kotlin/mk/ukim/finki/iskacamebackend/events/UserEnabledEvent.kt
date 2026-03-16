package mk.ukim.finki.iskacamebackend.events

import mk.ukim.finki.iskacamebackend.model.domain.User

data class UserEnabledEvent(
  val user: User,
)

