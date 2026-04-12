package mk.ukim.finki.iskacamebackend.service.intf

import mk.ukim.finki.iskacamebackend.dto.request.gathering.CastVoteRequest
import mk.ukim.finki.iskacamebackend.dto.request.gathering.CreatePollRequest
import mk.ukim.finki.iskacamebackend.dto.response.gathering.PlacePollDto

interface PlacePollService {

    fun createPoll(gatheringId: Long, request: CreatePollRequest): PlacePollDto

    fun getPoll(gatheringId: Long): PlacePollDto?

    fun castVote(gatheringId: Long, request: CastVoteRequest)

    fun endExpiredPolls()
}
