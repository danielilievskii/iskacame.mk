package mk.ukim.finki.iskacamebackend.service

/**
 * Security service responsible for evaluating a user's permissions within a gathering.
 * This service is typically used in method-level security expressions (e.g. @PreAuthorize).
 */
interface PermissionService {

    /**
     * Checks whether a user is a participant of the specified gathering.
     *
     * @param gatheringId The ID of the gathering.
     * @param userId The ID of the user.
     * @return `true` if the user is associated with the gathering, otherwise `false`.
     */
    fun isGatheringParticipant(gatheringId: Long, userId: Long): Boolean

    /**
     * Checks whether a user is creator of the specified gathering.
     *
     * @param gatheringId The ID of the gathering.
     * @param userId The ID of the user.
     * @return `true` if the user is creator of the gathering, otherwise `false`.
     */
    fun isGatheringCreator(gatheringId: Long, userId: Long): Boolean

    /**
     * Checks whether the user owns the specified participation.
     *
     * @param participationId the ID of the gathering participation
     * @param userId the ID of the user
     * @return `true` if the participation belongs to the user, `false` otherwise
     */
    fun isParticipationOwner(participationId: Long, userId: Long): Boolean
}