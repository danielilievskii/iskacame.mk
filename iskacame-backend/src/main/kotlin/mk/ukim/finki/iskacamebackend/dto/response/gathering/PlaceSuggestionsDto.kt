package mk.ukim.finki.iskacamebackend.dto.response.gathering

import mk.ukim.finki.iskacamebackend.model.enums.PriceLevel

data class PlaceSuggestionDto(
    val name: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val type: PlaceType,
    val priceLevel: PriceLevel
)
