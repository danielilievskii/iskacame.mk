package mk.ukim.finki.iskacamebackend.common

/**
 * Definitions for gathering exception error messages.
 */
object GatheringExceptionMessages {

    const val GATHERING_NOT_FOUND = "The requested gathering could not be found."
    const val NOT_GATHERING_CREATOR = "Only the gathering creator can perform this action."
    const val INVALID_DATE_RANGE = "End date must be after start date."
    const val GATHERING_ALREADY_FINALIZED = "This gathering has already been finalized."
    const val GATHERING_ALREADY_CANCELLED = "This gathering has already been cancelled."
    const val CANNOT_EDIT_FINALIZED_GATHERING = "Cannot edit a finalized gathering."
    const val CANNOT_EDIT_CANCELLED_GATHERING = "Cannot edit a cancelled gathering."
    const val USER_NOT_PARTICIPANT = "You are not a participant of this gathering."
    const val INVALID_PARTICIPANTS = "At least one participant must be invited to the gathering."
}