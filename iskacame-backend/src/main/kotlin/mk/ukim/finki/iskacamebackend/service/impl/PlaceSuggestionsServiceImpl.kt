package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.model.domain.Place
import mk.ukim.finki.iskacamebackend.model.enums.PriceLevel
import mk.ukim.finki.iskacamebackend.repository.GatheringResponseRepository
import mk.ukim.finki.iskacamebackend.repository.PlaceRepository
import mk.ukim.finki.iskacamebackend.service.intf.GatheringService
import mk.ukim.finki.iskacamebackend.service.intf.PlaceSuggestionService
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class PlaceSuggestionsServiceImpl(
    private val placeRepository: PlaceRepository,
    private val gatheringService: GatheringService,
    private val gatheringResponseRepository: GatheringResponseRepository,
    private val chatClient: ChatClient,
) : PlaceSuggestionService {

    private val promptResource = ClassPathResource("prompts/place-suggestions.st")

    @PreAuthorize("@permissionService.isGatheringCreator(#gatheringId, authentication.principal.id)")
    override fun generateAndSaveSuggestions(gatheringId: Long): List<Place> {

        val gathering = gatheringService.getGatheringById(gatheringId)
        val allResponses = gatheringResponseRepository.findAllByGatheringId(gatheringId)

        val mostPreferredTypes = allResponses
            .flatMap { it.typePreferences }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .joinToString(", ") { it.key.name }
            .ifEmpty { "No preferences submitted yet" }

        val mostPreferredTimeSlots = allResponses
            .flatMap { it.timeSlotPreferences }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .joinToString(", ") { "${it.key.date} ${it.key.slot}" }
            .ifEmpty { "No time slot preferences submitted yet" }

        val prompt = PromptTemplate(promptResource)
            .apply {
                add("count", 3)
                add("title", gathering.title)
                add("description", gathering.description)
                add("mostPreferredTypes", mostPreferredTypes)
                add("mostPreferredTimeSlots", mostPreferredTimeSlots)
                add("priceLevels", PriceLevel.entries)
            }
            .create()

        val suggestions = chatClient
            .prompt(prompt)
            .call()
            .entity(object : ParameterizedTypeReference<List<PlaceSuggestionDto>>() {})

        val places = suggestions?.map {
            Place(
                name = it.name,
                address = it.address,
                latitude = it.latitude,
                longitude = it.longitude,
                type = it.type,
                priceLevel = it.priceLevel,
                link = null,
                owner = currentUser
            )
        } ?: emptyList()

        return placeRepository.saveAll(places)
    }
}