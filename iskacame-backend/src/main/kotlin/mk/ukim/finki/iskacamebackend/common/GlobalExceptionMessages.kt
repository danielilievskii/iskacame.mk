package mk.ukim.finki.iskacamebackend.common

/**
 * Definitions for global exception error messages.
 */
object GlobalExceptionMessages {

  const val INVALID_ARGUMENT = "One or more provided arguments are invalid."
  const val ILLEGAL_STATE = "The request cannot be processed in the current state."

  const val BAD_REQUEST = "The request could not be understood or was missing required parameters."
  const val CONFLICT = "The request could not be completed due to a conflict with the current state of the resource."

  const val RESOURCE_NOT_FOUND = "The requested resource could not be found."
  const val USER_NOT_FOUND = "The requested user does not exist."

  const val INTERNAL_SERVER_ERROR = "An unexpected error occurred. Please try again later."
}