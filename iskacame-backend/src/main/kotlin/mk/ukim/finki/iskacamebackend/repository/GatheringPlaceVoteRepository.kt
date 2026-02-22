package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.GatheringPlaceVote
import org.springframework.data.jpa.repository.JpaRepository

interface GatheringPlaceVoteRepository : JpaRepository<GatheringPlaceVote, Long>