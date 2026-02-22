package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.GatheringResponse
import org.springframework.data.jpa.repository.JpaRepository

interface GatheringResponseRepository : JpaRepository<GatheringResponse, Long>