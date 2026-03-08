package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.Gathering
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GatheringRepository : JpaRepository<Gathering, Long> {
    fun existsByIdAndCreatorId(id: Long, creatorId: Long): Boolean
}