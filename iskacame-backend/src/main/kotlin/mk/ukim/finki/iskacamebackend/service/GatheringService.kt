package mk.ukim.finki.iskacamebackend.service

import mk.ukim.finki.iskacamebackend.dto.request.CreateGatheringRequest
import mk.ukim.finki.iskacamebackend.dto.request.UpdateGatheringRequest
import mk.ukim.finki.iskacamebackend.dto.response.GatheringDetailsDto
import mk.ukim.finki.iskacamebackend.dto.response.GatheringSummaryDto
import mk.ukim.finki.iskacamebackend.exception.BadRequestException
import mk.ukim.finki.iskacamebackend.exception.CustomAccessDeniedException
import mk.ukim.finki.iskacamebackend.exception.ResourceNotFoundException
import mk.ukim.finki.iskacamebackend.model.Gathering

interface GatheringService {

    /**
     * Retrieves a gathering by ID
     *
     * @param id the ID of the gathering to retrieve
     * @return the Gathering entity
     */
    fun getGatheringById(id: Long): Gathering

    /**
     * Creates a new gathering with the given details and invites participants.
     *
     * @param request the gathering creation request
     * @return the created gathering as a DTO
     * @throws BadRequestException if the date range is invalid or no participants are invited
     */
    fun createGathering(request: CreateGatheringRequest): GatheringDetailsDto

    /**
     * Updates an existing gathering. Only the creator can update the gathering.
     *
     * @param gatheringId the ID of the gathering to update
     * @param request the update request containing new values
     * @return the updated gathering as a DTO
     * @throws ResourceNotFoundException if the gathering is not found
     * @throws CustomAccessDeniedException if the current user is not the creator
     * @throws BadRequestException if the gathering is finalized or cancelled
     */
    fun updateGathering(gatheringId: Long, request: UpdateGatheringRequest): GatheringDetailsDto

    /**
     * Cancels a gathering. Only the creator can cancel the gathering.
     *
     * @param gatheringId the ID of the gathering to cancel
     * @throws ResourceNotFoundException if the gathering is not found
     * @throws CustomAccessDeniedException if the current user is not the creator
     * @throws BadRequestException if the gathering is already cancelled
     */
    fun cancelGathering(gatheringId: Long)

    /**
     * Retrieves detailed information about a gathering including participants,
     * suggested places, and finalized details.
     *
     * @param gatheringId the ID of the gathering
     * @return the gathering details as a DTO
     * @throws ResourceNotFoundException if the gathering is not found
     * @throws CustomAccessDeniedException if the current user is not a participant
     */
    fun getGatheringDetails(gatheringId: Long): GatheringDetailsDto

    /**
     * Retrieves all gatherings that the current user has joined
     * (accepted invitations).
     *
     * @return list of gathering summaries the user is participating in
     */
    fun getMyGatherings(): List<GatheringSummaryDto>
}