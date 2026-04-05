package mk.ukim.finki.iskacamebackend.service.intf

import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringDetailsDto
import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringInvitationDto


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
     * Updates the participation status to [ParticipationStatus.JOINED] and
     * creates a [ChatRoomReceipt] for the user in the gathering's chat room.
     *
     * @param participationId the ID of the gathering participation
     * @return details of the accepted gathering
     * @throws BadRequestException if the participation status is not [ParticipationStatus.INVITED]
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
     * Updates the participation status to [ParticipationStatus.LEFT] and
     * deletes the user's [ChatRoomReceipt] from the gathering's chat room.
     *
     * @param gatheringId the ID of the gathering to leave
     * @throws BadRequestException if the user does not have an active [ParticipationStatus.JOINED] participation
     */
    fun leaveGathering(gatheringId: Long)

    /**
     * Removes a user from an existing gathering.
     *
     * Updates the participation status to [ParticipationStatus.REMOVED] and
     * deletes the user's [ChatRoomReceipt] from the gathering's chat room.
     *
     * Only the creator of the gathering can remove participants.
     *
     * @param userId the ID of the user to remove
     * @param gatheringId the ID of the gathering
     * @throws BadRequestException if the target user does not have an active [ParticipationStatus.JOINED] participation
     */
    fun removeUserFromGathering(userId: Long, gatheringId: Long)
}