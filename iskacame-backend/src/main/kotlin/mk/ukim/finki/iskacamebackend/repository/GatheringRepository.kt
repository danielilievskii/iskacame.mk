package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.Gathering
import org.springframework.data.jpa.repository.JpaRepository

interface GatheringRepository : JpaRepository<Gathering, Long> {
    fun existsByIdAndCreatorId(id: Long, creatorId: Long): Boolean
}