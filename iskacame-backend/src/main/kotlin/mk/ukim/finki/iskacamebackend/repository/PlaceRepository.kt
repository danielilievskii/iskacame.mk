package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.Place
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlaceRepository : JpaRepository<Place, Long> {
    fun findAllByGatheringId(gatheringId: Long): List<Place>
    fun deleteAllByGatheringId(gatheringId: Long)
}