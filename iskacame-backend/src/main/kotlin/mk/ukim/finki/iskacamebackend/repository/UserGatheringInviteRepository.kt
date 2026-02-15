package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.UserGatheringInvite
import mk.ukim.finki.iskacamebackend.model.enums.InviteStatus
import org.springframework.data.jpa.repository.JpaRepository

interface UserGatheringInviteRepository : JpaRepository<UserGatheringInvite, Long> {
    fun findByGatheringIdAndUserId(gatheringId: Long, userId: Long): UserGatheringInvite?
    fun findAllByGatheringId(gatheringId: Long): List<UserGatheringInvite>
    fun findAllByUserId(userId: Long): List<UserGatheringInvite>
    fun findAllByUserIdAndStatus(userId: Long, status: InviteStatus): List<UserGatheringInvite>
}