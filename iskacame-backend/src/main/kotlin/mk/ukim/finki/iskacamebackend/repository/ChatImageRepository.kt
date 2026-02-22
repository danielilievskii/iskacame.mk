package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.ChatImage
import org.springframework.data.jpa.repository.JpaRepository

interface ChatImageRepository : JpaRepository<ChatImage, Long>