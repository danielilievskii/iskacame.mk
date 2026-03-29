package mk.ukim.finki.iskacamebackend.service.intf

import mk.ukim.finki.iskacamebackend.model.domain.Place

interface PlaceSuggestionService {

    fun generateAndSaveSuggestions(gatheringId: Long): List<Place>
}