package mk.ukim.finki.iskacamebackend.service.intf

import mk.ukim.finki.iskacamebackend.dto.request.gathering.SubmitGatheringResponseRequest
import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringResponseOptionsDto
import mk.ukim.finki.iskacamebackend.model.domain.GatheringResponse
import mk.ukim.finki.iskacamebackend.model.domain.Place


interface GatheringResponseService {

    fun findAllByGatheringId(gatheringId: Long): List<GatheringResponse>

    /**
     * Retrieves the available response options for a gathering.
     *
     * Returns all possible [GatheringType] values and the time slots
     * generated for the given gathering, intended to be displayed to
     * the participant before submitting their response.
     *
     * @param gatheringId the ID of the gathering.
     * @return a [GatheringResponseOptionsDto] containing all available types and time slots.
     * @throws AccessDeniedException if the current user is not a participant of the gathering.
     */
    fun getGatheringResponseOptions(gatheringId: Long): GatheringResponseOptionsDto

    /**
     * Submits a new response for the current user to a gathering.
     *
     * Saves the participant's preferred [GatheringType]s and time slots.
     * Each participant may only submit one response per gathering - attempting
     * to submit again will throw an exception.
     *
     * @param gatheringId the ID of the gathering to respond to.
     * @param request the participant's selected types and time slot IDs.
     * @throws IllegalStateException if the current user has already submitted a response.
     * @throws AccessDeniedException if the current user is not a participant of the gathering.
     */
    fun submitGatheringResponse(gatheringId: Long, request: SubmitGatheringResponseRequest)

    /**
     * Updates the current user's existing response for a gathering.
     *
     * Replaces the participant's previously submitted type and time slot
     * preferences with the new values provided in the request.
     *
     * @param gatheringId the ID of the gathering whose response is being updated.
     * @param request the participant's updated types and time slot IDs.
     * @throws ResourceNotFoundException if the current user has not yet submitted a response.
     * @throws AccessDeniedException if the current user is not a participant of the gathering.
     */
    fun updateGatheringResponse(gatheringId: Long, request: SubmitGatheringResponseRequest)
}