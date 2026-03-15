package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatRoomRepository : JpaRepository<ChatRoom, Long>