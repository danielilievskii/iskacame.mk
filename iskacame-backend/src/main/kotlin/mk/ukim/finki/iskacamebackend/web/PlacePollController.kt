package mk.ukim.finki.iskacamebackend.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mk.ukim.finki.iskacamebackend.dto.request.gathering.CastVoteRequest
import mk.ukim.finki.iskacamebackend.dto.request.gathering.CreatePollRequest
import mk.ukim.finki.iskacamebackend.dto.response.gathering.PlacePollDto
import mk.ukim.finki.iskacamebackend.service.intf.PlacePollService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/gatherings/{gatheringId}/poll")
@Tag(name = "Place Poll", description = "Manages place voting polls for gatherings")
class PlacePollController(
    private val placePollService: PlacePollService
) {

    @PostMapping
    @Operation(summary = "Create a place vote poll (creator only)")
    fun createPoll(
        @PathVariable gatheringId: Long,
        @Valid @RequestBody request: CreatePollRequest
    ): ResponseEntity<PlacePollDto> {
        val poll = placePollService.createPoll(gatheringId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(poll)
    }

    @GetMapping
    @Operation(summary = "Get the poll for a gathering")
    fun getPoll(@PathVariable gatheringId: Long): ResponseEntity<PlacePollDto> {
        val poll = placePollService.getPoll(gatheringId)
            ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(poll)
    }

    @PostMapping("/vote")
    @Operation(summary = "Cast vote(s) for places")
    fun castVote(
        @PathVariable gatheringId: Long,
        @Valid @RequestBody request: CastVoteRequest
    ): ResponseEntity<Void> {
        placePollService.castVote(gatheringId, request)
        return ResponseEntity.noContent().build()
    }
}
