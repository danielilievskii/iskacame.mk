package mk.ukim.finki.iskacamebackend.repository

import mk.ukim.finki.iskacamebackend.model.domain.DeviceToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceTokenRepository : JpaRepository<DeviceToken, Long> {

    fun findAllByUserId(userId: Long): List<DeviceToken>

    fun findByToken(token: String): DeviceToken?

    fun deleteByToken(token: String)

    fun findAllByUserIdIn(userIds: List<Long>): List<DeviceToken>
}
