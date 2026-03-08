package mk.ukim.finki.iskacamebackend.service.intf

import mk.ukim.finki.iskacamebackend.model.domain.User
import mk.ukim.finki.iskacamebackend.model.domain.VerificationToken

interface VerificationTokenService {

  /**
   * Creates and saves a new verification token for the given user.
   *
   * @param user the user that needs email verification
   * @return the created verification token
   */
  fun createVerificationToken(user: User): VerificationToken

  /**
   * Validates the token and verifies the user's email.
   *
   * Marks the token as used if it is valid and not expired.
   *
   * @param token the verification token string
   * @param user the user associated with the token
   *
   * @throws mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException if the token does not exist, or if the provided email does not match the token's email
   * @throws mk.ukim.finki.iskacamebackend.exception.ConflictException if the token was already used
   * @throws mk.ukim.finki.iskacamebackend.exception.ResourceGoneException if the token is expired
   */
  fun verifyToken(user: User, token: String)

  /**
   * Deletes all expired or already used tokens.
   *
   * @return number of deleted tokens
   */
  fun cleanExpiredOrUsedTokens(): Int
}