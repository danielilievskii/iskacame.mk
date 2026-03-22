package mk.ukim.finki.iskacamebackend.web

import mk.ukim.finki.iskacamebackend.dto.response.gathering.GatheringResponseOptionsDto
import mk.ukim.finki.iskacamebackend.service.intf.GatheringResponseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import mk.ukim.finki.iskacamebackend.dto.request.gathering.SubmitGatheringResponseRequest

@RestController
@RequestMapping("/api/gatherings/{gatheringId}/responses")
@Tag(
    name = "Gathering Responses",
    description = "Endpoints for submitting, updating, and retrieving responses for gatherings"
)
class GatheringResponseController(
    private val gatheringResponseService: GatheringResponseService
) {

    @Operation(
        summary = "Get response options",
        description = "Retrieve available response options for a given gathering."
    )
    @GetMapping("/options")
    fun getGatheringResponseOptions(
        @Parameter(description = "ID of the gathering") @PathVariable gatheringId: Long
    ): ResponseEntity<GatheringResponseOptionsDto> {
        return ResponseEntity.ok(gatheringResponseService.getGatheringResponseOptions(gatheringId))
    }

    @Operation(
        summary = "Submit a new response",
        description = "Submit a new response for a given gathering."
    )
    @PostMapping
    fun submitGatheringResponse(
        @Parameter(description = "ID of the gathering") @PathVariable gatheringId: Long,
        @RequestBody request: SubmitGatheringResponseRequest
    ): ResponseEntity<Unit> {
        gatheringResponseService.submitGatheringResponse(gatheringId, request)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @Operation(
        summary = "Update an existing response",
        description = "Update an already submitted response for a given gathering."
    )
    @PutMapping
    fun updateGatheringResponse(
        @Parameter(description = "ID of the gathering") @PathVariable gatheringId: Long,
        @RequestBody request: SubmitGatheringResponseRequest
    ): ResponseEntity<Unit> {
        gatheringResponseService.updateGatheringResponse(gatheringId, request)
        return ResponseEntity.noContent().build()
    }
}