package mk.ukim.finki.iskacamebackend.service.intf

import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.model.domain.VerificationToken

interface VerificationTokenService {
  fun createVerificationToken(user: User): VerificationToken
  fun verifyToken(token: String)
  fun cleanExpiredOrUsedTokens() : Int
}