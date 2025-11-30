package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository

interface ChatMessageRepository : JpaRepository<ChatMessage, Long>