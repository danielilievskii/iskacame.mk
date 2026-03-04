package mk.ukim.finki.iskacamebackend.model.enums

/**
 * Enum representing the lifecycle state of a user's participation in a gathering.
 */
enum class ParticipationStatus {

    /**
     * User has been invited but has not responded yet.
     */
    INVITED,

    /**
     * User has accepted the invitation and is part of the gathering.
     */
    JOINED,

    /**
     * User declined the invitation.
     */
    DECLINED,

    /**
     * User voluntarily left the gathering after joining.
     */
    LEFT,

    /**
     * User was removed from the gathering.
     */
    REMOVED
}