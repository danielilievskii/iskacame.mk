package mk.ukim.finki.iskacamebackend.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import mk.ukim.finki.iskacamebackend.dto.response.GatheringDetailsDto
import mk.ukim.finki.iskacamebackend.dto.response.GatheringInvitationDto
import mk.ukim.finki.iskacamebackend.service.GatheringParticipationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/gatherings")
@Tag(name = "Gathering Participation", description = "Manages invitations and participation in gatherings")
class GatheringParticipationController(
    private val gatheringParticipationService: GatheringParticipationService
) {

    @GetMapping("/invitations")
    @Operation(summary = "Get all gathering invitations")
    fun getGatheringInvitations(): ResponseEntity<List<GatheringInvitationDto>> {

        val invitations = gatheringParticipationService.getGatheringInvitations()
        return ResponseEntity.ok(invitations)
    }

    @PostMapping("/{gatheringId}/invite/{userId}")
    @Operation(summary = "Invite user to gathering")
    fun inviteUserToGathering(
        @PathVariable gatheringId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<Void> {

        gatheringParticipationService.inviteUserToGathering(userId, gatheringId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/invitations/{id}/accept")
    @Operation(summary = "Accept invitation")
    fun acceptInvitation(@PathVariable id: Long): ResponseEntity<GatheringDetailsDto> {

        gatheringParticipationService.acceptInvitation(id)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/invitations/{id}/decline")
    @Operation(summary = "Decline invitation")
    fun declineInvitation(@PathVariable id: Long): ResponseEntity<Void> {

        gatheringParticipationService.declineInvitation(id)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{gatheringId}/leave")
    @Operation(summary = "Leave gathering")
    fun leaveGathering(@PathVariable gatheringId: Long): ResponseEntity<Void> {

        gatheringParticipationService.leaveGathering(gatheringId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{gatheringId}/participants/{userId}")
    @Operation(summary = "Remove user from gathering")
    fun removeUserFromGathering(
        @PathVariable gatheringId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<Void> {

        gatheringParticipationService.removeUserFromGathering(userId, gatheringId)
        return ResponseEntity.noContent().build()
    }
}