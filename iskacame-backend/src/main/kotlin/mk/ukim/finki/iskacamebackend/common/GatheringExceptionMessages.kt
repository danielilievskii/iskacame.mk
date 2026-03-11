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

    const val GATHERING_PARTICIPATION_NOT_FOUND = "Gathering participation not found."

    const val USER_ALREADY_IN_GATHERING = "User is already participant of this gathering."
    const val USER_NOT_IN_GATHERING = "User is not participant of the gathering."

    const val INVITATION_ALREADY_SENT = "The invitation has already been sent"
    const val INVITATION_ALREADY_ACCEPTED = "The invitation has already been accepted"
    const val INVITATION_ALREADY_DECLINED = "The invitation has already been declined"
    const val INVITATION_NO_LONGER_VALID = "The invitation is no longer valid"

    const val CANNOT_INVITE_TO_CANCELLED_GATHERING = "Cannot invite users to a cancelled gathering."

    const val IMAGE_NOT_FOUND = "The requested image could not be found."


}