package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.GatheringPlace
import org.springframework.data.jpa.repository.JpaRepository

interface GatheringPlaceRepository : JpaRepository<GatheringPlace, Long> {
    fun findAllByGatheringId(gatheringId: Long): List<GatheringPlace>
    fun deleteAllByGatheringId(gatheringId: Long)
}