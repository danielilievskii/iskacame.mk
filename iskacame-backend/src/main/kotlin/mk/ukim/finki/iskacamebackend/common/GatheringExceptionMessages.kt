package mk.ukim.finki.iskacamebackend.common

/**
 * Definitions for gathering exception error messages.
 */
object GatheringExceptionMessages {

    const val GATHERING_NOT_FOUND = "The requested gathering could not be found."
    const val TITLE_REQUIRED = "Title must not be blank."
    const val TITLE_MAX_LENGTH = "Title must not exceed 100 characters."
    const val DESCRIPTION_MAX_LENGTH = "Description must not exceed 500 characters."
    const val START_DATE_REQUIRED = "Start date is required."
    const val END_DATE_REQUIRED = "End date is required."
    const val INVALID_DATE_RANGE = "End date must be after start date."
    const val GATHERING_ALREADY_FINALIZED = "This gathering has already been finalized."
    const val GATHERING_ALREADY_CANCELLED = "This gathering has already been cancelled."
    const val CANNOT_EDIT_FINALIZED_GATHERING = "Cannot edit a finalized gathering."
    const val CANNOT_EDIT_CANCELLED_GATHERING = "Cannot edit a cancelled gathering."
    const val CREATOR_IN_PARTICIPANTS = "Creator cannot be added as a participant."
    const val INVALID_PARTICIPANTS = "At least one participant must be invited to the gathering."
    const val GATHERING_INVITATION_NOT_FOUND = "The requested gathering invitation could not be found."
}