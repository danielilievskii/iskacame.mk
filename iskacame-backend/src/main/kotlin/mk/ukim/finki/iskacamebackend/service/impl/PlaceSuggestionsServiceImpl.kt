package mk.ukim.finki.iskacamebackend.service.impl

import jakarta.transaction.Transactional
import mk.ukim.finki.iskacamebackend.dto.response.gathering.PlaceDto
import mk.ukim.finki.iskacamebackend.exception.BadRequestException
import mk.ukim.finki.iskacamebackend.mapper.PlaceMapper
import mk.ukim.finki.iskacamebackend.model.domain.GatheringResponse
import mk.ukim.finki.iskacamebackend.model.domain.Place
import mk.ukim.finki.iskacamebackend.model.enums.PriceLevel
import mk.ukim.finki.iskacamebackend.repository.PlacePollRepository
import mk.ukim.finki.iskacamebackend.repository.PlaceRepository
import mk.ukim.finki.iskacamebackend.service.intf.GatheringResponseService
import mk.ukim.finki.iskacamebackend.service.intf.GatheringService
import mk.ukim.finki.iskacamebackend.service.intf.PlaceSuggestionService
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.ClassPathResource
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class PlaceSuggestionsServiceImpl(
    private val placeRepository: PlaceRepository,
    private val placePollRepository: PlacePollRepository,
    private val gatheringService: GatheringService,
    private val gatheringResponseService: GatheringResponseService,
    private val chatClient: ChatClient,
    private val placeMapper: PlaceMapper,
) : PlaceSuggestionService {

    private val promptResource = ClassPathResource("prompts/place-suggestions.st")

    @Transactional
    @PreAuthorize("@permissionService.isGatheringCreator(#gatheringId, authentication.principal.id)")
    override fun generateSuggestions(gatheringId: Long): List<PlaceDto> {

        if (placePollRepository.findByGatheringId(gatheringId) != null) {
            throw BadRequestException("Cannot regenerate places after a poll has been created.")
        }

        val gathering = gatheringService.getGatheringById(gatheringId)
        val responses = gatheringResponseService.findAllByGatheringId(gatheringId)

        val topTypePreferences = resolveTopTypePreferences(responses)
        val topTimeSlotPreferences = resolveTopTimeSlotPreferences(responses)

        val priceLevels = PriceLevel.entries.joinToString(",")

        val prompt = PromptTemplate(promptResource)
            .apply {
                add("count", 3)
                add("title", gathering.title)
                add("description", gathering.description)
                add("topTypePreferences", topTypePreferences)
                add("topTimeSlotPreferences", topTimeSlotPreferences)
                add("priceLevels", priceLevels)
            }
            .create()

        val suggestions = chatClient
            .prompt(prompt)
            .call()
            .entity(object : ParameterizedTypeReference<List<PlaceDto>>() {})

        val places = suggestions?.map {
            Place(
                gathering = gathering,
                name = it.name,
                address = it.address,
                latitude = it.latitude,
                longitude = it.longitude,
                type = it.type,
                priceLevel = it.priceLevel,
            )
        } ?: emptyList()

        placeRepository.deleteAllByGatheringId(gatheringId)
        val savedPlaces = placeRepository.saveAll(places)

        return savedPlaces.map { placeMapper.toPlaceDto(it) }
    }

    /**
     * Aggregates type preferences across all gathering responses and returns
     * the top 3 most frequently chosen types as a comma-separated string.
     *
     * Example: "FOOD, CASUAL, SPORT"
     *
     * @param responses all submitted responses for the gathering
     * @return a formatted string of the top gathering types, or a fallback message if none exist
     */
    private fun resolveTopTypePreferences(responses: List<GatheringResponse>): String =
        responses
            .flatMap { it.typePreferences }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .joinToString(", ") { it.key.name }
            .ifEmpty { "No preferences submitted yet" }

    /**
     * Aggregates time slot preferences across all gathering responses and returns
     * the top 3 most frequently chosen time slots as a comma-separated string.
     *
     * Example: "2025-06-14 EVENING, 2025-06-15 MORNING, 2025-06-14 AFTERNOON"
     *
     * @param responses all submitted responses for the gathering
     * @return a formatted string of the top time slots, or a fallback message if none exist
     */
    private fun resolveTopTimeSlotPreferences(responses: List<GatheringResponse>): String =
        responses
            .flatMap { it.timeSlotPreferences }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .joinToString(", ") { "${it.key.date} ${it.key.slot}" }
            .ifEmpty { "No time slot preferences submitted yet" }
}