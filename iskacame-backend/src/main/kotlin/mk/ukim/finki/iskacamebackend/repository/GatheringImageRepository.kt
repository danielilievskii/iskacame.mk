package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.GatheringImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GatheringImageRepository : JpaRepository<GatheringImage, Long> {
    fun findAllByGatheringId(gatheringId: Long): List<GatheringImage>
    fun existsByIdAndGatheringIdAndUploaderId(id: Long, gatheringId: Long, uploaderId: Long): Boolean
}