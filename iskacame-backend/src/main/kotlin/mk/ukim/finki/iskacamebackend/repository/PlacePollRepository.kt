package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.PlacePoll
import mk.ukim.finki.iskacamebackend.model.enums.PollStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface PlacePollRepository : JpaRepository<PlacePoll, Long> {

    fun findByGatheringId(gatheringId: Long): PlacePoll?

    fun findByGatheringIdAndStatus(gatheringId: Long, status: PollStatus): PlacePoll?

    fun findAllByStatusAndEndsAtBefore(status: PollStatus, now: Instant): List<PlacePoll>
}
