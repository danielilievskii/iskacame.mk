package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.GatheringPlace
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GatheringPlaceRepository : JpaRepository<GatheringPlace, Long> {
    fun findAllByGatheringId(gatheringId: Long): List<GatheringPlace>
    fun deleteAllByGatheringId(gatheringId: Long)
}