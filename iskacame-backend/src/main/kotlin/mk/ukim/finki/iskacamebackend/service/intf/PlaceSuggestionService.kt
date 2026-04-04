package mk.ukim.finki.iskacamebackend.service.intf

import mk.ukim.finki.iskacamebackend.dto.response.gathering.PlaceDto
import mk.ukim.finki.iskacamebackend.model.domain.Place

interface PlaceSuggestionService {

    /**
     * Generates AI-powered place suggestions for a gathering based on participant responses
     * and persists them, replacing any previously generated suggestions.
     *
     * Aggregates participant type and time slot preferences from all submitted responses,
     * feeds them into a prompt template alongside the gathering context, and calls the AI model
     * to produce a list of place suggestions. Any previously saved suggestions for the gathering
     * are deleted before the new ones are saved.
     *
     * Only the gathering creator is authorized to trigger generation.
     *
     * @param gatheringId the ID of the gathering to generate suggestions for
     * @return the list of newly generated [PlaceDto]
     */
    fun generateSuggestions(gatheringId: Long): List<PlaceDto>
}