package mk.ukim.finki.iskacamebackend.service

import mk.ukim.finki.iskacamebackend.dto.response.GatheringDetailsDto
import mk.ukim.finki.iskacamebackend.dto.response.GatheringInvitationDto


interface GatheringParticipationService {

    /**
     * Retrieves all gathering invitations for the current user.
     *
     * @return list of gathering invitations
     */
    fun getGatheringInvitations(): List<GatheringInvitationDto>

    /**
     * Invites a user to an existing gathering.
     *
     * @param userId the ID of the user to invite
     * @param gatheringId the ID of the gathering
     */
    fun inviteUserToGathering(userId: Long, gatheringId: Long)

    /**
     * Accepts a gathering invitation for the current user.
     *
     * @param participationId the ID of the gathering participation
     * @return details of the accepted gathering
     */
    fun acceptInvitation(participationId: Long): GatheringDetailsDto

    /**
     * Declines a gathering invitation for the current user.
     *
     * @param participationId the ID of the gathering participation
     */
    fun declineInvitation(participationId: Long)

    /**
     * Leaves a gathering the current user has already joined.
     *
     * @param gatheringId the ID of the gathering to leave
     */
    fun leaveGathering(gatheringId: Long)

    /**
     * Removes a user from an existing gathering.
     * Only the creator of the gathering can remove participants.
     *
     * @param userId the ID of the user to remove
     * @param gatheringId the ID of the gathering
     */
    fun removeUserFromGathering(userId: Long, gatheringId: Long)
}