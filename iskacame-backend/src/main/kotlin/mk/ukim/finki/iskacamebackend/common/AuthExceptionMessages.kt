package mk.ukim.finki.iskacamebackend.common

/**
 * Definitions for authentication exception messages.
 */
object AuthExceptionMessages {

  const val TOKEN_EXPIRED = "Your session has expired. Please log in again."
  const val INVALID_SIGNATURE = "Invalid authentication token."
  const val INVALID_TOKEN = "Invalid authentication token."
  const val UNSUPPORTED_TOKEN = "Unsupported authentication token."
  const val JWT_ERROR = "Authentication token error. Please log in again."

  const val INVALID_CREDENTIALS = "Invalid email or password."
  const val ACCOUNT_DISABLED = "Your account has been disabled."
  const val AUTHENTICATION_ERROR = "Authentication error. Please log in again."
  const val INVALID_PRINCIPAL = "Invalid authentication details. Please log in again."

  const val ACCESS_DENIED = "You do not have permission to perform this action."

  const val EMAIL_TAKEN = "This email is already associated with an account."
  const val USERNAME_TAKEN = "This username is already associated with an account."

  const val VERIFICATION_TOKEN_NOT_FOUND = "Verification token not found."
  const val VERIFICATION_TOKEN_EXPIRED = "Verification token has expired."
  const val VERIFICATION_TOKEN_USED = "Verification token has already been used."

  const val EMAIL_ALREADY_VERIFIED = "Email is already verified."
  const val EMAIL_NOT_VERIFIED = "Email is not verified."
}