package mk.ukim.finki.iskacamebackend.web

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import mk.ukim.finki.iskacamebackend.dto.request.CreateGatheringRequest
import mk.ukim.finki.iskacamebackend.dto.request.UpdateGatheringRequest
import mk.ukim.finki.iskacamebackend.dto.response.GatheringDto
import mk.ukim.finki.iskacamebackend.service.GatheringService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/gatherings")
class GatheringController(
    private val gatheringService: GatheringService
) {

    @PostMapping
    @Operation(summary = "Create a new gathering")
    fun createGathering(
        @Valid @RequestBody request: CreateGatheringRequest
    ): ResponseEntity<GatheringDto> {
        val gathering = gatheringService.createGathering(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(gathering)
    }

    @PutMapping("/{gatheringId}")
    @Operation(summary = "Update a gathering (creator only)")
    fun updateGathering(
        @PathVariable gatheringId: Long,
        @Valid @RequestBody request: UpdateGatheringRequest
    ): ResponseEntity<GatheringDto> {
        val gathering = gatheringService.updateGathering(gatheringId, request)
        return ResponseEntity.ok(gathering)
    }

    @DeleteMapping("/{gatheringId}")
    @Operation(summary = "Cancel a gathering (creator only)")
    fun cancelGathering(
        @PathVariable gatheringId: Long
    ): ResponseEntity<Void> {
        gatheringService.cancelGathering(gatheringId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{gatheringId}")
    @Operation(summary = "Get gathering details")
    fun getGatheringDetails(
        @PathVariable gatheringId: Long
    ): ResponseEntity<GatheringDto> {
        val gathering = gatheringService.getGatheringDetails(gatheringId)
        return ResponseEntity.ok(gathering)
    }

    @GetMapping("/my-gatherings")
    @Operation(summary = "Get all gatherings for the current user")
    fun getMyGatherings(): ResponseEntity<List<GatheringDto>> {
        val gatherings = gatheringService.getMyGatherings()
        return ResponseEntity.ok(gatherings)
    }
}