package mk.ukim.finki.iskacamebackend.service.impl

import mk.ukim.finki.iskacamebackend.dto.request.gathering.CastVoteRequest
import mk.ukim.finki.iskacamebackend.dto.request.gathering.CreatePollRequest
import mk.ukim.finki.iskacamebackend.dto.response.gathering.PlacePollDto
import mk.ukim.finki.iskacamebackend.dto.response.gathering.PlacePollOptionDto
import mk.ukim.finki.iskacamebackend.exception.BadRequestException
import mk.ukim.finki.iskacamebackend.mapper.PlaceMapper
import mk.ukim.finki.iskacamebackend.model.domain.GatheringPlaceVote
import mk.ukim.finki.iskacamebackend.model.domain.PlacePoll
import mk.ukim.finki.iskacamebackend.model.enums.GatheringStatus
import mk.ukim.finki.iskacamebackend.model.enums.PollStatus
import mk.ukim.finki.iskacamebackend.repository.GatheringPlaceVoteRepository
import mk.ukim.finki.iskacamebackend.repository.GatheringRepository
import mk.ukim.finki.iskacamebackend.repository.GatheringResponseRepository
import mk.ukim.finki.iskacamebackend.repository.PlacePollRepository
import mk.ukim.finki.iskacamebackend.repository.PlaceRepository
import mk.ukim.finki.iskacamebackend.model.enums.NotificationType
import mk.ukim.finki.iskacamebackend.service.intf.AuthService
import mk.ukim.finki.iskacamebackend.service.intf.GatheringService
import mk.ukim.finki.iskacamebackend.service.intf.NotificationService
import mk.ukim.finki.iskacamebackend.service.intf.PlacePollService
import java.time.LocalDateTime
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PlacePollServiceImpl(
    private val placePollRepository: PlacePollRepository,
    private val placeRepository: PlaceRepository,
    private val gatheringPlaceVoteRepository: GatheringPlaceVoteRepository,
    private val gatheringService: GatheringService,
    private val gatheringRepository: GatheringRepository,
    private val gatheringResponseRepository: GatheringResponseRepository,
    private val authService: AuthService,
    private val placeMapper: PlaceMapper,
    private val notificationService: NotificationService,
) : PlacePollService {

    @Transactional
    @PreAuthorize("@permissionService.isGatheringCreator(#gatheringId, authentication.principal.id)")
    override fun createPoll(gatheringId: Long, request: CreatePollRequest): PlacePollDto {
        val gathering = gatheringService.getGatheringById(gatheringId)

        val existingPoll = placePollRepository.findByGatheringId(gatheringId)
        if (existingPoll != null) {
            throw BadRequestException("A poll already exists for this gathering.")
        }

        val places = placeRepository.findAllByGatheringId(gatheringId)
        if (places.isEmpty()) {
            throw BadRequestException("No suggested places available. Generate place suggestions first.")
        }

        val endsAt = Instant.now().plus(request.durationMinutes.toLong(), ChronoUnit.MINUTES)

        val poll = PlacePoll(
            gathering = gathering,
            status = PollStatus.ACTIVE,
            endsAt = endsAt
        )
        val savedPoll = placePollRepository.save(poll)

        notificationService.notifyGatheringParticipants(
            gatheringId,
            NotificationType.VOTE_STARTED,
            "Vote Started",
            "A place vote has started for \"${gathering.title}\". Cast your vote!"
        )

        return assemblePollDto(savedPoll, gatheringId)
    }

    @Transactional
    @PreAuthorize("@permissionService.isGatheringParticipant(#gatheringId, authentication.principal.id) " +
            "|| @permissionService.isGatheringCreator(#gatheringId, authentication.principal.id)")
    override fun getPoll(gatheringId: Long): PlacePollDto? {
        val poll = placePollRepository.findByGatheringId(gatheringId) ?: return null

        if (poll.status == PollStatus.ACTIVE && poll.endsAt.isBefore(Instant.now())) {
            poll.status = PollStatus.ENDED
            placePollRepository.save(poll)

            val gathering = gatheringService.getGatheringById(gatheringId)
            if (gathering.status != GatheringStatus.FINALIZED && gathering.status != GatheringStatus.CANCELLED) {
                val votes = gatheringPlaceVoteRepository.findAllByGatheringId(gatheringId)
                val winningPlace = votes
                    .groupBy { it.place }
                    .maxByOrNull { it.value.size }
                    ?.key
                    ?: placeRepository.findAllByGatheringId(gatheringId).randomOrNull()

                val responses = gatheringResponseRepository.findAllByGatheringId(gatheringId)
                val winningTimeSlot = responses
                    .flatMap { it.timeSlotPreferences }
                    .groupingBy { it }
                    .eachCount()
                    .maxByOrNull { it.value }
                    ?.key

                if (winningPlace != null) {
                    gathering.finalizedPlace = winningPlace
                }
                if (winningTimeSlot != null) {
                    gathering.finalizedTime = LocalDateTime.of(
                        winningTimeSlot.date,
                        java.time.LocalTime.of(winningTimeSlot.slot.startHour, 0)
                    )
                }
                gathering.status = GatheringStatus.FINALIZED
                gatheringRepository.save(gathering)
            }
        }

        return assemblePollDto(poll, gatheringId)
    }

    @Transactional
    @PreAuthorize("@permissionService.isGatheringParticipant(#gatheringId, authentication.principal.id)")
    override fun castVote(gatheringId: Long, request: CastVoteRequest) {
        val poll = placePollRepository.findByGatheringIdAndStatus(gatheringId, PollStatus.ACTIVE)
            ?: throw BadRequestException("No active poll found for this gathering.")

        if (poll.endsAt.isBefore(Instant.now())) {
            poll.status = PollStatus.ENDED
            placePollRepository.save(poll)
            throw BadRequestException("This poll has ended.")
        }

        val gathering = gatheringService.getGatheringById(gatheringId)
        val currentUser = authService.getCurrentUser()

        val places = placeRepository.findAllByGatheringId(gatheringId)
        val placeIds = places.map { it.id!! }.toSet()

        val invalidIds = request.placeIds.filter { it !in placeIds }
        if (invalidIds.isNotEmpty()) {
            throw BadRequestException("Invalid place IDs: $invalidIds")
        }
        gatheringPlaceVoteRepository.deleteAllByGatheringIdAndUserId(gatheringId, currentUser.id!!)

        val votes = request.placeIds.map { placeId ->
            GatheringPlaceVote(
                user = currentUser,
                gathering = gathering,
                place = places.first { it.id == placeId }
            )
        }
        gatheringPlaceVoteRepository.saveAll(votes)
    }

    @Transactional
    override fun endExpiredPolls() {
        val expiredPolls = placePollRepository.findAllByStatusAndEndsAtBefore(PollStatus.ACTIVE, Instant.now())
        for (poll in expiredPolls) {
            poll.status = PollStatus.ENDED
            placePollRepository.save(poll)

            val gathering = poll.gathering
            val gatheringId = gathering.id!!

            val votes = gatheringPlaceVoteRepository.findAllByGatheringId(gatheringId)
            val winningPlace = votes
                .groupBy { it.place }
                .maxByOrNull { it.value.size }
                ?.key
                ?: placeRepository.findAllByGatheringId(gatheringId).randomOrNull()

            val responses = gatheringResponseRepository.findAllByGatheringId(gatheringId)
            val winningTimeSlot = responses
                .flatMap { it.timeSlotPreferences }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key

            if (winningPlace != null) {
                gathering.finalizedPlace = winningPlace
            }
            if (winningTimeSlot != null) {
                gathering.finalizedTime = LocalDateTime.of(
                    winningTimeSlot.date,
                    java.time.LocalTime.of(winningTimeSlot.slot.startHour, 0)
                )
            }
            gathering.status = GatheringStatus.FINALIZED
            gatheringRepository.save(gathering)

            notificationService.notifyGatheringParticipants(
                gatheringId,
                NotificationType.VOTE_ENDED,
                "Vote Ended",
                "The place vote for \"${gathering.title}\" has ended. The gathering is now finalized!"
            )
        }
    }

    private fun assemblePollDto(poll: PlacePoll, gatheringId: Long): PlacePollDto {
        val places = placeRepository.findAllByGatheringId(gatheringId)
        val allVotes = gatheringPlaceVoteRepository.findAllByGatheringId(gatheringId)

        val currentUserId = authService.getCurrentUserId()
        val myVotedPlaceIds = allVotes
            .filter { it.user.id == currentUserId }
            .mapNotNull { it.place.id }

        val voteCountByPlaceId = allVotes.groupBy { it.place.id!! }.mapValues { it.value.size }

        val placeOptions = places.map { place ->
            PlacePollOptionDto(
                place = placeMapper.toPlaceDto(place),
                voteCount = voteCountByPlaceId[place.id!!] ?: 0
            )
        }

        return PlacePollDto(
            id = poll.id!!,
            status = poll.status,
            endsAt = poll.endsAt,
            createdAt = poll.createdAt!!,
            places = placeOptions,
            myVotedPlaceIds = myVotedPlaceIds
        )
    }
}
